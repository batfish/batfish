package org.batfish.datamodel.trace;

import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.acl.TraceEvent;

/** A class for building Traces. */
public class Tracer {

  /** A node in a trace */
  public static final class TraceNode {
    private @Nullable TraceEvent _traceEvent;
    private final @Nonnull List<TraceNode> _children;

    private TraceNode() {
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

    private void setTraceEvent(@Nonnull TraceEvent traceEvent) {
      _traceEvent = traceEvent;
    }

    /** Adds a new child to this node trace node. Returns pointer to given node */
    private TraceNode addChild() {
      TraceNode child = new TraceNode();
      _children.add(child);
      return child;
    }

    /** Clears all children from this node */
    public void reset() {
      _traceEvent = null;
      _children.clear();
    }
  }

  // invariant: never empty
  private final Stack<TraceNode> _nodeStack;

  public Tracer() {
    _nodeStack = new Stack<>();
    _nodeStack.push(new TraceNode());
  }

  public TraceNode getRootNode() {
    checkState(!_nodeStack.isEmpty(), "Trace is missing");
    return _nodeStack.get(0);
  }

  /** Set the {@link TraceEvent} for the current trace node. Must not already be set. */
  public void setEvent(@Nonnull TraceEvent traceEvent) {
    TraceNode currentNode = _nodeStack.peek();
    checkState(currentNode._traceEvent == null, "TraceNode event already set.");
    currentNode.setTraceEvent(traceEvent);
  }

  /**
   * Start a new trace at the current depth level. Indicates jump in a level of indirection to a new
   * structure (even though said structure can still be part of a single ACL line.
   */
  public void newSubTrace() {
    // Add new child, set it as current node
    _nodeStack.push(_nodeStack.peek().addChild());
  }

  /** End a trace: indicates that tracing of a structure is finished. */
  public void endSubTrace() {
    // make sure we're ending a subtrace, not the root trace.
    checkState(_nodeStack.size() > 1, "Not in a subTrace");
    // Go up level of a tree, do not delete children
    _nodeStack.pop();
  }

  /** Clear the current subtrace. */
  public void resetSubTrace() {
    _nodeStack.peek().reset();
  }
}
