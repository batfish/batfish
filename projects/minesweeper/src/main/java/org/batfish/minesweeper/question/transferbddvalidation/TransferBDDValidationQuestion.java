package org.batfish.minesweeper.question.transferbddvalidation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.questions.Question;

/**
 * A question for validating symbolic route analysis ({@link
 * org.batfish.minesweeper.bdd.TransferBDD}) against concrete route simulation ({@link
 * org.batfish.question.testroutepolicies.TestRoutePoliciesQuestion}).
 *
 * <p>This question currently validates only the route policies that are used as import or export
 * policies on BGP peers.
 */
@ParametersAreNonnullByDefault
public final class TransferBDDValidationQuestion extends Question {

  private static final String PROP_NODES = "nodes";
  private static final String PROP_POLICIES = "policies";
  private static final String PROP_RETAIN_ALL_PATHS = "retainAllPaths";
  private static final String PROP_SEED = "seed";

  private final @Nullable String _nodes;
  private final @Nullable String _policies;

  // If true, then the symbolic analysis will produce one result per feasible path through the given
  // route policy, rather than coalescing paths that are compatible with one another (same
  // permit/deny result and same route updates). Default value is false.
  private final boolean _retainAllPaths;

  // A seed for random-number generation
  private final long _seed;

  public TransferBDDValidationQuestion() {
    this(null, null, false, new Random().nextLong());
  }

  public TransferBDDValidationQuestion(
      @Nullable String nodes, @Nullable String policies, boolean retainAllPaths, long seed) {
    _nodes = nodes;
    _policies = policies;
    _retainAllPaths = retainAllPaths;
    _seed = seed;
  }

  @JsonCreator
  private static TransferBDDValidationQuestion jsonCreator(
      @JsonProperty(PROP_NODES) @Nullable String nodes,
      @JsonProperty(PROP_POLICIES) @Nullable String policies,
      @JsonProperty(PROP_RETAIN_ALL_PATHS) boolean retainAllPaths,
      @JsonProperty(PROP_SEED) long seed) {
    return new TransferBDDValidationQuestion(nodes, policies, retainAllPaths, seed);
  }

  @JsonIgnore
  @Override
  public boolean getDataPlane() {
    return false;
  }

  @JsonIgnore
  @Override
  public @Nonnull String getName() {
    return "transferBDDValidation";
  }

  @JsonProperty(PROP_NODES)
  public @Nullable String getNodes() {
    return _nodes;
  }

  @JsonProperty(PROP_POLICIES)
  public @Nullable String getPolicies() {
    return _policies;
  }

  @JsonProperty(PROP_RETAIN_ALL_PATHS)
  public boolean getRetainAllPaths() {
    return _retainAllPaths;
  }

  @JsonProperty(PROP_SEED)
  public long getSeed() {
    return _seed;
  }
}
