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
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import net.sf.javabdd.BDD;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.OriginMechanism;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.ReceivedFromSelf;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.bgp.community.Community;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.batfish.datamodel.bgp.community.LargeCommunity;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.questions.BgpRoute;
import org.batfish.datamodel.route.nh.NextHop;
import org.batfish.datamodel.route.nh.NextHopBgpPeerAddress;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.route.nh.NextHopInterface;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.batfish.datamodel.route.nh.NextHopSelf;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.minesweeper.CommunityVar;
import org.batfish.minesweeper.ConfigAtomicPredicates;
import org.batfish.minesweeper.utils.Tuple;
import org.batfish.question.testroutepolicies.Result;
import org.batfish.question.testroutepolicies.TestRoutePoliciesAnswerer;
import org.parboiled.common.Preconditions;

public class ModelGeneration {
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
   * Given a single satisfying assignment to the constraints from symbolic route analysis, produce a
   * set of communities for a given symbolic route that is consistent with the assignment.
   *
   * @param fullModel a full model of the symbolic route constraints
   * @param r the symbolic route
   * @param configAPs an object that provides information about the community atomic predicates
   * @return a set of communities
   */
  static Set<Community> satAssignmentToCommunities(
      BDD fullModel, BDDRoute r, ConfigAtomicPredicates configAPs) {

    BDD[] aps = r.getCommunityAtomicPredicates();
    Map<Integer, Automaton> apAutomata =
        configAPs.getStandardCommunityAtomicPredicates().getAtomicPredicateAutomata();

    ImmutableSet.Builder<Community> comms = new ImmutableSet.Builder<>();

    int numStandardAPs = configAPs.getStandardCommunityAtomicPredicates().getNumAtomicPredicates();
    // handle standard community literals and regexes
    for (int i = 0; i < numStandardAPs; i++) {
      if (aps[i].andSat(fullModel)) {
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
      if (aps[entry.getKey()].andSat(fullModel)) {
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
   * @param fullModel a full model of the symbolic route constraints
   * @param r the symbolic route
   * @param configAPs an object provides information about the AS-path regex atomic predicates
   * @return an AsPath
   */
  static AsPath satAssignmentToAsPath(BDD fullModel, BDDRoute r, ConfigAtomicPredicates configAPs) {

    BDD[] aps = r.getAsPathRegexAtomicPredicates();
    Map<Integer, Automaton> apAutomata =
        configAPs.getAsPathRegexAtomicPredicates().getAtomicPredicateAutomata();

    // find all atomic predicates that are required to be true in the given model
    List<Integer> trueAPs =
        IntStream.range(0, configAPs.getAsPathRegexAtomicPredicates().getNumAtomicPredicates())
            .filter(i -> aps[i].andSat(fullModel))
            .boxed()
            .collect(Collectors.toList());

    // since atomic predicates are disjoint, at most one of them should be true in the model
    checkState(
        trueAPs.size() == 1,
        "Error in symbolic AS-path analysis: exactly one atomic predicate should be true");

    Automaton asPathRegexAutomaton = apAutomata.get(trueAPs.get(0));
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
   * @param fullModel a full model of the symbolic route constraints
   * @param r the symbolic route
   * @param configAPs an object provides information about the AS-path regex atomic predicates
   * @return a next-hop
   */
  static NextHop satAssignmentToNextHop(
      BDD fullModel, BDDRoute r, ConfigAtomicPredicates configAPs) {
    // Note: this is the only part of model generation that relies on the fact that we are solving
    // for the input route.  If we also want to produce the output route from the model, given the
    // BDDRoute that results from symbolic analysis, we need to consider the direction of the route
    // map (in or out) as well as the values of the other next-hop-related in the BDDRoute, in order
    // to do it properly.

    Ip ip = Ip.create(r.getNextHop().satAssignmentToLong(fullModel));
    // if we matched on a next-hop interface then include the interface name in the produced
    // next-hop
    List<String> nextHopInterfaces =
        allSatisfyingItems(configAPs.getNextHopInterfaces(), r.getNextHopInterfaces(), fullModel);
    checkState(
        nextHopInterfaces.size() <= 1,
        "Error in symbolic route analysis: at most one source VRF can be in the environment");
    if (nextHopInterfaces.isEmpty()) {
      return NextHopIp.of(ip);
    } else {
      return NextHopInterface.of(nextHopInterfaces.get(0), ip);
    }
  }

  /**
   * Produce the concrete input route that is represented by the given assignment of values to BDD
   * variables from the symbolic route analysis.
   *
   * @param fullModel the satisfying assignment
   * @param configAPs an object that provides information about the atomic predicates in the model
   * @return a route
   */
  public static Bgpv4Route satAssignmentToInputRoute(
      BDD fullModel, ConfigAtomicPredicates configAPs) {
    return satAssignmentToRoute(
        fullModel, new BDDRoute(fullModel.getFactory(), configAPs), configAPs);
  }

  /**
   * Check that the results of symbolic analysis are consistent with a given concrete input-output
   * result (which would typically come from running Batfish's route-map simulation question {@link
   * org.batfish.question.testroutepolicies.TestRoutePoliciesQuestion}). Specifically, check that
   * the symbolic analysis agrees with the given result, on the action (permit or deny) that the
   * route map will take on the given input route announcement as well as on the output route that
   * will result (in the case that the route is permitted).
   *
   * <p>The method raises an exception if a discrepancy is found. This indicates that either the
   * symbolic route analysis or the concrete route simulation (or both) has a modeling error.
   *
   * @param fullModel a satisfying assignment to the constraints from symbolic route analysis along
   *     some path
   * @param bddRoute a symbolic representation of the output route produced along that path
   * @param configAPs the {@link ConfigAtomicPredicates} object, which enables proper interpretation
   *     of atomic predicates in the bddRoute
   * @param action the action that the symbolic analysis determined is taken on that path
   * @param direction whether the route map is used an import or export policy
   * @param expectedResult the expected input-output behavior
   */
  public static void validateModel(
      BDD fullModel,
      BDDRoute bddRoute,
      ConfigAtomicPredicates configAPs,
      LineAction action,
      Environment.Direction direction,
      Result<BgpRoute> expectedResult) {
    String message =
        "The symbolic route analysis and concrete route simulation disagree on the behavior of a"
            + " route map";
    Preconditions.checkState(expectedResult.getAction().equals(action), message);
    if (action == PERMIT) {
      BgpRoute outputRouteFromModel =
          satAssignmentToOutputRoute(fullModel, bddRoute, configAPs, direction);
      Preconditions.checkState(
          expectedResult.getOutputRoute().equals(outputRouteFromModel), message);
    }
  }

  /**
   * Produce the concrete output route that is represented by the given assignment of values to BDD
   * variables as well as resulting {@link BDDRoute} from the symbolic route analysis.
   *
   * <p>Note: This method assumes that any AS-prepending that happens along the given path has
   * already been accounted for through an update to the AS-path atomic predicates appropriately
   * (see {@link org.batfish.minesweeper.AsPathRegexAtomicPredicates#prependAPs(List)}).
   *
   * @param fullModel the satisfying assignment
   * @param bddRoute symbolic representation of the output route
   * @param configAPs an object that provides information about the atomic predicates in the
   *     fullModel and bddRoute
   * @param direction whether the route map is used an import or export policy
   * @return a route
   */
  private static BgpRoute satAssignmentToOutputRoute(
      BDD fullModel,
      BDDRoute bddRoute,
      ConfigAtomicPredicates configAPs,
      Environment.Direction direction) {
    BgpRoute.Builder builder =
        TestRoutePoliciesAnswerer.toQuestionBgpRoute(
            satAssignmentToRoute(fullModel, bddRoute, configAPs))
            .toBuilder();
    if (direction == Environment.Direction.OUT && !bddRoute.getNextHopSet()) {
      // in the OUT direction the next hop is ignored unless explicitly set
      builder.setNextHop(NextHopDiscard.instance());
    } else {
      switch (bddRoute.getNextHopType()) {
        case BGP_PEER_ADDRESS:
          builder.setNextHop(NextHopBgpPeerAddress.instance());
          break;
        case DISCARDED:
          builder.setNextHop(NextHopDiscard.instance());
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
   * Produce the concrete route that is represented by the given assignment of values to BDD
   * variables and {@link BDDRoute} from the symbolic route analysis.
   *
   * @param fullModel the satisfying assignment
   * @param bddRoute symbolic representation of the desired route
   * @param configAPs an object that provides information about the atomic predicates in the
   *     fullModel and bddRoute
   * @return a route
   */
  private static Bgpv4Route satAssignmentToRoute(
      BDD fullModel, BDDRoute bddRoute, ConfigAtomicPredicates configAPs) {

    Bgpv4Route.Builder builder =
        Bgpv4Route.builder()
            .setOriginatorIp(Ip.ZERO) /* dummy value until supported */
            .setReceivedFrom(ReceivedFromSelf.instance()) /* dummy value until supported */
            .setOriginMechanism(OriginMechanism.LEARNED) /* dummy value until supported */;

    Ip ip = Ip.create(bddRoute.getPrefix().satAssignmentToLong(fullModel));
    long len = bddRoute.getPrefixLength().satAssignmentToLong(fullModel);
    builder.setNetwork(Prefix.create(ip, (int) len));

    builder.setLocalPreference(bddRoute.getLocalPref().satAssignmentToLong(fullModel));
    builder.setAdmin(bddRoute.getAdminDist().satAssignmentToInt(fullModel));
    builder.setMetric(bddRoute.getMed().satAssignmentToLong(fullModel));
    builder.setTag(bddRoute.getTag().satAssignmentToLong(fullModel));
    builder.setWeight(bddRoute.getWeight().satAssignmentToInt(fullModel));
    builder.setOriginType(bddRoute.getOriginType().satAssignmentToValue(fullModel));
    builder.setProtocol(bddRoute.getProtocolHistory().satAssignmentToValue(fullModel));

    // if the cluster list length is N, create the cluster list 0,...,N-1
    long clusterListLength = bddRoute.getClusterListLength().satAssignmentToLong(fullModel);
    builder.setClusterList(
        LongStream.range(0, clusterListLength).boxed().collect(ImmutableSet.toImmutableSet()));

    Set<Community> communities = satAssignmentToCommunities(fullModel, bddRoute, configAPs);
    builder.setCommunities(communities);

    AsPath asPath = satAssignmentToAsPath(fullModel, bddRoute, configAPs);
    builder.setAsPath(asPath);

    NextHop nextHop = satAssignmentToNextHop(fullModel, bddRoute, configAPs);
    builder.setNextHop(nextHop);

    return builder.build();
  }

  /**
   * Given a satisfying assignment to the constraints from symbolic route analysis, produce a
   * concrete environment (for now, a predicate on tracks as well as an optional source VRF) that is
   * consistent with the assignment.
   *
   * @param fullModel the satisfying assignment
   * @param configAPs an object that provides information about the community atomic predicates
   * @return a pair of a predicate on tracks and an optional source VRF
   */
  public static Tuple<Predicate<String>, String> satAssignmentToEnvironment(
      BDD fullModel, ConfigAtomicPredicates configAPs) {

    BDDRoute r = new BDDRoute(fullModel.getFactory(), configAPs);

    List<String> successfulTracks =
        allSatisfyingItems(configAPs.getTracks(), r.getTracks(), fullModel);

    // see if the route should have a source VRF, and if so then add it
    List<String> sourceVrfs =
        allSatisfyingItems(configAPs.getSourceVrfs(), r.getSourceVrfs(), fullModel);
    checkState(
        sourceVrfs.size() <= 1,
        "Error in symbolic route analysis: at most one source VRF can be in the environment");

    return new Tuple<>(successfulTracks::contains, sourceVrfs.isEmpty() ? null : sourceVrfs.get(0));
  }

  // Return a list of all items whose corresponding BDD is consistent with the given variable
  // assignment.
  private static List<String> allSatisfyingItems(
      List<String> items, BDD[] itemBDDs, BDD fullModel) {
    return IntStream.range(0, itemBDDs.length)
        .filter(i -> itemBDDs[i].andSat(fullModel))
        .mapToObj(items::get)
        .collect(ImmutableList.toImmutableList());
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

  // Produces a full model of the given constraints, which represents a concrete route announcement
  // that is consistent with the constraints.  The protocol defaults to BGP if it is consistent with
  // the constraints.  The same approach could be used to provide default values for other fields in
  // the future.
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
    return augmentedConstraints.fullSatOne();
  }
}
