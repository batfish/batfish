package org.batfish.minesweeper.bdd;

import static com.google.common.base.Preconditions.checkState;
import static org.batfish.datamodel.LineAction.PERMIT;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import dk.brics.automaton.Automaton;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import javax.annotation.Nullable;
import net.sf.javabdd.BDD;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.OriginMechanism;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.ReceivedFromSelf;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.answers.NextHopBgpPeerAddress;
import org.batfish.datamodel.answers.NextHopSelf;
import org.batfish.datamodel.bgp.TunnelEncapsulationAttribute;
import org.batfish.datamodel.bgp.community.Community;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.batfish.datamodel.bgp.community.LargeCommunity;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.questions.BgpRoute;
import org.batfish.datamodel.route.nh.NextHop;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.route.nh.NextHopInterface;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.minesweeper.CommunityVar;
import org.batfish.minesweeper.ConfigAtomicPredicates;
import org.batfish.minesweeper.bdd.BDDTunnelEncapsulationAttribute.Value;
import org.batfish.minesweeper.bdd.BDDTunnelEncapsulationAttribute.Value.Type;
import org.batfish.minesweeper.utils.RouteMapEnvironment;
import org.batfish.question.testroutepolicies.Result;
import org.batfish.question.testroutepolicies.TestRoutePoliciesAnswerer;

public class ModelGeneration {
  private static final Logger LOGGER = LogManager.getLogger(ModelGeneration.class);

  private static Optional<Community> stringToCommunity(String str) {
    Optional<StandardCommunity> scomm = StandardCommunity.tryParse(str);
    if (scomm.isPresent()) {
      return Optional.of(scomm.get());
    }
    Optional<ExtendedCommunity> ecomm = ExtendedCommunity.tryParse(str);
    if (ecomm.isPresent()) {
      return Optional.of(ecomm.get());
    }
    Optional<LargeCommunity> lcomm = LargeCommunity.tryParse(str);
    if (lcomm.isPresent()) {
      return Optional.of(lcomm.get());
    }
    return Optional.empty();
  }

  /**
   * Determine whether a given boolean formula must be true according to the given satisfying
   * assignment. This method properly handles assignments that are partial (missing truth values for
   * some variables).
   *
   * @param b the boolean formula, represented as a BDD
   * @param model the model
   * @return a boolean
   */
  static boolean mustBeTrueInModel(BDD b, BDD model) {
    return !model.diffSat(b);
  }

  /**
   * Given a single satisfying assignment to the constraints from symbolic route analysis, produce a
   * set of communities for a given symbolic route that is consistent with the assignment.
   *
   * @param model a (possibly partial) model of the symbolic route constraints
   * @param r the symbolic route
   * @param configAPs an object that provides information about the community atomic predicates
   * @return a set of communities
   */
  static Set<Community> satAssignmentToCommunities(
      BDD model, BDDRoute r, ConfigAtomicPredicates configAPs) {

    BDD[] aps = r.getCommunityAtomicPredicates();
    Map<Integer, Automaton> apAutomata =
        configAPs.getStandardCommunityAtomicPredicates().getAtomicPredicateAutomata();

    ImmutableSet.Builder<Community> comms = new ImmutableSet.Builder<>();

    int numStandardAPs = configAPs.getStandardCommunityAtomicPredicates().getNumAtomicPredicates();
    // handle standard community literals and regexes
    for (int i = 0; i < numStandardAPs; i++) {
      if (mustBeTrueInModel(aps[i], model)) {
        Automaton a = apAutomata.get(i);
        // community atomic predicates should always be non-empty;
        // see RegexAtomicPredicates::initAtomicPredicates
        checkState(!a.isEmpty(), "Cannot produce example string for empty automaton");
        String str = a.getShortestExample(true);
        // community automata should only accept strings with this property;
        // see CommunityVar::toAutomaton
        checkState(
            str.startsWith("^") && str.endsWith("$"),
            "Community example %s has an unexpected format",
            str);
        // strip off the leading ^ and trailing $
        str = str.substring(1, str.length() - 1);
        Optional<Community> exampleOpt = stringToCommunity(str);
        if (exampleOpt.isPresent()) {
          comms.add(exampleOpt.get());
        } else {
          throw new BatfishException("Failed to produce a valid community for answer");
        }
      }
    }
    // handle extended/large community literals
    for (Map.Entry<Integer, CommunityVar> entry :
        configAPs.getNonStandardCommunityLiterals().entrySet()) {
      if (mustBeTrueInModel(aps[entry.getKey()], model)) {
        assert entry.getValue().getLiteralValue() != null;
        comms.add(entry.getValue().getLiteralValue());
      }
    }
    return comms.build();
  }

