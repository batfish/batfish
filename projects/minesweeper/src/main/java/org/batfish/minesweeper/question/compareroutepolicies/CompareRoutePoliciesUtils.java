package org.batfish.minesweeper.question.compareroutepolicies;

import static org.batfish.minesweeper.bdd.BDDRouteDiff.computeDifferences;
import static org.batfish.minesweeper.bdd.ModelGeneration.constraintsToModel;
import static org.batfish.minesweeper.bdd.ModelGeneration.satAssignmentToEnvironment;
import static org.batfish.minesweeper.bdd.ModelGeneration.satAssignmentToInputRoute;
import static org.batfish.minesweeper.question.searchroutepolicies.SearchRoutePoliciesAnswerer.simulatePolicy;
import static org.batfish.question.testroutepolicies.Result.RouteAttributeType.ADMINISTRATIVE_DISTANCE;
import static org.batfish.question.testroutepolicies.Result.RouteAttributeType.AS_PATH;
import static org.batfish.question.testroutepolicies.Result.RouteAttributeType.CLUSTER_LIST;
import static org.batfish.question.testroutepolicies.Result.RouteAttributeType.COMMUNITIES;
import static org.batfish.question.testroutepolicies.Result.RouteAttributeType.LOCAL_PREFERENCE;
import static org.batfish.question.testroutepolicies.Result.RouteAttributeType.METRIC;
import static org.batfish.question.testroutepolicies.Result.RouteAttributeType.NETWORK;
import static org.batfish.question.testroutepolicies.Result.RouteAttributeType.NEXT_HOP;
import static org.batfish.question.testroutepolicies.Result.RouteAttributeType.ORIGIN_TYPE;
import static org.batfish.question.testroutepolicies.Result.RouteAttributeType.PROTOCOL;
import static org.batfish.question.testroutepolicies.Result.RouteAttributeType.TAG;
import static org.batfish.question.testroutepolicies.Result.RouteAttributeType.TUNNEL_ENCAPSULATION_ATTRIBUTE;
import static org.batfish.question.testroutepolicies.Result.RouteAttributeType.WEIGHT;
import static org.batfish.specifier.NameRegexRoutingPolicySpecifier.ALL_ROUTING_POLICIES;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.common.BatfishException;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.questions.BgpRoute;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.minesweeper.CommunityVar;
import org.batfish.minesweeper.ConfigAtomicPredicates;
import org.batfish.minesweeper.bdd.BDDRoute;
import org.batfish.minesweeper.bdd.BDDRouteDiff;
import org.batfish.minesweeper.bdd.TransferBDD;
import org.batfish.minesweeper.bdd.TransferReturn;
import org.batfish.minesweeper.utils.Tuple;
import org.batfish.question.testroutepolicies.Result;
import org.batfish.specifier.AllNodesNodeSpecifier;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.RoutingPolicySpecifier;
import org.batfish.specifier.SpecifierContext;
import org.batfish.specifier.SpecifierFactories;

public final class CompareRoutePoliciesUtils {

  private final @Nonnull Environment.Direction _direction;

  private final @Nonnull String _policySpecifierString;
  private final @Nullable String _referencePolicySpecifierString;
  private final @Nonnull RoutingPolicySpecifier _policySpecifier;
  private final @Nullable RoutingPolicySpecifier _referencePolicySpecifier;

  private final @Nonnull NodeSpecifier _nodeSpecifier;

  private final @Nonnull Set<String> _communityRegexes;
  private final @Nonnull Set<String> _asPathRegexes;

  private final @Nonnull IBatfish _batfish;

  private static Comparator<RoutingPolicy> RP_BY_NAME =
      Comparator.comparing(RoutingPolicy::getName);

