package org.batfish.minesweeper.question.transferbddvalidation;

import static org.batfish.datamodel.LineAction.PERMIT;
import static org.batfish.datamodel.answers.Schema.BGP_ROUTE;
import static org.batfish.datamodel.answers.Schema.BGP_ROUTE_DIFFS;
import static org.batfish.datamodel.answers.Schema.LONG;
import static org.batfish.datamodel.answers.Schema.NODE;
import static org.batfish.datamodel.answers.Schema.STRING;
import static org.batfish.datamodel.answers.Schema.TRACE_TREE;
import static org.batfish.datamodel.answers.Schema.list;
import static org.batfish.datamodel.questions.BgpRouteDiff.routeDiffs;
import static org.batfish.minesweeper.question.searchroutepolicies.SearchRoutePoliciesAnswerer.toSymbolicBgpOutputRoute;
import static org.batfish.question.testroutepolicies.TestRoutePoliciesAnswerer.COL_ACTION;
import static org.batfish.question.testroutepolicies.TestRoutePoliciesAnswerer.COL_DIFF;
import static org.batfish.question.testroutepolicies.TestRoutePoliciesAnswerer.COL_INPUT_ROUTE;
import static org.batfish.question.testroutepolicies.TestRoutePoliciesAnswerer.COL_NODE;
import static org.batfish.question.testroutepolicies.TestRoutePoliciesAnswerer.COL_OUTPUT_ROUTE;
import static org.batfish.question.testroutepolicies.TestRoutePoliciesAnswerer.COL_POLICY_NAME;
import static org.batfish.question.testroutepolicies.TestRoutePoliciesAnswerer.COL_TRACE;
import static org.batfish.question.testroutepolicies.TestRoutePoliciesAnswerer.simulatePolicyWithBgpRoute;
import static org.batfish.question.testroutepolicies.TestRoutePoliciesAnswerer.toQuestionBgpRoute;
import static org.batfish.specifier.NameRegexRoutingPolicySpecifier.ALL_ROUTING_POLICIES;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.common.Answerer;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.BgpPeerConfig;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.BgpRoute;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;
import org.batfish.minesweeper.AsPathRegexAtomicPredicates;
import org.batfish.minesweeper.ConfigAtomicPredicates;
import org.batfish.minesweeper.bdd.BDDRoute;
import org.batfish.minesweeper.bdd.ModelGeneration;
import org.batfish.minesweeper.bdd.TransferBDD;
import org.batfish.minesweeper.bdd.TransferReturn;
import org.batfish.minesweeper.utils.RouteMapEnvironment;
import org.batfish.question.testroutepolicies.Result;
import org.batfish.specifier.AllNodesNodeSpecifier;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.RoutingPolicySpecifier;
import org.batfish.specifier.SpecifierContext;
import org.batfish.specifier.SpecifierFactories;

/** Answerer for {@link TransferBDDValidationQuestion}. */
@ParametersAreNonnullByDefault
public final class TransferBDDValidationAnswerer extends Answerer {

  public static final String COL_SYMBOLIC_PREFIX = "Symbolic_";
  public static final String COL_CONCRETE_PREFIX = "Concrete_";

  public static final String COL_SYMBOLIC_ACTION = COL_SYMBOLIC_PREFIX + COL_ACTION;
  public static final String COL_CONCRETE_ACTION = COL_CONCRETE_PREFIX + COL_ACTION;
  public static final String COL_SYMBOLIC_OUTPUT_ROUTE = COL_SYMBOLIC_PREFIX + COL_OUTPUT_ROUTE;
  public static final String COL_CONCRETE_OUTPUT_ROUTE = COL_CONCRETE_PREFIX + COL_OUTPUT_ROUTE;
  public static final String COL_CONCRETE_TRACE = COL_CONCRETE_PREFIX + COL_TRACE;

  public static final String COL_SEED = "Seed";

  // the maximum number of communities to allow on a generated input route
  @VisibleForTesting public static final int MAX_COMMUNITIES_SIZE = 32;

  private final boolean _retainAllPaths;
  private final long _seed;
  private final Random _random;

  public TransferBDDValidationAnswerer(TransferBDDValidationQuestion question, IBatfish batfish) {
    super(question, batfish);
    _retainAllPaths = question.getRetainAllPaths();
    _seed = question.getSeed();
    _random = new Random(_seed);
  }