  /**
   * Given a single satisfying assignment to the constraints from symbolic route analysis, produce
   * an AS-path for a given symbolic route that is consistent with the assignment.
   *
   * @param model a (possibly partial) model of the symbolic route constraints
   * @param r the symbolic route
   * @param configAPs an object provides information about the AS-path regex atomic predicates
   * @return an AsPath
   */
  static AsPath satAssignmentToAsPath(BDD model, BDDRoute r, ConfigAtomicPredicates configAPs) {

    Integer ap = r.getAsPathRegexAtomicPredicates().satAssignmentToValue(model);
    Map<Integer, Automaton> apAutomata =
        configAPs.getAsPathRegexAtomicPredicates().getAtomicPredicateAutomata();
    Automaton asPathRegexAutomaton = apAutomata.get(ap);

    String asPathStr = asPathRegexAutomaton.getShortestExample(true);
    // As-path regex automata should only accept strings with this property;
    // see SymbolicAsPathRegex::toAutomaton
    checkState(
        asPathStr.startsWith("^^") && asPathStr.endsWith("$"),
        "AS-path example %s has an unexpected format",
        asPathStr);
    // strip off the leading ^^ and trailing $
    asPathStr = asPathStr.substring(2, asPathStr.length() - 1);
    // the string is a space-separated list of numbers; convert them to a list of numbers
    List<Long> asns;
    if (asPathStr.isEmpty()) {
      asns = ImmutableList.of();
    } else {
      try {
        asns =
            Arrays.stream(asPathStr.split(" "))
                .mapToLong(Long::valueOf)
                .boxed()
                .collect(Collectors.toList());
      } catch (NumberFormatException nfe) {
        throw new BatfishException("Failed to produce a valid AS path for answer");
      }
    }
    return AsPath.ofSingletonAsSets(asns);
  }

  /**
   * Given a single satisfying assignment to the constraints from symbolic route analysis, produce a
   * next-hop for a given symbolic route that is consistent with the assignment.
   *
   * @param model a (possibly partial) model of the symbolic route constraints
   * @param r the symbolic route
   * @param configAPs an object provides information about the AS-path regex atomic predicates
   * @return a next-hop
   */
  static NextHop satAssignmentToNextHop(BDD model, BDDRoute r, ConfigAtomicPredicates configAPs) {
    Ip ip = Ip.create(r.getNextHop().satAssignmentToLong(model));
    // if we matched on a next-hop interface then include the interface name in the produced
    // next-hop
    String nextHopInterface =
        optionalSatisfyingItem(configAPs.getNextHopInterfaces(), r.getNextHopInterfaces(), model);
    if (nextHopInterface == null) {
      return NextHopIp.of(ip);
    } else {
      return NextHopInterface.of(nextHopInterface, ip);
    }
  }

  /**
   * Given a single satisfying assignment to the constraints from symbolic route analysis, produce
   * an optional {@link TunnelEncapsulationAttribute} for a given symbolic route that is consistent
   * with the assignment.
   */
  static @Nullable TunnelEncapsulationAttribute satAssignmentToTunnelEncapsulationAttribute(
      BDD model, BDDRoute r) {
    Value value = r.getTunnelEncapsulationAttribute().satAssignmentToValue(model);
    if (value.type() == Type.ABSENT) {
      return null;
    } else if (value.type() == Type.LITERAL) {
      return value.value();
    }
    // BgpRoute requires a concrete value, so make one up.
    assert value.type() == Type.OTHER;
    return new TunnelEncapsulationAttribute(Ip.create(1));
  }

  /**
   * Given a satisfying assignment to the constraints from symbolic route analysis, produce a
   * concrete input route that is consistent with the assignment.
   *
   * @param model the (possibly partial) satisfying assignment
   * @param configAPs an object that provides information about the atomic predicates in the model
   * @return a route
   */
  public static AbstractRoute satAssignmentToInputRoute(
      BDD model, ConfigAtomicPredicates configAPs) {
    BDDRoute bddRoute = new BDDRoute(model.getFactory(), configAPs);
    RoutingProtocol p = bddRoute.getProtocolHistory().satAssignmentToValue(model);
    if (p == RoutingProtocol.STATIC) {
      return satAssignmentToStaticInputRoute(model, configAPs);
    } else if (BDDRoute.ALL_BGP_PROTOCOLS.contains(p)) {
      return satAssignmentToBgpInputRoute(model, configAPs);
    } else {
      throw new IllegalArgumentException("Unexpected routing protocol " + p);
    }
  }