  public CompareRoutePoliciesUtils(
      @Nonnull Environment.Direction direction,
      @Nonnull String policySpecifierString,
      @Nullable String referencePolicySpecifierString,
      String nodes,
      IBatfish batfish) {
    _nodeSpecifier =
        SpecifierFactories.getNodeSpecifierOrDefault(nodes, AllNodesNodeSpecifier.INSTANCE);
    _direction = direction;

    _policySpecifierString = policySpecifierString;
    _policySpecifier =
        SpecifierFactories.getRoutingPolicySpecifierOrDefault(
            _policySpecifierString, ALL_ROUTING_POLICIES);

    // If the referencePolicySpecifier is null then we compare using the route-maps in
    // policySpecifier
    _referencePolicySpecifierString = referencePolicySpecifierString;
    _referencePolicySpecifier =
        SpecifierFactories.getRoutingPolicySpecifierOrDefault(
            _referencePolicySpecifierString, _policySpecifier);

    // in the future, it may improve performance to combine all input community regexes
    // into a single regex representing their disjunction, and similarly for all output
    // community regexes, in order to minimize the number of atomic predicates that are
    // created and tracked by the analysis
    _communityRegexes = ImmutableSet.of();
    _asPathRegexes = ImmutableSet.of();
    this._batfish = batfish;
  }

  /**
   * Compares the policies in policySpecifier with the policies in proposedPolicySpecifier (all of
   * them, their names don't have to match up). If, however, the proposedPolicySpecifier is empty it
   * will do a 1-1 comparison with the policies found in policySpecifier. Note, this only compares
   * across the same hostnames between the two snapshots, i.e., it will compare route-maps in r1
   * with route-maps in r1 of the new snapshot.
   *
   * <p>This method returns a structured representation suitable for use by other questions, rather
   * than a TableAnswer. In particular, it returns a stream of "differences", represented as a tuple
   * where the first element corresponds to the behavior in the current snapshot, and the second
   * element corresponds to the behavior in the reference snapshot. The two elements contain the
   * corresponding node and routing policies, as well, as the input route that triggers the
   * difference (should be the same across both tuple components) and the action+output route+trace
   * in each case (will be different).
   */
  public Stream<Tuple<Result<BgpRoute, BgpRoute>, Result<BgpRoute, BgpRoute>>> getDifferencesStream(
      NetworkSnapshot snapshot, NetworkSnapshot reference) {

    SpecifierContext currentContext = _batfish.specifierContext(snapshot);
    SpecifierContext referenceContext = _batfish.specifierContext(reference);
    Set<String> currentNodes = _nodeSpecifier.resolve(currentContext);
    Set<String> referenceNodes = _nodeSpecifier.resolve(referenceContext);
    // Only compare nodes that are in both snapshots.
    Stream<String> nodes = currentNodes.stream().filter(referenceNodes::contains);

    // Using stream.sorted() to ensure consistent order.
    return nodes
        .sorted()
        .flatMap(
            node -> {
              // If the referencePolicySpecifier is null then use the policies from
              // policySpecifier and do a 1-1 comparison based on policy name equality.
              if (_referencePolicySpecifier == null) {
                return comparePoliciesForNode(
                    node,
                    _policySpecifier.resolve(node, currentContext).stream().sorted(RP_BY_NAME),
                    _policySpecifier.resolve(node, referenceContext).stream().sorted(RP_BY_NAME),
                    false,
                    snapshot,
                    reference);
              } else {
                // Otherwise cross-compare all policies in each set (policySpecifier and
                // referencePolicySpecifier)
                return comparePoliciesForNode(
                    node,
                    _policySpecifier.resolve(node, currentContext).stream().sorted(RP_BY_NAME),
                    _referencePolicySpecifier.resolve(node, referenceContext).stream()
                        .sorted(RP_BY_NAME),
                    true,
                    snapshot,
                    reference);
              }
            });
  }

