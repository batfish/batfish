package org.batfish.datamodel.trace;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.TraceElement;

/**
 * A class for building Trace trees. The tracer tracks a current trace, which is updated by creating
 * subtraces, and then completing or discarding them. Clients can set a {@link TraceElement} for the
 * current (sub)trace.
 */
public final class Tracer {

  // the complete trace. once set, can't create new subtraces
  private @Nullable List<TraceTree> _trace;

  // invariant: never empty
  private final Stack<TraceContext> _stack;

  /** Builder for {@link TraceTree}. */
  private static final class TraceContext {
    private @Nullable TraceElement _traceElement;
    private final List<TraceTree> _children = new ArrayList<>();

    void setTraceElement(@Nonnull TraceElement traceElement) {
      _traceElement = traceElement;
    }

    void addChild(TraceTree child) {
      _children.add(child);
    }

    @Nullable
    TraceElement getTraceElement() {
      return _traceElement;
    }

    List<TraceTree> getChildren() {
      return _children;
    }
  }

  public Tracer() {
    _stack = new Stack<>();
  }

  public List<TraceTree> getTrace() {
    checkState(_trace != null, "cannot get incomplete trace");
    return _trace;
  }

  /** Set the {@link TraceElement} for the current trace node. Must not already be set. */
  public void setTraceElement(@Nonnull TraceElement traceElement) {
    checkState(!_stack.isEmpty(), "no trace in progress");
    TraceContext context = _stack.peek();
    checkState(context.getTraceElement() == null, "TraceElement already set");
    context.setTraceElement(traceElement);
  }

  /**
   * Start a new trace at the current depth level. Indicates jump in a level of indirection to a new
   * structure (even though said structure can still be part of a single ACL line.
   */
  public void newSubTrace() {
    checkState(_trace == null, "trace already completed");
    // Add new child, set it as current node
    _stack.push(new TraceContext());
  }

  /** Complete the current (sub)trace. If it's a subtrace, add it as a child of the parent trace. */
  public void endSubTrace() {
    checkState(!_stack.isEmpty(), "no trace in progress");
    TraceContext context = _stack.pop();
    if (!_stack.isEmpty()) {
      if (context.getTraceElement() != null) {
        _stack.peek().addChild(new TraceTree(context.getTraceElement(), context.getChildren()));
      } else {
        context.getChildren().forEach(_stack.peek()::addChild);
      }
    } else {
      if (context.getTraceElement() == null) {
        _trace = ImmutableList.copyOf(context.getChildren());
      } else {
        _trace = ImmutableList.of(new TraceTree(context.getTraceElement(), context.getChildren()));
      }
    }
  }

  /**
   * Discard the current (sub)trace. Returns to the parent trace (if one exists), and does not add
   * the current subtrace as a child.
   */
  public void discardSubTrace() {
    checkState(!_stack.isEmpty(), "no trace in progress");
    _stack.pop();
  }
}
