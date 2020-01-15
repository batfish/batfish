package org.batfish.datamodel.trace;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.TraceElement;

/** A node in a trace */
@ParametersAreNonnullByDefault
public final class TraceNode {
  /** Builder for {@link TraceNode}. */
  public static class Builder {
    private @Nullable TraceElement _traceElement;
    private final List<TraceNode> _children = new ArrayList<>();

    public Builder setTraceElement(TraceElement traceElement) {
      _traceElement = traceElement;
      return this;
    }

    public Builder addChild(TraceNode child) {
      _children.add(child);
      return this;
    }

    public TraceNode build() {
      return new TraceNode(_traceElement, _children);
    }
  }

  private final @Nullable TraceElement _traceElement;
  private final @Nonnull List<TraceNode> _children;

  TraceNode(@Nullable TraceElement traceElement, List<TraceNode> children) {
    _traceElement = traceElement;
    _children = ImmutableList.copyOf(children);
  }

  public static Builder builder() {
    return new Builder();
  }

  @Nonnull
  public List<TraceNode> getChildren() {
    return _children;
  }

  @Nonnull
  public TraceElement getTraceElement() {
    return _traceElement;
  }
}