  /**
   * Search all of the route policies of a particular node for behaviors of interest.
   *
   * @param node the node - for now assuming a single config, might lift that assumption later.
   * @param policies all route policies in the given node for the new snapshot
   * @param referencePolicies all route policies in the given node for the reference snapshot.
   * @param crossPolicies if true then policies and referencePolicies are all compared with each
   *     other. Otherwise we use a one-to-one mapping where names must match in order to compare.
   * @param snapshot the snapshot of the proposed change
   * @param reference the reference snapshot
   * @return all results from analyzing those route policies
   */
  private Stream<Tuple<Result<BgpRoute, BgpRoute>, Result<BgpRoute, BgpRoute>>>
      comparePoliciesForNode(
          String node,
          Stream<RoutingPolicy> policies,
          Stream<RoutingPolicy> referencePolicies,
          boolean crossPolicies,
          NetworkSnapshot snapshot,
          NetworkSnapshot reference) {
    List<RoutingPolicy> referencePoliciesList = referencePolicies.collect(Collectors.toList());
    List<RoutingPolicy> currentPoliciesList = policies.collect(Collectors.toList());

    if (referencePoliciesList.isEmpty()) {
      throw new IllegalArgumentException(
          String.format(
              "Could not find policy matching %s in reference snapshot",
              _referencePolicySpecifierString));
    }
    if (currentPoliciesList.isEmpty()) {
      throw new IllegalArgumentException(
          String.format(
              "Could not find policy matching %s in current snapshot", _policySpecifierString));
    }

    if (!crossPolicies) {
      // In this case we are comparing all route-maps with the same name.
      Set<String> referencePoliciesNames =
          referencePoliciesList.stream().map(RoutingPolicy::getName).collect(Collectors.toSet());
      Set<String> policiesNames =
          currentPoliciesList.stream().map(RoutingPolicy::getName).collect(Collectors.toSet());
      Set<String> intersection =
          referencePoliciesNames.stream()
              .filter(policiesNames::contains)
              .collect(Collectors.toSet());
      if (intersection.isEmpty()) {
        throw new IllegalArgumentException(
            String.format(
                "No common policies described by %s in %s", _policySpecifierString, node));
      }
      // Filter down the lists such that they include policies with the same name only
      referencePoliciesList.removeIf(p -> !intersection.contains(p.getName()));
      currentPoliciesList.removeIf(p -> !intersection.contains(p.getName()));
    }

    Configuration snapshotConfig = _batfish.loadConfigurations(snapshot).get(node);
    Configuration refConfig = _batfish.loadConfigurations(reference).get(node);

    ConfigAtomicPredicates configAPs =
        new ConfigAtomicPredicates(
            ImmutableList.of(
                new SimpleImmutableEntry<>(snapshotConfig, currentPoliciesList),
                new SimpleImmutableEntry<>(refConfig, referencePoliciesList)),
            _communityRegexes.stream()
                .map(CommunityVar::from)
                .collect(ImmutableSet.toImmutableSet()),
            _asPathRegexes);

    if (crossPolicies) {
      // In this case we cross-compare all routing policies in the two sets regardless of their
      // names.
      return referencePoliciesList.stream()
          .flatMap(
              referencePolicy ->
                  currentPoliciesList.stream()
                      .flatMap(
                          currentPolicy ->
                              comparePolicies(referencePolicy, currentPolicy, configAPs)));
    } else {
      // In this case we only compare policies with the same name.
      // Create a stream of policy tuples (referencePolicy, currentPolicy) for policies with the
      // same name.
      currentPoliciesList.sort(RP_BY_NAME);
      referencePoliciesList.sort(RP_BY_NAME);

      // Since the two lists have been filtered to include only elements in their intersection they
      // should have the same
      // length.
      assert (currentPoliciesList.size() == referencePoliciesList.size());

      // Since they have been sorted by name the policies at each index should have the same name.
      return IntStream.range(0, currentPoliciesList.size())
          .mapToObj(
              i -> {
                assert (referencePoliciesList
                    .get(i)
                    .getName()
                    .equals(currentPoliciesList.get(i).getName()));
                return new Tuple<>(referencePoliciesList.get(i), currentPoliciesList.get(i));
              })
          .flatMap(
              policyTuple ->
                  comparePolicies(policyTuple.getFirst(), policyTuple.getSecond(), configAPs));
    }
  }