  /**
   * Given a satisfying assignment to the constraints from symbolic route analysis, produce a
   * concrete input BGP route that is consistent with the assignment.
   *
   * @param model the (possibly partial) satisfying assignment
   * @param configAPs an object that provides information about the atomic predicates in the model
   * @return a BGP route
   */
  public static Bgpv4Route satAssignmentToBgpInputRoute(
      BDD model, ConfigAtomicPredicates configAPs) {
    return satAssignmentToBgpRoute(model, new BDDRoute(model.getFactory(), configAPs), configAPs)
        .build();
  }

  /**
   * Given a satisfying assignment to the constraints from symbolic route analysis, produce a
   * concrete input static route that is consistent with the assignment.
   *
   * @param model the (possibly partial) satisfying assignment
   * @param configAPs an object that provides information about the atomic predicates in the model
   * @return a static route
   */
  private static StaticRoute satAssignmentToStaticInputRoute(
      BDD model, ConfigAtomicPredicates configAPs) {

    BDDRoute bddRoute = new BDDRoute(model.getFactory(), configAPs);

    StaticRoute.Builder builder = StaticRoute.builder();
    // dummy value
    builder.setAdministrativeCost(0);
    Ip ip = Ip.create(bddRoute.getPrefix().satAssignmentToLong(model));
    long len = bddRoute.getPrefixLength().satAssignmentToLong(model);
    builder.setNetwork(Prefix.create(ip, (int) len));
    builder.setNextHop(satAssignmentToNextHop(model, bddRoute, configAPs));
    builder.setTag(bddRoute.getTag().satAssignmentToLong(model));

    return builder.build();
  }

  /**
   * Check whether the results of symbolic analysis are consistent with a given concrete
   * input-output result (which would typically come from running Batfish's route-map simulation
   * question {@link org.batfish.question.testroutepolicies.TestRoutePoliciesQuestion}).
   * Specifically, check that the symbolic analysis agrees with the given result, on the action
   * (permit or deny) that the route map will take on the given input route announcement as well as
   * on the output route that will result (in the case that the route is permitted).
   *
   * @param model a (possibly partial) satisfying assignment to the constraints from symbolic route
   *     analysis along some path
   * @param bddRoute a symbolic representation of the output route produced along that path
   * @param configAPs the {@link ConfigAtomicPredicates} object, which enables proper interpretation
   *     of atomic predicates in the bddRoute
   * @param action the action that the symbolic analysis determined is taken on that path
   * @param direction whether the route map is used as an import or export policy
   * @param expectedResult the expected input-output behavior
   * @return a boolean indicating whether the check succeeded
   */
  public static boolean validateModel(
      BDD model,
      BDDRoute bddRoute,
      ConfigAtomicPredicates configAPs,
      LineAction action,
      Environment.Direction direction,
      Result<?, BgpRoute> expectedResult) {
    if (!expectedResult.getAction().equals(action)) {
      LOGGER.warn(
          "Mismatched action for input {}: simulation {} model {}",
          expectedResult.getInputRoute(),
          expectedResult.getAction(),
          action);
      return false;
    }
    if (action == PERMIT) {
      assert expectedResult.getOutputRoute() != null;
      BgpRoute outputRouteFromModel =
          satAssignmentToOutputRoute(model, bddRoute, configAPs, direction);
      boolean result = expectedResult.getOutputRoute().equals(outputRouteFromModel);
      if (!result) {
        LOGGER.warn(
            "Mismatched output route for input {}: simulation {} model {}",
            expectedResult.getInputRoute(),
            expectedResult.getOutputRoute(),
            outputRouteFromModel);
      }
      return result;
    }
    return true;
  }

