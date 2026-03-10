package org.batfish.datamodel.trace;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
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

  private final @Nullable TraceElement _traceElement;
  private final @Nonnull List<TraceTree> _children;

  TraceTree(TraceElement traceElement, List<TraceTree> children) {
    _traceElement = traceElement;
    _children = ImmutableList.copyOf(children);
  }

  @JsonCreator
  private static TraceTree jsonCreator(
      @JsonProperty(PROP_TRACE_ELEMENT) @Nullable TraceElement traceElement,
      @JsonProperty(PROP_CHILDREN) @Nullable List<TraceTree> children) {
    checkNotNull(traceElement, "%s cannot be null", PROP_TRACE_ELEMENT);
    return new TraceTree(traceElement, firstNonNull(children, ImmutableList.of()));
  }

  @JsonProperty(PROP_CHILDREN)
  public @Nonnull List<TraceTree> getChildren() {
    return _children;
  }

  @JsonProperty(PROP_TRACE_ELEMENT)
  public @Nullable TraceElement getTraceElement() {
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

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .omitNullValues()
        .add("traceElement", _traceElement)
        .add("children", _children)
        .toString();
  }
}