  /**
   * @param constraints Logical constraints that describe a set of routes.
   * @param configAPs the atomic predicates used for communities/as-paths.
   * @return An input route, a predicate on tracks, and a (possibly null) source VRF that conform to
   *     the given constraints.
   */
  private static @Nonnull Tuple<Bgpv4Route, Tuple<Predicate<String>, String>> constraintsToInputs(
      BDD constraints, ConfigAtomicPredicates configAPs) {
    assert (!constraints.isZero());
    BDD model = constraintsToModel(constraints, configAPs);
    return new Tuple<>(
        satAssignmentToInputRoute(model, configAPs), satAssignmentToEnvironment(model, configAPs));
  }

  /**
   * @param tbdd the transfer function to symbolically evaluate
   * @return a list of paths generated by symbolic execution
   */
  private List<TransferReturn> computePaths(TransferBDD tbdd, RoutingPolicy policy) {
    try {
      return tbdd.computePaths(policy);
    } catch (Exception e) {
      throw new BatfishException(
          "Unexpected error analyzing policy "
              + policy.getName()
              + " in node "
              + policy.getOwner().getHostname(),
          e);
    }
  }

  /**
   * @param diffs A list of differences found between two output routes.
   * @param r1 the first of the two output routes that were compared
   * @param r2 the second of the two output routes that were compared
   * @param inputConstraints the input constraints within which we want to identify differences
   * @return A BDD that represents at least one of the differences in the given diffs list. If there
   *     are no differences that are compatible with the input constraints then the method returns
   *     ZERO.
   */
  private static BDD computeOutputConstraints(
      List<BDDRouteDiff.DifferenceType> diffs, BDDRoute r1, BDDRoute r2, BDD inputConstraints) {
    for (BDDRouteDiff.DifferenceType d : diffs) {
      // If the diff in this attribute is compatible with the input constraints then we are done
      BDD differences = allDifferencesForType(d, r1, r2);
      if (inputConstraints.andSat(differences)) {
        return differences;
      }
      differences.free();
    }
    // none of the differences are compatible with the input constraints
    return r1.getFactory().zero();
  }

  /**
   * Return all differences in the two routes for the given attribute. See {@link
   * #computeOutputConstraints(List, BDDRoute, BDDRoute, BDD)}.
   */
  private static BDD allDifferencesForType(
      BDDRouteDiff.DifferenceType d, BDDRoute r1, BDDRoute r2) {
    switch (d) {
      case LOCAL_PREF:
        return r1.getLocalPref().allDifferences(r2.getLocalPref());
      case COMMUNITIES:
        BDD[] communityAtomicPredicates = r1.getCommunityAtomicPredicates();
        BDD[] otherCommunityAtomicPredicates = r2.getCommunityAtomicPredicates();
        return r1.getFactory()
            .orAllAndFree(
                IntStream.range(0, communityAtomicPredicates.length)
                    .mapToObj(
                        i -> communityAtomicPredicates[i].xor(otherCommunityAtomicPredicates[i]))
                    .collect(ImmutableList.toImmutableList()));
      case MED:
        return r1.getMed().allDifferences(r2.getMed());
      case NEXTHOP:
        return r1.getNextHop().allDifferences(r2.getNextHop());
      case TAG:
        return r1.getTag().allDifferences(r2.getTag());
      case ADMIN_DIST:
        return r1.getAdminDist().allDifferences(r2.getAdminDist());
      case TUNNEL_ENCAPSULATION_ATTRIBUTE:
        return r1.getTunnelEncapsulationAttribute()
            .allDifferences(r2.getTunnelEncapsulationAttribute());
      case WEIGHT:
        return r1.getWeight().allDifferences(r2.getWeight());
      case AS_PATH:
      case NEXTHOP_TYPE:
      case NEXTHOP_SET:
      case UNSUPPORTED:
        // these kinds of differences are independent of the input route, so we don't need
        // additional constraints to expose them
        return r1.getFactory().one();
      default:
        throw new UnsupportedOperationException(d.name());
    }
  }