  @Override
  public AnswerElement answer(NetworkSnapshot snapshot) {
    TransferBDDValidationQuestion question = (TransferBDDValidationQuestion) _question;

    NodeSpecifier nodeSpecifier =
        SpecifierFactories.getNodeSpecifierOrDefault(
            question.getNodes(), AllNodesNodeSpecifier.INSTANCE);

    RoutingPolicySpecifier policySpecifier =
        SpecifierFactories.getRoutingPolicySpecifierOrDefault(
            question.getPolicies(), ALL_ROUTING_POLICIES);

    SpecifierContext ctx = _batfish.specifierContext(snapshot);
    Set<String> nodeNames = nodeSpecifier.resolve(ctx);

    List<Row> rows = new ArrayList<>();

    for (String nodeName : nodeNames) {
      Configuration c = ctx.getConfigs().get(nodeName);
      if (c == null) {
        continue;
      }

      Set<RoutingPolicy> specifiedPolicies = policySpecifier.resolve(nodeName, ctx);

      ConfigAtomicPredicates aps =
          new ConfigAtomicPredicates(
              ImmutableList.of(Map.entry(c, c.getRoutingPolicies().values())),
              ImmutableSet.of(),
              ImmutableSet.of());
      TransferBDD tbdd = new TransferBDD(aps);

      // Collect and analyze import policies
      List<RoutingPolicy> importPolicies =
          collectBgpPeerPolicies(c, specifiedPolicies, Ipv4UnicastAddressFamily::getImportPolicy);
      rows.addAll(analyzePolicies(importPolicies, tbdd, Environment.Direction.IN));

      // Collect and analyze export policies
      List<RoutingPolicy> exportPolicies =
          collectBgpPeerPolicies(c, specifiedPolicies, Ipv4UnicastAddressFamily::getExportPolicy);
      rows.addAll(analyzePolicies(exportPolicies, tbdd, Environment.Direction.OUT));
    }

    TableAnswerElement answerElement = new TableAnswerElement(createMetadata());
    answerElement.postProcessAnswer(_question, rows);
    return answerElement;
  }

  /**
   * Collects Bgp peer route policies to use for validation.
   *
   * @param c the network configuration
   * @param specifiedPolicies the set of route policies specified by the user
   * @param policyAccessor function to access import or export policies from an address family
   * @return a list of the policies that are accessible by the given function and are within the set
   *     specified by the user
   */
  private List<RoutingPolicy> collectBgpPeerPolicies(
      Configuration c,
      Set<RoutingPolicy> specifiedPolicies,
      Function<Ipv4UnicastAddressFamily, String> policyAccessor) {
    List<RoutingPolicy> policies = new ArrayList<>();

    // Track policies that we've already collected to avoid duplicates
    Set<List<Statement>> policiesSeen = new HashSet<>();

    for (Vrf vrf : c.getVrfs().values()) {
      BgpProcess bgpProcess = vrf.getBgpProcess();
      if (bgpProcess == null) {
        continue;
      }

      // Iterate through all BGP peer configs
      for (BgpPeerConfig peer : bgpProcess.getAllPeerConfigs()) {
        Ipv4UnicastAddressFamily family = peer.getIpv4UnicastAddressFamily();
        if (family != null) {
          String policyName = policyAccessor.apply(family);
          RoutingPolicy policy = c.getRoutingPolicies().get(policyName);
          if (policy != null
              && specifiedPolicies.contains(policy)
              && !policiesSeen.contains(policy.getStatements())) {
            policiesSeen.add(policy.getStatements());
            policies.add(policy);
          }
        }
      }
    }
    return policies;
  }

  /**
   * Validates symbolic analysis against concrete simulation on a list of routing policies.
   *
   * @param policies the routing policies to use
   * @param tbdd the TransferBDD object for symbolic analysis
   * @param direction whether these are import (IN) or export (OUT) policies
   * @return a list of rows representing validation violations
   */
  private List<Row> analyzePolicies(
      List<RoutingPolicy> policies, TransferBDD tbdd, Environment.Direction direction) {
    List<Row> results = new ArrayList<>();
    for (RoutingPolicy policy : policies) {
      List<TransferReturn> paths = tbdd.computePaths(policy, _retainAllPaths);
      results.addAll(validatePaths(policy, paths, tbdd, direction));
    }
    return results;
  }

