package org.batfish.datamodel.flow;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableSet;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Flow;

/**
 * A {@link Trace flow trace} and a {@link Flow} for the reverse direction, if one exists for the
 * trace. Traces that end in a {@link org.batfish.datamodel.FlowDisposition#SUCCESS_DISPOSITIONS
 * success disposition} have reverse flows; traces that end in a {@link
 * org.batfish.datamodel.FlowDisposition#FAILURE_DISPOSITIONS failure disposition} do not.
 */
@ParametersAreNonnullByDefault
public final class TraceAndReverseFlow {
  private final @Nonnull Trace _trace;
  private final @Nullable Flow _reverseFlow;
  private final @Nonnull Set<FirewallSessionTraceInfo> _newFirewallSessions;

  public TraceAndReverseFlow(
      @Nonnull Trace trace,
      @Nullable Flow reverseFlow,
      @Nonnull Iterable<FirewallSessionTraceInfo> newFirewallSessions) {
    checkArgument(
        !trace.getDisposition().isSuccessful() ^ reverseFlow != null,
        "reverseFlow should be present if and only if Trace is successful");
    _trace = trace;
    _reverseFlow = reverseFlow;
    _newFirewallSessions = ImmutableSet.copyOf(newFirewallSessions);
  }

  public @Nullable Flow getReverseFlow() {
    return _reverseFlow;
  }

  public @Nonnull Trace getTrace() {
    return _trace;
  }

  /** Return the new firewall sessions initialized by the trace. */
  public @Nonnull Set<FirewallSessionTraceInfo> getNewFirewallSessions() {
    return _newFirewallSessions;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof TraceAndReverseFlow)) {
      return false;
    }
    TraceAndReverseFlow that = (TraceAndReverseFlow) o;
    return _trace.equals(that._trace)
        && Objects.equals(_reverseFlow, that._reverseFlow)
        && _newFirewallSessions.equals(that._newFirewallSessions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_trace, _reverseFlow, _newFirewallSessions);
  }
}
