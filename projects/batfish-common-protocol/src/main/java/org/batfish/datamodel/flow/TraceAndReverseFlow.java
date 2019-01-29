package org.batfish.datamodel.flow;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.specifier.DispositionSpecifier.SUCCESS_DISPOSITIONS;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Flow;
import org.batfish.specifier.DispositionSpecifier;

/**
 * A {@link Trace flow trace} and a {@link Flow} for the reverse direction, if one exists for the
 * trace. Traces that end {@link DispositionSpecifier#SUCCESS successfully} have reverse flows;
 * traces that {@link DispositionSpecifier#FAILURE fail} do not.
 */
@ParametersAreNonnullByDefault
public final class TraceAndReverseFlow {
  private final @Nonnull Trace _trace;
  private final @Nullable Flow _reverseFlow;
  private final @Nonnull Set<FirewallSessionTraceInfo> _newFirewallSessions;

  public TraceAndReverseFlow(@Nonnull Trace trace, @Nullable Flow reverseFlow) {
    this(trace, reverseFlow, ImmutableSet.of());
  }

  public TraceAndReverseFlow(
      @Nonnull Trace trace,
      @Nullable Flow reverseFlow,
      @Nonnull Iterable<FirewallSessionTraceInfo> newFirewallSessions) {
    checkArgument(
        !SUCCESS_DISPOSITIONS.contains(trace.getDisposition()) ^ reverseFlow != null,
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
}
