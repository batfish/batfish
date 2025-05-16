package org.batfish.minesweeper.question.transferbddvalidation;

import static org.batfish.question.testroutepolicies.TestRoutePoliciesAnswerer.simulatePolicy;
import static org.batfish.specifier.NameRegexRoutingPolicySpecifier.ALL_ROUTING_POLICIES;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.common.Answerer;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;
import org.batfish.minesweeper.ConfigAtomicPredicates;
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

  static final String COL_NODE = "Node";
  static final String COL_POLICY = "Policy";
  static final String COL_STATUS = "Status";
  static final String COL_DETAILS = "Details";

  public TransferBDDValidationAnswerer(TransferBDDValidationQuestion question, IBatfish batfish) {
    super(question, batfish);
  }

  @Override
  public AnswerElement answer(NetworkSnapshot snapshot) {
    TransferBDDValidationQuestion question = (TransferBDDValidationQuestion) _question;

    // Get the specifier for routing policies
    RoutingPolicySpecifier policySpecifier =
        SpecifierFactories.getRoutingPolicySpecifierOrDefault(
            question.getPolicies(), ALL_ROUTING_POLICIES);

    // Get the specifier for nodes
    NodeSpecifier nodeSpecifier =
        SpecifierFactories.getNodeSpecifierOrDefault(
            question.getNodes(), AllNodesNodeSpecifier.INSTANCE);

    // Get the context and configurations
    SpecifierContext ctx = _batfish.specifierContext(snapshot);

    // Get the nodes to analyze
    Set<String> nodeNames = nodeSpecifier.resolve(ctx);

    // Create the answer table
    TableAnswerElement answerElement = new TableAnswerElement(createMetadata());

    // Process each node and policy
    for (String nodeName : nodeNames) {
      Configuration c = ctx.getConfigs().get(nodeName);
      if (c == null) {
        continue;
      }

      // Get policies for this node
      Set<RoutingPolicy> policies = policySpecifier.resolve(nodeName, ctx);

      for (RoutingPolicy policy : policies) {
        String policyName = policy.getName();

        // Validate the policy using TransferBDD
        try {
          // Perform validation by comparing TransferBDD results with TestRoutePolicies
          ValidationResult result = validatePolicyWithTestRoutePolicies(c, policy);
          String status = result.isValid() ? "Valid" : "Invalid";

          // Add a row to the answer
          answerElement.addRow(
              Row.builder()
                  .put(COL_NODE, nodeName)
                  .put(COL_POLICY, policyName)
                  .put(COL_STATUS, status)
                  .put(COL_DETAILS, result.getDetails())
                  .build());
        } catch (Exception e) {
          // Handle exceptions during validation
          answerElement.addRow(
              Row.builder()
                  .put(COL_NODE, nodeName)
                  .put(COL_POLICY, policyName)
                  .put(COL_STATUS, "Error")
                  .put(COL_DETAILS, e.getMessage())
                  .build());
        }
      }
    }

    return answerElement;
  }

  /**
   * Validates a routing policy by comparing TransferBDD results with TestRoutePolicies.
   *
   * @param c The configuration containing the policy
   * @param policy The routing policy to validate
   * @return A ValidationResult containing validation status and details
   */
  private ValidationResult validatePolicyWithTestRoutePolicies(
      Configuration c, RoutingPolicy policy) {
    try {
      // Create ConfigAtomicPredicates for the configuration
      ConfigAtomicPredicates aps = new ConfigAtomicPredicates(c);

      // Create TransferBDD instance
      TransferBDD tbdd = new TransferBDD(aps);

      // Create context for the policy
      TransferBDD.Context context = TransferBDD.Context.forPolicy(policy);

      // Compute all possible paths through the policy
      List<TransferReturn> paths = tbdd.computePaths(policy);

      // Check for various validation issues
      boolean hasUnsupportedFeatures =
          paths.stream().anyMatch(path -> path.getOutputRoute().getUnsupported());

      boolean hasUnreachablePaths = false;

      // Check for unreachable paths (paths with zero BDD)
      for (TransferReturn path : paths) {
        if (path.getInputConstraints().isZero()) {
          hasUnreachablePaths = true;
          break;
        }
      }

      // Validate paths by comparing with TestRoutePolicies
      boolean pathsValid = validatePaths(policy, paths, tbdd.getFactory());

      // Determine overall validation status
      boolean isValid = !hasUnsupportedFeatures && !hasUnreachablePaths && pathsValid;

      // Build detailed message
      StringBuilder details = new StringBuilder();
      details.append("TransferBDD validation ");

      if (isValid) {
        details.append("succeeded. All paths match TestRoutePolicies simulation.");
      } else {
        details.append("found the following issues:");
        if (hasUnsupportedFeatures) {
          details.append("\n- Policy uses unsupported features");
        }
        if (hasUnreachablePaths) {
          details.append("\n- Policy contains unreachable paths");
        }
        if (!pathsValid) {
          details.append("\n- TransferBDD results don't match TestRoutePolicies simulation");
        }
      }

      return new ValidationResult(isValid, details.toString());
    } catch (Exception e) {
      return new ValidationResult(false, "Error during validation: " + e.getMessage());
    }
  }

  /**
   * Compare the results of the symbolic route analysis with Batfish's concrete route simulation.
   * For each path returned by the symbolic analysis, we solve for an input route that goes down
   * that path, simulate it through the route map, and compare the result to what the symbolic
   * analysis expects.
   *
   * @param policy the route policy being checked
   * @param paths the results of the symbolic analysis -- a set of paths through the policy
   * @param factory the BDD factory
   * @return a boolean indicating whether the check succeeded
   */
  private boolean validatePaths(
      RoutingPolicy policy, List<TransferReturn> paths, BDDFactory factory) {
    for (TransferReturn path : paths) {
      // Skip paths with zero constraints (unreachable paths)
      if (path.getInputConstraints().isZero()) {
        continue;
      }

      // solve for an input route and environment that causes execution to go down this path
      BDD fullConstraints = path.getInputConstraints();
      BDD fullModel =
          ModelGeneration.constraintsToModel(
              fullConstraints, path.getOutputRoute().getConfigAtomicPredicates());
      AbstractRoute inRoute =
          ModelGeneration.satAssignmentToInputRoute(
              fullModel, path.getOutputRoute().getConfigAtomicPredicates());
      RouteMapEnvironment env =
          ModelGeneration.satAssignmentToEnvironment(
              fullModel, path.getOutputRoute().getConfigAtomicPredicates());

      // simulate the input route in that environment;
      // for good measure we simulate twice, with the policy respectively considered an import and
      // export policy
      Result<? extends AbstractRoute, Bgpv4Route> inResult =
          simulatePolicy(
              policy,
              inRoute,
              env.getSessionProperties(),
              Environment.Direction.IN,
              env.getSuccessfulTracks(),
              env.getSourceVrf());

      Result<? extends AbstractRoute, Bgpv4Route> outResult =
          simulatePolicy(
              policy,
              inRoute,
              env.getSessionProperties(),
              Environment.Direction.OUT,
              env.getSuccessfulTracks(),
              env.getSourceVrf());

      // update the atomic predicates to include any prepended ASes on this path
      ConfigAtomicPredicates configAPsCopy =
          new ConfigAtomicPredicates(path.getOutputRoute().getConfigAtomicPredicates());
      configAPsCopy
          .getAsPathRegexAtomicPredicates()
          .prependAPs(path.getOutputRoute().getPrependedASes());

      // compare the simulated results to that produced by the symbolic analysis
      LineAction action = path.getAccepted() ? LineAction.PERMIT : LineAction.DENY;
      boolean inValidate =
          ModelGeneration.validateModel(
              fullModel,
              path.getOutputRoute(),
              configAPsCopy,
              action,
              Environment.Direction.IN,
              inResult);
      boolean outValidate =
          ModelGeneration.validateModel(
              fullModel,
              path.getOutputRoute(),
              configAPsCopy,
              action,
              Environment.Direction.OUT,
              outResult);
      if (!inValidate || !outValidate) {
        return false;
      }
    }
    return true;
  }

  /** Create table metadata for the answer. */
  private static TableMetadata createMetadata() {
    List<ColumnMetadata> columnMetadata =
        ImmutableList.of(
            new ColumnMetadata(COL_NODE, Schema.STRING, "Node name", true, false),
            new ColumnMetadata(COL_POLICY, Schema.STRING, "Routing policy name", true, false),
            new ColumnMetadata(COL_STATUS, Schema.STRING, "Validation status", false, true),
            new ColumnMetadata(COL_DETAILS, Schema.STRING, "Additional details", false, false));

    return new TableMetadata(columnMetadata);
  }

  /** Simple class to hold validation results. */
  private static class ValidationResult {
    private final boolean isValid;
    private final String details;

    public ValidationResult(boolean isValid, String details) {
      this.isValid = isValid;
      this.details = details;
    }

    public boolean isValid() {
      return isValid;
    }

    public String getDetails() {
      return details;
    }
  }
}
