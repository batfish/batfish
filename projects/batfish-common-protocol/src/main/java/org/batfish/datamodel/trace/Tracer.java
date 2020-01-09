package org.batfish.datamodel.trace;

import static com.google.common.base.Preconditions.checkState;

import java.util.Stack;
import javax.annotation.Nonnull;
import org.batfish.datamodel.acl.TraceEvent;
import org.batfish.datamodel.trace.TraceNode.Builder;

/** A class for building Traces. */
public class Tracer {

  // invariant: never empty
  private final Stack<TraceNode.Builder> _nodeStack;

  public Tracer() {
    _nodeStack = new Stack<>();
    _nodeStack.push(TraceNode.builder());
  }

  public TraceNode getRootNode() {
    checkState(!_nodeStack.isEmpty(), "Trace is missing");
    return _nodeStack.get(0).build();
  }

  /** Set the {@link TraceEvent} for the current trace node. Must not already be set. */
  public void setEvent(@Nonnull TraceEvent traceEvent) {
    TraceNode.Builder currentNode = _nodeStack.peek();
    currentNode.setTraceEvent(traceEvent);
  }

  /**
   * Start a new trace at the current depth level. Indicates jump in a level of indirection to a new
   * structure (even though said structure can still be part of a single ACL line.
   */
  public void newSubTrace() {
    // Add new child, set it as current node
    _nodeStack.push(TraceNode.builder());
  }

  /** End a trace: indicates that tracing of a structure is finished. */
  public void endSubTrace() {
    // make sure we're ending a subtrace, not the root trace.
    checkState(_nodeStack.size() > 1, "Not in a subTrace");
    // Go up level of a tree, do not delete children
    TraceNode child = _nodeStack.pop().build();
    _nodeStack.peek().addChild(child);
  }

  /** Clear the current subtrace. */
  public void resetSubTrace() {
    _nodeStack.pop();
    newSubTrace();
  }
}
