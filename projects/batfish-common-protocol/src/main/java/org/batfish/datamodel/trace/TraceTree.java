package org.batfish.datamodel.trace;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.TraceElement;

/** A node in a trace */
@ParametersAreNonnullByDefault
public final class TraceTree {
  private static final String PROP_TRACE_ELEMENT = "traceElement";
  private static final String PROP_CHILDREN = "children";

  /** Builder for {@link TraceTree}. */
  public static class Builder {
    private @Nullable TraceElement _traceElement;
    private final List<TraceTree> _children = new ArrayList<>();

    public Builder setTraceElement(TraceElement traceElement) {
      _traceElement = traceElement;
      return this;
    }

    public Builder addChild(TraceTree child) {
      _children.add(child);
      return this;
    }

    public TraceTree build() {
      return new TraceTree(_traceElement, _children);
    }
  }

  private final @Nullable TraceElement _traceElement;
  private final @Nonnull List<TraceTree> _children;

  TraceTree(@Nullable TraceElement traceElement, List<TraceTree> children) {
    _traceElement = traceElement;
    _children = ImmutableList.copyOf(children);
  }

  @JsonCreator
  private static TraceTree jsonCreator(
      @Nullable @JsonProperty(PROP_TRACE_ELEMENT) TraceElement traceElement,
      @Nullable @JsonProperty(PROP_CHILDREN) List<TraceTree> children) {
    return new TraceTree(traceElement, firstNonNull(children, ImmutableList.of()));
  }

  public static Builder builder() {
    return new Builder();
  }

  @JsonProperty(PROP_CHILDREN)
  @Nonnull
  public List<TraceTree> getChildren() {
    return _children;
  }

  @JsonProperty(PROP_TRACE_ELEMENT)
  @Nullable
  public TraceElement getTraceElement() {
    return _traceElement;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof TraceTree)) {
      return false;
    }
    TraceTree other = (TraceTree) o;
    return Objects.equals(_traceElement, other._traceElement) && _children.equals(other._children);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_traceElement, _children);
  }
}
