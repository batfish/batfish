package org.batfish.minesweeper.question.compareroutepolicies;

import static org.batfish.minesweeper.bdd.BDDRouteDiff.computeDifferences;
import static org.batfish.minesweeper.bdd.ModelGeneration.constraintsToModel;
import static org.batfish.minesweeper.bdd.ModelGeneration.satAssignmentToEnvironment;
import static org.batfish.minesweeper.bdd.ModelGeneration.satAssignmentToInputRoute;
import static org.batfish.specifier.NameRegexRoutingPolicySpecifier.ALL_ROUTING_POLICIES;

import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.JFactory;
import org.batfish.common.BatfishException;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Bgpv4Route;
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
import org.batfish.minesweeper.question.searchroutepolicies.SearchRoutePoliciesAnswerer;
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
    _communityRegexes = ImmutableSet.<String>builder().build();
    _asPathRegexes = ImmutableSet.<String>builder().build();
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
  public Stream<Tuple<Result<BgpRoute>, Result<BgpRoute>>> getDifferencesStream(
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
                    _policySpecifier.resolve(node, currentContext).stream().sorted(),
                    _policySpecifier.resolve(node, referenceContext).stream().sorted(),
                    false,
                    snapshot,
                    reference);
              } else {
                // Otherwise cross-compare all policies in each set (policySpecifier and
                // referencePolicySpecifier)
                return comparePoliciesForNode(
                    node,
                    _policySpecifier.resolve(node, currentContext).stream().sorted(),
                    _referencePolicySpecifier.resolve(node, referenceContext).stream().sorted(),
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
  private Stream<Tuple<Result<BgpRoute>, Result<BgpRoute>>> comparePoliciesForNode(
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

    ConfigAtomicPredicates configAPs =
        new ConfigAtomicPredicates(
            _batfish,
            snapshot,
            reference,
            node,
            _communityRegexes.stream()
                .map(CommunityVar::from)
                .collect(ImmutableSet.toImmutableSet()),
            _asPathRegexes,
            currentPoliciesList,
            referencePoliciesList);

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
      currentPoliciesList.sort(Comparator.comparing(RoutingPolicy::getName));
      referencePoliciesList.sort(Comparator.comparing(RoutingPolicy::getName));

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
  private Tuple<Bgpv4Route, Tuple<Predicate<String>, String>> constraintsToInputs(
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
  private List<TransferReturn> computePaths(TransferBDD tbdd) {
    try {
      return tbdd.computePaths(ImmutableSet.of());
    } catch (Exception e) {
      throw new BatfishException(
          "Unexpected error analyzing policy "
              + tbdd.getPolicy().getName()
              + " in node "
              + tbdd.getPolicy().getOwner().getHostname(),
          e);
    }
  }

  /**
   * @param factory the BDD factory used for this analysis
   * @param diffs A list of differences found between two output routes.
   * @param r1 the first of the two output routes that were compared
   * @param r2 the second of the two output routes that were compared
   * @return A BDD that denotes at least one of the differences in the given diffs list. Only
   *     capturing differences in communities for now; the rest are not necessary because they do
   *     not have additive semantics, like "set community additive". Note that AS-path prepends are
   *     recorded concretely rather than symbolically in {@link BDDRoute}s, so their differences are
   *     also ignored here. I think you might be able to plus on the local-pref value (TODO: check)
   *     so we might have to account for this case too.
   */
  private BDD counterExampleOutputConstraints(
      BDDFactory factory, List<BDDRouteDiff.DifferenceType> diffs, BDDRoute r1, BDDRoute r2) {
    BDD acc = factory.zero();
    for (BDDRouteDiff.DifferenceType d : diffs) {
      switch (d) {
        case AS_PATH:
        case OSPF_METRIC:
        case LOCAL_PREF:
        case MED:
        case NEXTHOP:
        case NEXTHOP_TYPE:
        case NEXTHOP_SET:
        case TAG:
        case ADMIN_DIST:
        case WEIGHT:
        case UNSUPPORTED:
          break;
        case COMMUNITIES:
          BDD[] communityAtomicPredicates = r1.getCommunityAtomicPredicates();
          BDD[] otherCommunityAtomicPredicates = r2.getCommunityAtomicPredicates();
          for (int i = 0; i < communityAtomicPredicates.length; i++) {
            BDD outConstraint = communityAtomicPredicates[i].xor(otherCommunityAtomicPredicates[i]);
            // If there is a scenario where the two outputs differ at this community then ensure
            // this scenario
            // manifests during model generation.
            acc = acc.or(outConstraint);
          }
          break;
        default:
          throw new UnsupportedOperationException(d.name());
      }
    }
    if (!acc.isZero()) {
      // If we have accumulated some constraints from the output routes then return these
      return acc;
    } else {
      // otherwise return true.
      return factory.one();
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
  private boolean validateModel(
      BDD fullModel,
      ConfigAtomicPredicates configAPs,
      TransferReturn path,
      Result<BgpRoute> result) {
    // update the atomic predicates to include any prepended ASes on this path
    ConfigAtomicPredicates configAPsCopy = new ConfigAtomicPredicates(configAPs);
    org.batfish.minesweeper.AsPathRegexAtomicPredicates aps =
        configAPsCopy.getAsPathRegexAtomicPredicates();
    aps.prependAPs(path.getFirst().getPrependedASes());

    return org.batfish.minesweeper.bdd.ModelGeneration.validateModel(
        fullModel,
        path.getFirst(),
        configAPsCopy,
        path.getAccepted() ? LineAction.PERMIT : LineAction.DENY,
        _direction,
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
  private boolean validateDifference(
      BDD constraints,
      ConfigAtomicPredicates configAPs,
      TransferReturn path,
      TransferReturn otherPath,
      Result<BgpRoute> result,
      Result<BgpRoute> otherResult) {
    BDD fullModel =
        org.batfish.minesweeper.bdd.ModelGeneration.constraintsToModel(constraints, configAPs);
    return validateModel(fullModel, configAPs, path, result)
        && validateModel(fullModel, configAPs, otherPath, otherResult);
  }

  /**
   * Compare two route policies for behavioral differences.
   *
   * @param referencePolicy the routing policy of the reference snapshot
   * @param policy the routing policy of the current snapshot
   * @param configAPs an object providing the atomic predicates for the policy's owner configuration
   * @return a set of differences
   */
  private Stream<Tuple<Result<BgpRoute>, Result<BgpRoute>>> comparePolicies(
      RoutingPolicy referencePolicy, RoutingPolicy policy, ConfigAtomicPredicates configAPs) {
    // The set of differences if any.
    List<Tuple<Result<BgpRoute>, Result<BgpRoute>>> differences = new ArrayList<>();

    BDDFactory factory = JFactory.init(100000, 10000);
    TransferBDD tBDD = new TransferBDD(factory, configAPs, referencePolicy);
    TransferBDD otherTBDD = new TransferBDD(factory, configAPs, policy);

    // Generate well-formedness constraints
    BDD wf = new BDDRoute(tBDD.getFactory(), configAPs).bgpWellFormednessConstraints();

    // The set of paths for the current policy
    List<TransferReturn> paths = computePaths(tBDD);
    // The set of paths for the proposed policy
    List<TransferReturn> otherPaths = computePaths(otherTBDD);

    // Potential optimization: if a set of input routes between the two paths is the same then we
    // only need to validate
    // the outputs between this pair; the intersection with all others is going to be empty.
    // This will probably be more efficient when we expect the two route-maps to be (almost)
    // equivalent.
    for (TransferReturn path : paths) {
      for (TransferReturn otherPath : otherPaths) {
        BDD inputRoutes = path.getSecond();
        BDD inputRoutesOther = otherPath.getSecond();
        BDD intersection = inputRoutesOther.and(inputRoutes).and(wf);

        // If the sets of input routes between the two paths intersect, then these paths describe
        // some common input routes and their behavior should match.
        if (!intersection.isZero()) {
          // a flag that is set if we find a behavioral difference between the two paths
          boolean behaviorDiff = false;
          BDD finalConstraints = null;
          // Naive check to see if both policies accepted/rejected the route(s).
          if (path.getAccepted() != otherPath.getAccepted()) {
            behaviorDiff = true;
            finalConstraints = intersection;
          } else {
            // If both policies perform the same action, then check that their outputs match.
            // We compute the outputs of interest, by restricting the sets of output routes to the
            // intersection of the input routes and then comparing them.
            // We only need to compare the outputs if the routes were permitted.
            if (path.getAccepted()) {
              BDDRoute outputRoutes = new BDDRoute(intersection, path.getFirst());
              BDDRoute outputRoutesOther = new BDDRoute(intersection, otherPath.getFirst());
              List<BDDRouteDiff.DifferenceType> diff =
                  computeDifferences(outputRoutes, outputRoutesOther);
              // Compute any constraints on the output routes.
              BDD outputConstraints =
                  counterExampleOutputConstraints(factory, diff, outputRoutes, outputRoutesOther);
              if (!diff.isEmpty()) {
                behaviorDiff = true;
                finalConstraints = intersection.and(outputConstraints);
              }
            }
          }

          // we have found a difference, so let's get a concrete example of the difference
          if (behaviorDiff) {
            Tuple<Bgpv4Route, Tuple<Predicate<String>, String>> t =
                constraintsToInputs(finalConstraints, configAPs);
            Result<BgpRoute> otherResult =
                SearchRoutePoliciesAnswerer.simulatePolicy(
                    policy, t.getFirst(), _direction, t.getSecond(), otherPath.getFirst());
            Result<BgpRoute> refResult =
                SearchRoutePoliciesAnswerer.simulatePolicy(
                    referencePolicy, t.getFirst(), _direction, t.getSecond(), path.getFirst());
            differences.add(new Tuple<>(otherResult, refResult));

            // As a sanity check, compare the simulated results above with what the symbolic route
            // analysis predicts will happen.
            assert validateDifference(
                finalConstraints, configAPs, path, otherPath, refResult, otherResult);
          }
        }
      }
    }
    return differences.stream()
        .sorted(Comparator.comparing(t -> t.getFirst().getInputRoute().getNetwork()));
  }
}
