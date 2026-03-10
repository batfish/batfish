package org.batfish.datamodel.flow;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableSet;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Flow;

/** A pair of forward and reverse {@link Trace Traces}. */
@ParametersAreNonnullByDefault
public final class BidirectionalTrace {
  private final @Nonnull Flow _forwardFlow;
  private final @Nonnull Trace _forwardTrace;
  private final @Nonnull Set<FirewallSessionTraceInfo> _newSessions;
  private final @Nullable Flow _reverseFlow;
  private final @Nullable Trace _reverseTrace;

  public BidirectionalTrace(
      Flow forwardFlow,
      Trace forwardTrace,
      @Nonnull Set<FirewallSessionTraceInfo> newSessions,
      @Nullable Flow reverseFlow,
      @Nullable Trace reverseTrace) {
    checkArgument(
        forwardTrace.getDisposition().isSuccessful() == (reverseFlow != null),
        "reverseFlow should be present if and only if Trace is successful");
    checkArgument(
        (reverseFlow == null) == (reverseTrace == null),
        "reverseFlow should be present if and only if reverseTrace is present");
    _forwardFlow = forwardFlow;
    _forwardTrace = forwardTrace;
    _newSessions = ImmutableSet.copyOf(newSessions);
    _reverseFlow = reverseFlow;
    _reverseTrace = reverseTrace;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof BidirectionalTrace)) {
      return false;
    }
    BidirectionalTrace that = (BidirectionalTrace) o;
    return _forwardFlow.equals(that._forwardFlow)
        && _forwardTrace.equals(that._forwardTrace)
        && _newSessions.equals(that._newSessions)
        && Objects.equals(_reverseFlow, that._reverseFlow)
        && Objects.equals(_reverseTrace, that._reverseTrace);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_forwardFlow, _forwardTrace, _newSessions, _reverseFlow, _reverseTrace);
  }

  public @Nonnull Flow getForwardFlow() {
    return _forwardFlow;
  }

  public @Nonnull Trace getForwardTrace() {
    return _forwardTrace;
  }

  public @Nonnull Set<FirewallSessionTraceInfo> getNewSessions() {
    return _newSessions;
  }

  public @Nullable Flow getReverseFlow() {
    return _reverseFlow;
  }

  public @Nullable Trace getReverseTrace() {
    return _reverseTrace;
  }
}