  /**
   * Compare the results of the symbolic route analysis with Batfish's concrete route simulation.
   * For each path returned by the symbolic analysis, we solve for a random input route that goes
   * down that path, simulate it through the route map, and compare the result to what the symbolic
   * analysis expects.
   *
   * @param policy the route policy being checked
   * @param paths the results of the symbolic analysis -- a set of paths through the policy
   * @param tbdd object containing information about the symbolic analysis
   * @param direction whether this is an import (IN) or export (OUT) policy
   * @return a list of rows representing violations of the validity test
   */
  @VisibleForTesting
  List<Row> validatePaths(
      RoutingPolicy policy,
      List<TransferReturn> paths,
      TransferBDD tbdd,
      Environment.Direction direction) {
    BDDFactory factory = tbdd.getFactory();
    ConfigAtomicPredicates aps = tbdd.getConfigAtomicPredicates();
    List<Row> violations = new ArrayList<>();

    for (TransferReturn path : paths) {
      // skip validation for paths that encounter an unsupported feature
      if (path.getOutputRoute().getUnsupported()) {
        continue;
      }

      // solve for a random input route and environment that causes execution to go down this path
      BDDRoute origRoute = tbdd.getOriginalRoute();
      BDD constraints =
          path.getInputConstraints()
              // for now we only validate paths that process BGP routes
              .andWith(
                  origRoute
                      .wellFormednessConstraints(true)
                      // we limit the size of the cluster list for performance reasons;
                      // this means it is possible that we will skip validation for some feasible
                      // paths
                      .andWith(origRoute.getClusterListLength().leq(2000)));
      if (constraints.isZero()) {
        continue;
      }
      BDD fullConstraints = constraints;
      // TransferBDD does not accurately handle the HasSize predicate, which constrains the number
      // of communities on a route -- see CommunitySetMatchExprToBDD::hasSize.  Typically that
      // predicate is used to check for routes that have too many communities. To avoid false
      // positives in our validation, we limit the number of communities on generated routes.
      // TODO: TransferBDD has similar treatment of HasAsPathLength, so we could also limit the
      // lengths of AS paths to avoid false positives, but that seems less likely to be
      // an issue in practice.
      BDD[] commAPs = origRoute.getCommunityAtomicPredicates();
      int numCommAPs = commAPs.length;
      if (numCommAPs > MAX_COMMUNITIES_SIZE) {
        // figure out which community APs have to be true/false in order to satisfy the constraints.
        // there could be multiple models with different required community APs, and
        // here we are always getting the "smallest" model, but in practice that should not be a big
        // loss of coverage.
        BDD requiredCommDispositions = constraints.satOne();
        // determine how many communities are required to be present in the route
        int numPos =
            (int)
                Arrays.stream(commAPs)
                    .filter(var -> !requiredCommDispositions.diffSat(var))
                    .count();
        // skip this path if too many communities are required
        if (numPos > MAX_COMMUNITIES_SIZE) {
          continue;
        }
        // determine the list of community APs that are and are not required to have a particular
        // disposition according to this model
        BDD support = requiredCommDispositions.support();
        Map<Boolean, List<BDD>> partitioned =
            Arrays.stream(commAPs).collect(Collectors.partitioningBy(support::diffSat));
        List<BDD> unassigned = partitioned.get(true);
        // choose a random subset of the unassigned community APs of sufficient size and require
        // them to be false
        Collections.shuffle(unassigned, _random);
        int minFalse = unassigned.size() - (MAX_COMMUNITIES_SIZE - numPos);
        BDD mustBeFalse =
            minFalse > 0 ? factory.orAll(unassigned.subList(0, minFalse)).not() : factory.one();
        BDD assigned = factory.orAll(partitioned.get(false));
        fullConstraints =
            fullConstraints.andWith(
                requiredCommDispositions.project(assigned).andWith(mustBeFalse));
        assigned.free();
      }
      BDD fullModel = fullConstraints.randomFullSatOne(_random.nextInt());
      fullConstraints.free();
      Bgpv4Route inRoute = ModelGeneration.satAssignmentToBgpInputRoute(fullModel, aps);
      RouteMapEnvironment env = ModelGeneration.satAssignmentToEnvironment(fullModel, aps);

      ConfigAtomicPredicates updatedAPs;
      if (!path.getOutputRoute().getPrependedASes().isEmpty()) {
        // update the atomic predicates to include any prepended ASes on this path
        updatedAPs = new ConfigAtomicPredicates(aps);
        AsPathRegexAtomicPredicates asPathAPs = updatedAPs.getAsPathRegexAtomicPredicates();
        asPathAPs.prependAPs(path.getOutputRoute().getPrependedASes());
      } else {
        updatedAPs = aps;
      }

      // simulate the solved-for input route in the solved-for environment;
      violations.addAll(
          simulateAndCompare(policy, inRoute, env, direction, path, fullModel, updatedAPs));
    }
    return violations;
  }

