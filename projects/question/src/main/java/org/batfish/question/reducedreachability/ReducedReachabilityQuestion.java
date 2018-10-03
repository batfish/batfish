package org.batfish.question.reducedreachability;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSortedSet;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.questions.Question;

/** A zero-input question to check for reduced reachability between base and delta snapshots. */
public final class ReducedReachabilityQuestion extends Question {
  private static final String PROP_ACTIONS = "actions";

  @Nonnull private final SortedSet<FlowDisposition> _actions;

  @JsonCreator
  public ReducedReachabilityQuestion(
      @Nullable @JsonProperty(PROP_ACTIONS) SortedSet<FlowDisposition> actions) {
    setDifferential(true);
    _actions =
        actions == null
            ? ImmutableSortedSet.of(FlowDisposition.ACCEPTED)
            : ImmutableSortedSet.copyOf(actions);
  }

  public ReducedReachabilityQuestion() {
    this(null);
  }

  public SortedSet<FlowDisposition> getActions() {
    return _actions;
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
