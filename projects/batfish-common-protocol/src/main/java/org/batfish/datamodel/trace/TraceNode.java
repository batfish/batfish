package org.batfish.datamodel.trace;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.acl.TraceEvent;

/** A node in a trace */
public final class TraceNode {
  private @Nullable TraceEvent _traceEvent;
  private final @Nonnull List<TraceNode> _children;

  TraceNode() {
    _children = new ArrayList<>();
  }

  @Nonnull
  public List<TraceNode> getChildren() {
    return _children;
  }

  @Nullable
  public TraceEvent getTraceEvent() {
    return _traceEvent;
  }

  void setTraceEvent(@Nonnull TraceEvent traceEvent) {
    _traceEvent = traceEvent;
  }

  /** Adds a new child to this node trace node. Returns pointer to given node */
  TraceNode createChild() {
    TraceNode child = new TraceNode();
    _children.add(child);
    return child;
  }

  /** Clears all children from this node */
  void reset() {
    _traceEvent = null;
    _children.clear();
  }
}