  /**
   * Check that the results of symbolic analysis are consistent with the given concrete result from
   * route simulation.
   *
   * @param fullModel a satisfying assignment to the constraints from symbolic route analysis along
   *     a given path
   * @param configAPs the {@link ConfigAtomicPredicates} object, which enables proper interpretation
   *     of atomic predicates
   * @param path the symbolic representation of that path
   * @param result the expected input-output behavior
   * @return a boolean indicating whether the check succeeded
   */
  private static boolean validateModel(
      BDD fullModel,
      ConfigAtomicPredicates configAPs,
      TransferReturn path,
      Result<BgpRoute, BgpRoute> result,
      Environment.Direction direction) {
    // update the atomic predicates to include any prepended ASes on this path
    ConfigAtomicPredicates configAPsCopy = new ConfigAtomicPredicates(configAPs);
    org.batfish.minesweeper.AsPathRegexAtomicPredicates aps =
        configAPsCopy.getAsPathRegexAtomicPredicates();
    aps.prependAPs(path.getOutputRoute().getPrependedASes());

    return org.batfish.minesweeper.bdd.ModelGeneration.validateModel(
        fullModel,
        path.getOutputRoute(),
        configAPsCopy,
        path.getAccepted() ? LineAction.PERMIT : LineAction.DENY,
        direction,
        result);
  }

  /**
   * Check that the example of a behavioral difference between the two route maps the symbolic
   * analysis finds is consistent with the results from the concrete route simulation.
   *
   * @param constraints representation of the set of input routes that should exhibit a difference
   * @param configAPs the {@link ConfigAtomicPredicates} object, which enables proper interpretation
   *     of atomic predicates
   * @param path the symbolic representation of the path through the original route map
   * @param otherPath the symbolic representation of the path through the other route map
   * @param result the expected behavior of the original route map on an input that should exhibit a
   *     difference
   * @param otherResult the expected behavior of the other route map on the same input
   * @return a boolean indicating whether the check succeeded
   */
  private static boolean validateDifference(
      BDD constraints,
      ConfigAtomicPredicates configAPs,
      TransferReturn path,
      TransferReturn otherPath,
      Result<BgpRoute, BgpRoute> result,
      Result<BgpRoute, BgpRoute> otherResult,
      Environment.Direction direction) {
    BDD fullModel =
        org.batfish.minesweeper.bdd.ModelGeneration.constraintsToModel(constraints, configAPs);
    return validateModel(fullModel, configAPs, path, result, direction)
        && validateModel(fullModel, configAPs, otherPath, otherResult, direction);
  }

