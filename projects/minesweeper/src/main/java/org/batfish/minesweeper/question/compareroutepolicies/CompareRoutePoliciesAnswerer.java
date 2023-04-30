package org.batfish.minesweeper.question.compareroutepolicies;

import static org.batfish.minesweeper.bdd.BDDRouteDiff.computeDifferences;
import static org.batfish.minesweeper.bdd.ModelGeneration.constraintsToModel;
import static org.batfish.minesweeper.bdd.ModelGeneration.satAssignmentToEnvironment;
import static org.batfish.minesweeper.bdd.ModelGeneration.satAssignmentToInputRoute;
import static org.batfish.question.testroutepolicies.TestRoutePoliciesAnswerer.diffRowResultsFor;
import static org.batfish.specifier.NameRegexRoutingPolicySpecifier.ALL_ROUTING_POLICIES;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
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
import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.JFactory;
import org.batfish.common.Answerer;
import org.batfish.common.BatfishException;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.minesweeper.CommunityVar;
import org.batfish.minesweeper.ConfigAtomicPredicates;
import org.batfish.minesweeper.bdd.BDDRoute;
import org.batfish.minesweeper.bdd.BDDRouteDiff;
import org.batfish.minesweeper.bdd.TransferBDD;
import org.batfish.minesweeper.bdd.TransferReturn;
import org.batfish.minesweeper.utils.Tuple;
import org.batfish.question.testroutepolicies.TestRoutePoliciesAnswerer;
import org.batfish.specifier.AllNodesNodeSpecifier;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.RoutingPolicySpecifier;
import org.batfish.specifier.SpecifierContext;
import org.batfish.specifier.SpecifierFactories;

/** An answerer for {@link CompareRoutePoliciesQuestion}. */
@SuppressWarnings("DuplicatedCode")
@ParametersAreNonnullByDefault
public final class CompareRoutePoliciesAnswerer extends Answerer {

  @Nonnull private final Environment.Direction _direction;

  @Nonnull private final String _policySpecifierString;
  @Nullable private final String _referencePolicySpecifierString;
  @Nonnull private final RoutingPolicySpecifier _policySpecifier;
  @Nullable private final RoutingPolicySpecifier _referencePolicySpecifier;

  @Nonnull private final NodeSpecifier _nodeSpecifier;

  @Nonnull private final Set<String> _communityRegexes;
  @Nonnull private final Set<String> _asPathRegexes;

  public CompareRoutePoliciesAnswerer(
      org.batfish.minesweeper.question.compareroutepolicies.CompareRoutePoliciesQuestion question,
      IBatfish batfish) {
    super(question, batfish);
    _nodeSpecifier =
        SpecifierFactories.getNodeSpecifierOrDefault(
            question.getNodes(), AllNodesNodeSpecifier.INSTANCE);
    _direction = question.getDirection();

    _policySpecifierString = question.getPolicy();
    _policySpecifier =
        SpecifierFactories.getRoutingPolicySpecifierOrDefault(
            _policySpecifierString, ALL_ROUTING_POLICIES);

    // If the referencePolicySpecifier is null then we compare using the route-maps in
    // policySpecifier
    _referencePolicySpecifierString = question.getReferencePolicy();
    _referencePolicySpecifier =
        SpecifierFactories.getRoutingPolicySpecifierOrDefault(
            _referencePolicySpecifierString, _policySpecifier);

    // in the future, it may improve performance to combine all input community regexes
    // into a single regex representing their disjunction, and similarly for all output
    // community regexes, in order to minimize the number of atomic predicates that are
    // created and tracked by the analysis
    _communityRegexes = ImmutableSet.<String>builder().build();
    _asPathRegexes = ImmutableSet.<String>builder().build();
  }

  /**
   * Convert the results of symbolic route analysis into an answer to this question, if the
   * resulting constraints are satisfiable.
   *
   * @param referencePolicy the reference route policy that we compared against.
   * @param policy the proposed route policy.
   * @param inRoute the input route to simulate on each policy
   * @param env a pair of predicate that assigns truth values to tracks and a source VRF
   * @return the concrete input route and, if the desired action is PERMIT, the concrete output
   *     routes resulting from analyzing the given policies.
   */
  private Row computeDifferencesForInputs(
      RoutingPolicy referencePolicy,
      RoutingPolicy policy,
      Bgpv4Route inRoute,
      Tuple<Predicate<String>, String> env) {
    return diffRowResultsFor(
        referencePolicy, policy, inRoute, _direction, env.getFirst(), env.getSecond());
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
    BDD fullModel = constraintsToModel(constraints, configAPs);
    return new Tuple<>(
        satAssignmentToInputRoute(fullModel, configAPs),
        satAssignmentToEnvironment(fullModel, configAPs));
  }