  /**
   * Produce the concrete output BGP route that is represented by the given assignment of values to
   * BDD variables as well as resulting {@link BDDRoute} from the symbolic route analysis.
   *
   * <p>Note: This method assumes that any AS-prepending that happens along the given path has
   * already been accounted for through an update to the AS-path atomic predicates appropriately
   * (see {@link org.batfish.minesweeper.AsPathRegexAtomicPredicates#prependAPs(List)}).
   *
   * @param model the (possibly partial) satisfying assignment
   * @param bddRoute symbolic representation of the output route
   * @param configAPs an object that provides information about the atomic predicates in the model
   *     and bddRoute
   * @param direction whether the route map is used as an import or export policy
   * @return a BGP route
   */
  private static BgpRoute satAssignmentToOutputRoute(
      BDD model,
      BDDRoute bddRoute,
      ConfigAtomicPredicates configAPs,
      Environment.Direction direction) {
    Bgpv4Route.Builder v4Builder = satAssignmentToBgpRoute(model, bddRoute, configAPs);
    if (v4Builder.getProtocol() == RoutingProtocol.STATIC) {
      // if the input route is a static route then set the output route's attributes consistently
      // with what the concrete route simulation does (see
      // TestRoutePoliciesAnswerer::simulatePolicyWithStaticRoute)
      v4Builder.setProtocol(RoutingProtocol.BGP);
      v4Builder.setSrcProtocol(RoutingProtocol.STATIC);
      v4Builder.setOriginType(OriginType.INCOMPLETE);
      v4Builder.setOriginMechanism(OriginMechanism.NETWORK);
    }
    BgpRoute.Builder builder =
        TestRoutePoliciesAnswerer.toQuestionBgpRoute(v4Builder.build()).toBuilder();
    if (direction == Environment.Direction.OUT && !bddRoute.getNextHopSet()) {
      // in the OUT direction the next hop is ignored unless explicitly set
      builder.setNextHopConcrete(NextHopDiscard.instance());
    } else {
      switch (bddRoute.getNextHopType()) {
        case BGP_PEER_ADDRESS:
          builder.setNextHop(NextHopBgpPeerAddress.instance());
          break;
        case DISCARDED:
          builder.setNextHopConcrete(NextHopDiscard.instance());
          break;
        case SELF:
          builder.setNextHop(NextHopSelf.instance());
          break;
        default:
          break;
      }
    }

    return builder.build();
  }

  /**
   * Produce a builder for the concrete BGP route that is represented by the given assignment of
   * values to BDD variables and {@link BDDRoute} from the symbolic route analysis.
   *
   * @param model the (possibly partial) satisfying assignment
   * @param bddRoute symbolic representation of the desired route
   * @param configAPs an object that provides information about the atomic predicates in the model
   *     and bddRoute
   * @return a route builder
   */
  private static Bgpv4Route.Builder satAssignmentToBgpRoute(
      BDD model, BDDRoute bddRoute, ConfigAtomicPredicates configAPs) {

    Bgpv4Route.Builder builder =
        Bgpv4Route.builder()
            .setOriginatorIp(Ip.ZERO) /* dummy value until supported */
            .setReceivedFrom(ReceivedFromSelf.instance()) /* dummy value until supported */
            .setOriginMechanism(OriginMechanism.LEARNED) /* dummy value until supported */;

    Ip ip = Ip.create(bddRoute.getPrefix().satAssignmentToLong(model));
    long len = bddRoute.getPrefixLength().satAssignmentToLong(model);
    builder.setNetwork(Prefix.create(ip, (int) len));

    builder.setLocalPreference(bddRoute.getLocalPref().satAssignmentToLong(model));
    builder.setAdmin(bddRoute.getAdminDist().satAssignmentToInt(model));
    builder.setMetric(bddRoute.getMed().satAssignmentToLong(model));
    builder.setTag(bddRoute.getTag().satAssignmentToLong(model));
    builder.setWeight(bddRoute.getWeight().satAssignmentToInt(model));
    builder.setOriginType(bddRoute.getOriginType().satAssignmentToValue(model));
    builder.setProtocol(bddRoute.getProtocolHistory().satAssignmentToValue(model));

    // if the cluster list length is N, create the cluster list 0,...,N-1
    long clusterListLength = bddRoute.getClusterListLength().satAssignmentToLong(model);
    builder.setClusterList(
        LongStream.range(0, clusterListLength).boxed().collect(ImmutableSet.toImmutableSet()));

    Set<Community> communities = satAssignmentToCommunities(model, bddRoute, configAPs);
    builder.setCommunities(communities);

    AsPath asPath = satAssignmentToAsPath(model, bddRoute, configAPs);
    builder.setAsPath(asPath);

    NextHop nextHop = satAssignmentToNextHop(model, bddRoute, configAPs);
    builder.setNextHop(nextHop);

    builder.setTunnelEncapsulationAttribute(
        satAssignmentToTunnelEncapsulationAttribute(model, bddRoute));

    return builder;
  }