  /**
   * Compare two route policies for behavioral differences.
   *
   * @param referencePolicy the routing policy of the reference snapshot
   * @param policy the routing policy of the current snapshot
   * @param configAPs an object providing the atomic predicates for the otherPolicy's owner
   *     configuration
   * @return a set of differences
   */
  private Stream<Tuple<Result<BgpRoute, BgpRoute>, Result<BgpRoute, BgpRoute>>> comparePolicies(
      RoutingPolicy referencePolicy, RoutingPolicy policy, ConfigAtomicPredicates configAPs) {
    TransferBDD tBDD = new TransferBDD(configAPs);

    // Generate well-formedness constraints
    BDD wf = new BDDRoute(tBDD.getFactory(), configAPs).bgpWellFormednessConstraints();

    // The set of paths for the current policy
    List<TransferReturn> paths = computePaths(tBDD, referencePolicy);
    // The set of paths for the proposed policy, also indexed by input routes.
    List<TransferReturn> otherPaths = computePaths(tBDD, policy);
    Map<BDD, TransferReturn> otherPathIndex =
        otherPaths.stream()
            .collect(ImmutableMap.toImmutableMap(t -> t.getInputConstraints(), t -> t));

    // The set of differences if any.
    List<Tuple<Result<BgpRoute, BgpRoute>, Result<BgpRoute, BgpRoute>>> differences =
        new ArrayList<>();

    for (TransferReturn path : paths) {
      BDD inputRoutes = path.getInputConstraints();
      // Optimization: since the inputRoutes for a given List are mutually disjoint, we can
      // avoid linear comparison when the same set is in the other group. This likely applies
      // when the input policies are very similar.
      TransferReturn sameInputs = otherPathIndex.get(inputRoutes);
      List<TransferReturn> iterationPaths =
          (sameInputs != null) ? ImmutableList.of(sameInputs) : otherPaths;

      for (TransferReturn otherPath : iterationPaths) {
        Tuple<Result<BgpRoute, BgpRoute>, Result<BgpRoute, BgpRoute>> difference =
            findConcreteDifference(
                path, otherPath, wf, configAPs, referencePolicy, policy, _direction);
        if (difference != null) {
          differences.add(difference);
        }
      }
    }
    wf.free();
    return differences.stream()
        .sorted(Comparator.comparing(t -> t.getFirst().getInputRoute().getNetwork()));
  }

  public static @Nullable Tuple<Result<BgpRoute, BgpRoute>, Result<BgpRoute, BgpRoute>>
      findConcreteDifference(
          TransferReturn path,
          TransferReturn otherPath,
          BDD wellFormedConstraints,
          ConfigAtomicPredicates configAPs,
          RoutingPolicy policy,
          RoutingPolicy otherPolicy,
          Environment.Direction direction) {
    BDD inputRoutes = path.getInputConstraints();
    BDD inputRoutesOther = otherPath.getInputConstraints();
    // in the case that both paths are accepting, additional constraints will be computed later to
    // ensure that the chosen concrete input route leads to differing output routes in the paths
    BDD outputConstraints = inputRoutes.getFactory().one();

    BDD intersection = inputRoutesOther.and(inputRoutes).andEq(wellFormedConstraints);
    if (intersection.isZero()) {
      // No common input routes to check equivalence.
      intersection.free();
      return null;
    }

    // A flag that is set if we find a behavioral difference between the two paths
    BDD finalConstraints = null;
    // Naive check to see if both policies accepted/rejected the route(s).
    if (path.getAccepted() != otherPath.getAccepted()) {
      finalConstraints = intersection;
    } else {
      // If both policies perform the same action, then check that their outputs match.
      // We only need to compare the outputs if the routes were permitted.
      if (path.getAccepted()) {
        BDDRoute outputRoutes = path.getOutputRoute();
        BDDRoute outputRoutesOther = otherPath.getOutputRoute();
        // Identify all differences in the two routes, ignoring the input constraints
        List<BDDRouteDiff.DifferenceType> diffs =
            computeDifferences(outputRoutes, outputRoutesOther);
        if (!diffs.isEmpty()) {
          // Now try to find a difference that is compatible with the input constraints
          outputConstraints =
              computeOutputConstraints(diffs, outputRoutes, outputRoutesOther, intersection);
          BDD allConstraints = intersection.andEq(outputConstraints);
          if (!allConstraints.isZero()) {
            finalConstraints = allConstraints;
          } else {
            outputConstraints.free();
            allConstraints.free();
          }
        } else {
          intersection.free();
        }
      }
    }

    if (finalConstraints == null) {
      // No difference found.
      return null;
    }

    // we have found a difference, so let's get a concrete example of the difference
    Tuple<Bgpv4Route, Tuple<Predicate<String>, String>> t =
        constraintsToInputs(finalConstraints, configAPs);
    Result<BgpRoute, BgpRoute> refResult =
        simulatePolicy(policy, t.getFirst(), direction, t.getSecond(), path.getOutputRoute());
    Result<BgpRoute, BgpRoute> otherResult =
        simulatePolicy(
            otherPolicy, t.getFirst(), direction, t.getSecond(), otherPath.getOutputRoute());

    // determine which input route attributes are relevant for this difference, based on the
    // variables that appear in the input constraints of the two paths as well as the output
    // constraint generated above. this differs from the variable finalConstraints only in not
    // including the well-formedness constraint, inclusion of which could cause some
    // irrelevant attributes to be considered relevant.
    List<Result.RouteAttributeType> relevantAttributes =
        relevantAttributesFor(
            inputRoutes.and(inputRoutesOther).andEq(outputConstraints), configAPs);
    outputConstraints.free();
    refResult.setRelevantInputAttributes(ImmutableList.copyOf(relevantAttributes));
    otherResult.setRelevantInputAttributes(ImmutableList.copyOf(relevantAttributes));

    // As a sanity check, compare the simulated results above with what the symbolic route analysis
    // predicts will happen.
    assert validateDifference(
        finalConstraints, configAPs, path, otherPath, refResult, otherResult, direction);
    return new Tuple<>(otherResult, refResult);
  }