  /**
   * @param tbdd the transfer function to symbolically evaluate
   * @return the set of paths generated by symbolic execution
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
   *     capturing differences in communities and as-path for now; the rest are not necessary
   *     because they do not have additive semantics, like "set community additive" and "set as-path
   *     prepend". (actually, I think you might be able to plus on the local-pref value, TODO:
   *     check)
   */
  private BDD counterExampleOutputConstraints(
      BDDFactory factory, List<BDDRouteDiff.DifferenceType> diffs, BDDRoute r1, BDDRoute r2) {
    BDD acc = factory.zero();
    for (BDDRouteDiff.DifferenceType d : diffs) {
      switch (d) {
        case OSPF_METRIC:
        case LOCAL_PREF:
        case MED:
        case NEXTHOP:
        case NEXTHOP_DISCARDED:
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
        case AS_PATH:
          BDD[] asPathAtomicPredicates = r1.getAsPathRegexAtomicPredicates();
          BDD[] otherAsPathAtomicPredicates = r2.getAsPathRegexAtomicPredicates();
          for (int i = 0; i < asPathAtomicPredicates.length; i++) {
            BDD outConstraint = asPathAtomicPredicates[i].xor(otherAsPathAtomicPredicates[i]);
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
   * Compare two route policies for behavioral differences.
   *
   * @param referencePolicy the routing policy of the reference snapshot
   * @param policy the routing policy of the current snapshot
   * @param configAPs an object providing the atomic predicates for the policy's owner configuration
   * @return a set of differences
   */
  private List<Row> comparePolicies(
      RoutingPolicy referencePolicy, RoutingPolicy policy, ConfigAtomicPredicates configAPs) {
    // The set of differences if any.
    List<BDD> differences = new ArrayList<>();

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
        // some common
        // input routes and their behavior should match.
        if (!intersection.isZero()) {
          // Naive check to see if both policies accepted/rejected the route(s).
          if (path.getAccepted() != otherPath.getAccepted()) {
            differences.add(intersection);
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
                differences.add(intersection.and(outputConstraints));
              }
            }
          }
        }
      }
    }
    return differences.stream()
        .map(intersection -> constraintsToInputs(intersection, configAPs))
        .sorted(Comparator.comparing(t -> t.getFirst().getNetwork()))
        .map(t -> computeDifferencesForInputs(referencePolicy, policy, t.getFirst(), t.getSecond()))
        .collect(Collectors.toList());
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
  private Stream<Row> comparePoliciesForNode(
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
              _referencePolicySpecifier));
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
                              comparePolicies(referencePolicy, currentPolicy, configAPs).stream()));
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
                  comparePolicies(policyTuple.getFirst(), policyTuple.getSecond(), configAPs)
                      .stream());
    }
  }

  @Override
  public AnswerElement answer(NetworkSnapshot snapshot) {
    throw new BatfishException(
        String.format("%s can only be run in differential mode.", _question.getName()));
  }

  /**
   * Compares the policies in policySpecifier with the policies in proposedPolicySpecifier (all of
   * them, their names don't have to match up). If, however, the proposedPolicySpecifier is empty it
   * will do a 1-1 comparison with the policies found in policySpecifier. Note, this only compares
   * across the same hostnames between the two snapshots, i.e., it will compare route-maps in r1
   * with route-maps in r1 of the new snapshot.
   *
   * @param snapshot the current snapshot
   * @param reference the reference snapshot
   * @return
   */
  @Override
  public AnswerElement answerDiff(NetworkSnapshot snapshot, NetworkSnapshot reference) {

    SpecifierContext currentContext = _batfish.specifierContext(snapshot);
    SpecifierContext referenceContext = _batfish.specifierContext(reference);
    Set<String> currentNodes = _nodeSpecifier.resolve(currentContext);
    Set<String> referenceNodes = _nodeSpecifier.resolve(referenceContext);
    // Only compare nodes that are in both snapshots.
    Stream<String> nodes = currentNodes.stream().filter(referenceNodes::contains);

    // Using stream.sorted() to ensure consistent order.
    List<Row> rows =
        nodes
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
                })
            .collect(ImmutableList.toImmutableList());
    TableAnswerElement answerElement =
        new TableAnswerElement(TestRoutePoliciesAnswerer.compareMetadata());
    answerElement.postProcessAnswer(_question, rows);
    return answerElement;
  }

  @Nonnull
  @VisibleForTesting
  NodeSpecifier getNodeSpecifier() {
    return _nodeSpecifier;
  }

  @Nonnull
  @VisibleForTesting
  RoutingPolicySpecifier getPolicySpecifier() {
    return _policySpecifier;
  }

  @Nullable
  @VisibleForTesting
  RoutingPolicySpecifier getReferencePolicySpecifier() {
    return _referencePolicySpecifier;
  }
}
