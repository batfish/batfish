package org.batfish.datamodel.trace;

import static com.google.common.base.Preconditions.checkState;

import java.util.Stack;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.acl.TraceEvent;

/**
 * A class for building Trace trees. The tracer tracks a current trace, which is updated by creating
 * subtraces, and then completing or discarding them. Clients can set a {@link TraceEvent} for the
 * current (sub)trace.
 */
public class Tracer {

  // the complete trace. once set, can't create new subtraces
  private @Nullable TraceNode _trace;

  // invariant: never empty
  private final Stack<TraceNode.Builder> _nodeStack;

  public Tracer() {
    _nodeStack = new Stack<>();
  }

  public TraceNode getTrace() {
    checkState(_trace != null, "cannot get incomplete trace");
    return _trace;
  }

  /** Set the {@link TraceEvent} for the current trace node. Must not already be set. */
  public void setEvent(@Nonnull TraceEvent traceEvent) {
    checkState(!_nodeStack.isEmpty(), "no trace in progress");
    TraceNode.Builder currentNode = _nodeStack.peek();
    currentNode.setTraceEvent(traceEvent);
  }

  /**
   * Start a new trace at the current depth level. Indicates jump in a level of indirection to a new
   * structure (even though said structure can still be part of a single ACL line.
   */
  public void newSubTrace() {
    checkState(_trace == null, "trace already completed");
    // Add new child, set it as current node
    _nodeStack.push(TraceNode.builder());
  }

  /** Complete the current (sub)trace. If it's a subtrace, add it as a child of the parent trace. */
  public void endSubTrace() {
    checkState(!_nodeStack.isEmpty(), "no trace in progress");
    TraceNode child = _nodeStack.pop().build();
    if (!_nodeStack.isEmpty()) {
      _nodeStack.peek().addChild(child);
    } else {
      _trace = child;
    }
  }

  /**
   * Discard the current (sub)trace. Returns to the parent trace (if one exists), and does not add
   * the current subtrace as a child.
   */
  public void discardSubTrace() {
    checkState(!_nodeStack.isEmpty(), "no trace in progress");
    _nodeStack.pop();
    newSubTrace();
  }
}
