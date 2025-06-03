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
 * <p>This question validates only the route policies that are used as import or export policies on
 * BGP peer groups.
 */
@ParametersAreNonnullByDefault
public final class TransferBDDValidationQuestion extends Question {

  private static final String PROP_NODES = "nodes";
  private static final String PROP_SEED = "seed";

  private final @Nullable String _nodes;

  // A seed for random-number generation
  private final long _seed;

  public TransferBDDValidationQuestion() {
    this(null, new Random().nextLong());
  }

  public TransferBDDValidationQuestion(@Nullable String nodes, long seed) {
    _nodes = nodes;
    _seed = seed;
  }

  @JsonCreator
  private static TransferBDDValidationQuestion jsonCreator(
      @JsonProperty(PROP_NODES) @Nullable String nodes, @JsonProperty(PROP_SEED) long seed) {
    return new TransferBDDValidationQuestion(nodes, seed);
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

  @JsonProperty(PROP_SEED)
  public long getSeed() {
    return _seed;
  }
}