  /**
   * Given a satisfying assignment to the constraints from symbolic route analysis, produce a
   * concrete environment that is consistent with the assignment.
   *
   * @param model the satisfying assignment
   * @param configAPs an object that provides information about the community atomic predicates
   * @return a environment that is consistent with the given model
   */
  public static RouteMapEnvironment satAssignmentToEnvironment(
      BDD model, ConfigAtomicPredicates configAPs) {

    BDDRoute r = new BDDRoute(model.getFactory(), configAPs);

    List<String> successfulTracks = allSatisfyingItems(configAPs.getTracks(), r.getTracks(), model);

    // get the optional (and hence possibly null) source VRF
    String sourceVrf = optionalSatisfyingItem(configAPs.getSourceVrfs(), r.getSourceVrfs(), model);

    return new RouteMapEnvironment(successfulTracks::contains, sourceVrf);
  }

  // Return a list of all items whose corresponding BDD is consistent with the given variable
  // assignment.
  private static List<String> allSatisfyingItems(List<String> items, BDD[] itemBDDs, BDD model) {
    return IntStream.range(0, itemBDDs.length)
        .filter(i -> mustBeTrueInModel(itemBDDs[i], model))
        .mapToObj(items::get)
        .collect(ImmutableList.toImmutableList());
  }

  /**
   * Returns the (possibly null, if none) item that is consistent with the given variable
   * assignment.
   *
   * @param items the list of items
   * @param itemsBDD the symbolic representation of the items
   * @param model the variable assignment
   * @return the unique item consistent with the model, or null if there is none
   */
  private static <T> @Nullable T optionalSatisfyingItem(
      List<T> items, BDDDomain<Integer> itemsBDD, BDD model) {
    // we subtract 1 to get the list index, since the 0th value in the BDDDomain is used to
    // represent that there is no value chosen
    int index = itemsBDD.satAssignmentToValue(model) - 1;
    if (index == -1) {
      return null;
    } else {
      return items.get(index);
    }
  }

  /**
   * Tries to "add" constraint c to constraints; if the result is not inconsistent returns it,
   * otherwise returns constraints.
   *
   * @param c a constraint expressed as a BDD
   * @param constraints the set of constraints to augment
   * @return the augmented constraints if consistent, otherwise the original constraints.
   */
  private static BDD tryAddingConstraint(BDD c, BDD constraints) {
    BDD augmentedConstraints = constraints.and(c);
    if (!augmentedConstraints.isZero()) {
      return augmentedConstraints;
    } else {
      return constraints;
    }
  }

  // Produces a model of the given constraints, which represents a concrete route announcement
  // that is consistent with the constraints.  The model uses certain defaults for certain fields,
  // like the prefix, if they are consistent with the constraints. Note that the model is a partial
  // assignment -- variables that don't matter are not assigned a truth value.
  public static BDD constraintsToModel(BDD constraints, ConfigAtomicPredicates configAPs) {
    BDDRoute route = new BDDRoute(constraints.getFactory(), configAPs);
    // set the protocol field to BGP if it is consistent with the constraints
    BDD isBGP = route.getProtocolHistory().value(RoutingProtocol.BGP);
    BDD defaultLP = route.getLocalPref().value(Bgpv4Route.DEFAULT_LOCAL_PREFERENCE);

    // Set the prefixes to one of the well-known ones
    BDD googlePrefix =
        route
            .getPrefix()
            .value(Ip.parse("8.8.8.0").asLong())
            .and(route.getPrefixLength().value(24));
    BDD amazonPrefix =
        route
            .getPrefix()
            .value(Ip.parse("52.0.0.0").asLong())
            .and(route.getPrefixLength().value(10));
    BDD rfc1918 =
        route
            .getPrefix()
            .value(Ip.parse("10.0.0.0").asLong())
            .and(route.getPrefixLength().value(8));
    BDD prefixes = googlePrefix.or(amazonPrefix).or(rfc1918);
    // Alternatively, if the above fails set the prefix to something >= 10.0.0.0 and the length to
    // something >= 16.
    BDD lessPreferredPrefixes =
        route.getPrefix().geq(167772160).and(route.getPrefixLength().geq(16));
    BDD augmentedConstraints = tryAddingConstraint(isBGP, constraints);
    augmentedConstraints = tryAddingConstraint(defaultLP, augmentedConstraints);
    augmentedConstraints = tryAddingConstraint(prefixes, augmentedConstraints);
    augmentedConstraints = tryAddingConstraint(lessPreferredPrefixes, augmentedConstraints);
    return augmentedConstraints.satOne();
  }
}