  /**
   * Determines which route attributes are relevant for the given constraint. If a route attribute
   * is not included in the result, then that attribute is irrelevant in the sense that any value
   * can be chosen for that attribute.
   *
   * @param constraint predicate that represents the input space leading to a particular difference
   * @param configAPs information about atomic predicates
   * @return a list of route attributes that are relevant for this difference
   */
  public static List<Result.RouteAttributeType> relevantAttributesFor(
      BDD constraint, ConfigAtomicPredicates configAPs) {
    ImmutableList.Builder<Result.RouteAttributeType> result = new ImmutableList.Builder<>();

    BDDFactory factory = constraint.getFactory();
    BDDRoute origRoute = new BDDRoute(factory, configAPs);

    BDD support = constraint.support();

    if (support.testsVars(origRoute.getAdminDist().support())) {
      result.add(ADMINISTRATIVE_DISTANCE);
    }
    if (support.testsVars(origRoute.getAsPathRegexAtomicPredicates().support())) {
      result.add(AS_PATH);
    }
    if (support.testsVars(origRoute.getClusterListLength().support())) {
      result.add(CLUSTER_LIST);
    }
    if (support.testsVars(factory.andAll(origRoute.getCommunityAtomicPredicates()))) {
      result.add(COMMUNITIES);
    }
    if (support.testsVars(origRoute.getLocalPref().support())) {
      result.add(LOCAL_PREFERENCE);
    }
    if (support.testsVars(origRoute.getMed().support())) {
      result.add(METRIC);
    }
    if (support.testsVars(
        origRoute.getPrefix().support().and(origRoute.getPrefixLength().support()))) {
      result.add(NETWORK);
    }
    if (support.testsVars(origRoute.getNextHop().support())) {
      result.add(NEXT_HOP);
    }
    if (support.testsVars(origRoute.getOriginType().support())) {
      result.add(ORIGIN_TYPE);
    }
    if (support.testsVars(origRoute.getProtocolHistory().support())) {
      result.add(PROTOCOL);
    }
    if (support.testsVars(origRoute.getTag().support())) {
      result.add(TAG);
    }
    if (support.testsVars(origRoute.getTunnelEncapsulationAttribute().support())) {
      result.add(TUNNEL_ENCAPSULATION_ATTRIBUTE);
    }
    if (support.testsVars(origRoute.getWeight().support())) {
      result.add(WEIGHT);
    }

    support.free();
    return result.build();
  }
}
