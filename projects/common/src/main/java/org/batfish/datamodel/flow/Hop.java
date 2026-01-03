package org.batfish.datamodel.flow;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.pojo.Node;

/** Represents a hop in a particular {@link Trace} of a {@link Flow} */
public final class Hop {
  private static final String PROP_NODE = "node";
  private static final String PROP_STEPS = "steps";

  /** Name of the node for this {@link Hop} */
  private final @Nonnull Node _node;

  /** {@link List} of {@link Step} present for the given {@link Hop} */
  private final @Nonnull List<Step<?>> _steps;

  public Hop(Node node, List<Step<?>> steps) {
    _node = node;
    _steps = ImmutableList.copyOf(steps);
  }

  @JsonCreator
  private static Hop jsonCreator(
      @JsonProperty(PROP_NODE) @Nullable Node node,
      @JsonProperty(PROP_STEPS) @Nullable List<Step<?>> steps) {
    checkArgument(node != null, "Hop should be have a node present");
    return new Hop(node, firstNonNull(steps, ImmutableList.of()));
  }

  @JsonProperty(PROP_NODE)
  public @Nonnull Node getNode() {
    return _node;
  }

  @JsonProperty(PROP_STEPS)
  public @Nonnull List<Step<?>> getSteps() {
    return _steps;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof Hop)) {
      return false;
    }
    Hop other = (Hop) o;
    return _node.equals(other._node) && _steps.equals(other._steps);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_node, _steps);
  }
}