  /**
   * Simulates a route through a routing policy along a given path and compares the results with the
   * symbolic analysis.
   *
   * @param policy The routing policy to simulate
   * @param inRoute The input BGP route
   * @param env The routing environment containing session properties and other context
   * @param direction The direction of the policy (IN or OUT)
   * @param path The symbolic analysis results for a particular path through the routing policy
   * @param fullModel A solution to the symbolic constraints, consistent with the given input route
   * @param aps The atomic predicates used for symbolic analysis
   * @return A list of rows representing validation violations (empty if validation succeeds)
   */
  private List<Row> simulateAndCompare(
      RoutingPolicy policy,
      Bgpv4Route inRoute,
      RouteMapEnvironment env,
      Environment.Direction direction,
      TransferReturn path,
      BDD fullModel,
      ConfigAtomicPredicates aps) {

    // simulate the policy
    Result<Bgpv4Route, Bgpv4Route> result =
        simulatePolicyWithBgpRoute(
            policy,
            inRoute,
            env.getSessionProperties(),
            direction,
            env.getSuccessfulTracks(),
            env.getSourceVrf());

    // convert the output route to a form that can be compared against the results
    // of symbolic analysis
    Result<?, BgpRoute> resultForComparison =
        result.setOutputRoute(
            toSymbolicBgpOutputRoute(
                toQuestionBgpRoute(result.getOutputRoute()), path.getOutputRoute()));

    // compare the simulated results to that produced by the symbolic analysis
    LineAction symbolicAction = path.getAccepted() ? PERMIT : LineAction.DENY;
    boolean validate =
        ModelGeneration.validateModel(
            fullModel, path.getOutputRoute(), aps, symbolicAction, direction, resultForComparison);

    if (!validate) {
      return ImmutableList.of(
          createViolationRow(
              policy,
              inRoute,
              symbolicAction,
              symbolicAction == PERMIT
                  ? ModelGeneration.satAssignmentToOutputRoute(
                      fullModel, path.getOutputRoute(), aps, direction)
                  : null,
              resultForComparison));
    }
    return ImmutableList.of();
  }

  /**
   * Creates a row representing a validation violation between symbolic analysis and concrete
   * simulation.
   *
   * @param policy The routing policy that was analyzed
   * @param inputRoute The input route used for simulation
   * @param symbolicAction The action (PERMIT/DENY) determined by symbolic analysis
   * @param symbolicOutputRoute The output route produced by symbolic analysis (null if action is
   *     DENY)
   * @param concreteResult The result of the concrete simulation
   * @return An object containing the violation details
   */
  private Row createViolationRow(
      RoutingPolicy policy,
      AbstractRoute inputRoute,
      LineAction symbolicAction,
      @Nullable BgpRoute symbolicOutputRoute,
      Result<?, BgpRoute> concreteResult) {

    BgpRoute concreteOutputRoute = concreteResult.getOutputRoute();

    return Row.builder()
        .put(COL_NODE, new Node(concreteResult.getPolicyId().getNode()))
        .put(COL_POLICY_NAME, policy.getName())
        .put(COL_INPUT_ROUTE, toQuestionBgpRoute((Bgpv4Route) inputRoute))
        .put(COL_SYMBOLIC_ACTION, symbolicAction)
        .put(COL_CONCRETE_ACTION, concreteResult.getAction())
        .put(COL_SYMBOLIC_OUTPUT_ROUTE, symbolicOutputRoute)
        .put(COL_CONCRETE_OUTPUT_ROUTE, concreteOutputRoute)
        .put(COL_DIFF, routeDiffs(symbolicOutputRoute, concreteOutputRoute))
        .put(COL_CONCRETE_TRACE, concreteResult.getTrace())
        .put(COL_SEED, _seed)
        .build();
  }

  /** Creates the metadata for the table answer. */
  private static TableMetadata createMetadata() {
    List<ColumnMetadata> columnMetadata =
        ImmutableList.of(
            new ColumnMetadata(COL_NODE, NODE, "The node that has the policy", true, false),
            new ColumnMetadata(COL_POLICY_NAME, STRING, "The name of this policy", true, false),
            new ColumnMetadata(COL_INPUT_ROUTE, BGP_ROUTE, "The input route", true, false),
            new ColumnMetadata(
                COL_SYMBOLIC_ACTION, STRING, "The action from symbolic analysis", false, true),
            new ColumnMetadata(
                COL_CONCRETE_ACTION, STRING, "The action from concrete simulation", false, true),
            new ColumnMetadata(
                COL_SYMBOLIC_OUTPUT_ROUTE,
                BGP_ROUTE,
                "The output route from symbolic analysis",
                false,
                true),
            new ColumnMetadata(
                COL_CONCRETE_OUTPUT_ROUTE,
                BGP_ROUTE,
                "The output route from concrete simulation",
                false,
                true),
            new ColumnMetadata(
                COL_DIFF,
                BGP_ROUTE_DIFFS,
                "The difference between the symbolic and concrete output routes",
                false,
                true),
            new ColumnMetadata(
                COL_CONCRETE_TRACE,
                list(TRACE_TREE),
                "Route policy trace from concrete simulation",
                false,
                true),
            new ColumnMetadata(
                COL_SEED, LONG, "The seed used for random number generation", false, true));
    return new TableMetadata(
        columnMetadata,
        String.format(
            "Validation violations for route policy ${%s} in node ${%s}",
            COL_POLICY_NAME, COL_NODE));
  }
}
