package org.batfish.minesweeper.bdd;

import static com.google.common.base.Preconditions.checkState;

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
import org.batfish.datamodel.OriginMechanism;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.ReceivedFromSelf;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.bgp.community.Community;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.batfish.datamodel.bgp.community.LargeCommunity;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.batfish.minesweeper.CommunityVar;
import org.batfish.minesweeper.ConfigAtomicPredicates;
import org.batfish.minesweeper.SymbolicAsPathRegex;
import org.batfish.minesweeper.utils.Tuple;

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
        trueAPs.size() <= 1,
        "Error in symbolic AS-path analysis: at most one atomic predicate should be true");

    // create an automaton for the language of AS-paths that are true in the model
    Automaton asPathRegexAutomaton = SymbolicAsPathRegex.ALL_AS_PATHS.toAutomaton();
    for (Integer i : trueAPs) {
      asPathRegexAutomaton = asPathRegexAutomaton.intersection(apAutomata.get(i));
    }

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
   * Given a satisfying assignment to the constraints from symbolic route analysis, produce a
   * concrete input route that is consistent with the assignment.
   *
   * @param fullModel the satisfying assignment
   * @param configAPs an object that provides information about the community atomic predicates
   * @return a route
   */
  public static Bgpv4Route satAssignmentToInputRoute(
      BDD fullModel, ConfigAtomicPredicates configAPs) {

    Bgpv4Route.Builder builder =
        Bgpv4Route.builder()
            .setOriginatorIp(Ip.ZERO) /* dummy value until supported */
            .setReceivedFrom(ReceivedFromSelf.instance()) /* dummy value until supported */
            .setOriginMechanism(OriginMechanism.LEARNED) /* dummy value until supported */;

    BDDRoute r = new BDDRoute(fullModel.getFactory(), configAPs);

    Ip ip = Ip.create(r.getPrefix().satAssignmentToLong(fullModel));
    long len = r.getPrefixLength().satAssignmentToLong(fullModel);
    builder.setNetwork(Prefix.create(ip, (int) len));

    builder.setLocalPreference(r.getLocalPref().satAssignmentToLong(fullModel));
    builder.setAdmin(r.getAdminDist().satAssignmentToInt(fullModel));
    builder.setMetric(r.getMed().satAssignmentToLong(fullModel));
    builder.setTag(r.getTag().satAssignmentToLong(fullModel));
    builder.setOriginType(r.getOriginType().satAssignmentToValue(fullModel));
    builder.setProtocol(r.getProtocolHistory().satAssignmentToValue(fullModel));

    // if the cluster list length is N, create the cluster list 0,...,N-1
    long clusterListLength = r.getClusterListLength().satAssignmentToLong(fullModel);
    builder.setClusterList(
        LongStream.range(0, clusterListLength).boxed().collect(ImmutableSet.toImmutableSet()));

    Set<Community> communities = satAssignmentToCommunities(fullModel, r, configAPs);
    builder.setCommunities(communities);

    AsPath asPath = satAssignmentToAsPath(fullModel, r, configAPs);
    builder.setAsPath(asPath);

    // Note: this is the only part of the method that relies on the fact that we are solving for the
    // input route.  If we also want to produce the output route from the model, given the BDDRoute
    // that results from symbolic analysis, we need to consider the _direction as well as the values
    // of the two next-hop flags in the BDDRoute, in order to do it properly
    builder.setNextHop(NextHopIp.of(Ip.create(r.getNextHop().satAssignmentToLong(fullModel))));

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
