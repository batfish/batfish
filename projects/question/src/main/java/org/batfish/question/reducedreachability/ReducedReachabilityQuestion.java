package org.batfish.question.reducedreachability;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.batfish.question.specifiers.DispositionSpecifier.SUCCESS_SPECIFIER;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.PacketHeaderConstraints;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.specifiers.DispositionSpecifier;
import org.batfish.question.specifiers.PathConstraintsInput;

/** A zero-input question to check for reduced reachability between base and delta snapshots. */
public final class ReducedReachabilityQuestion extends Question {
  private static final String PROP_ACTIONS = "actions";
  private static final String PROP_HEADERS = "headers";
  private static final String PROP_PATH_CONSTRAINTS = "pathConstraints";

  @Nonnull private final DispositionSpecifier _actions;
  @Nonnull private final PathConstraintsInput _pathConstraints;
  @Nonnull private final PacketHeaderConstraints _headerConstraints;

  @JsonCreator
  public ReducedReachabilityQuestion(
      @Nullable @JsonProperty(PROP_ACTIONS) DispositionSpecifier actions,
      @Nullable @JsonProperty(PROP_HEADERS) PacketHeaderConstraints headerConstraints,
      @Nullable @JsonProperty(PROP_PATH_CONSTRAINTS) PathConstraintsInput pathConstraints) {
    setDifferential(true);
    _actions = firstNonNull(actions, SUCCESS_SPECIFIER);
    _headerConstraints = firstNonNull(headerConstraints, PacketHeaderConstraints.unconstrained());
    _pathConstraints = firstNonNull(pathConstraints, PathConstraintsInput.unconstrained());
  }

  public ReducedReachabilityQuestion() {
    this(null, null, null);
  }

  @Nonnull
  DispositionSpecifier getActions() {
    return _actions;
  }

  @JsonProperty(PROP_HEADERS)
  @Nonnull
  PacketHeaderConstraints getHeaderConstraints() {
    return _headerConstraints;
  }

  @JsonProperty(PROP_PATH_CONSTRAINTS)
  @Nonnull
  PathConstraintsInput getPathConstraints() {
    return _pathConstraints;
  }

  @Override
  public boolean getDataPlane() {
    return true;
  }

  @Override
  public String getName() {
    return "reducedreachability";
  }
}
