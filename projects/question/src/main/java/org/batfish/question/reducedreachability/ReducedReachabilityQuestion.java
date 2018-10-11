package org.batfish.question.reducedreachability;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSortedSet;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.PacketHeaderConstraints;
import org.batfish.datamodel.questions.Question;

/** A zero-input question to check for reduced reachability between base and delta snapshots. */
public final class ReducedReachabilityQuestion extends Question {
  private static final String PROP_ACTIONS = "actions";
  private static final String PROP_HEADERS = "headers";

  @Nonnull private final SortedSet<FlowDisposition> _actions;
  @Nonnull private final PacketHeaderConstraints _headerConstraints;

  @JsonCreator
  public ReducedReachabilityQuestion(
      @Nullable @JsonProperty(PROP_ACTIONS) SortedSet<FlowDisposition> actions,
      @Nullable @JsonProperty(PROP_HEADERS) PacketHeaderConstraints headerConstraints) {
    setDifferential(true);
    _actions =
        actions == null
            ? ImmutableSortedSet.of(FlowDisposition.NEIGHBOR_UNREACHABLE_OR_EXITS_NETWORK)
            : ImmutableSortedSet.copyOf(actions);
    _headerConstraints = firstNonNull(headerConstraints, PacketHeaderConstraints.unconstrained());
  }

  public ReducedReachabilityQuestion() {
    this(null, null);
  }

  @Nonnull
  SortedSet<FlowDisposition> getActions() {
    return _actions;
  }

  @JsonProperty(PROP_HEADERS)
  @Nonnull
  PacketHeaderConstraints getHeaderConstraints() {
    return _headerConstraints;
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
