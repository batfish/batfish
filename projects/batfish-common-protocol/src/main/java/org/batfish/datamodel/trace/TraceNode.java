package org.batfish.datamodel.trace;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.acl.TraceEvent;

/** A node in a trace */
@ParametersAreNonnullByDefault
public final class TraceNode {
  /** Builder for {@link TraceNode}. */
  public static class Builder {
    private @Nullable TraceEvent _traceEvent;
    private final List<TraceNode> _children = new ArrayList<>();

    public Builder setTraceEvent(TraceEvent traceEvent) {
      _traceEvent = traceEvent;
      return this;
    }

    public Builder addChild(TraceNode child) {
      _children.add(child);
      return this;
    }

    public TraceNode build() {
      return new TraceNode(_traceEvent, _children);
    }
  }

  private final @Nullable TraceEvent _traceEvent;
  private final @Nonnull List<TraceNode> _children;

  TraceNode(@Nullable TraceEvent traceEvent, List<TraceNode> children) {
    _traceEvent = traceEvent;
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
  public TraceEvent getTraceEvent() {
    return _traceEvent;
  }
}
