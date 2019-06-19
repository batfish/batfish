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

  public static final class Key {
    private final @Nonnull Flow _forwardFlow;
    private final @Nonnull Set<FirewallSessionTraceInfo> _newSessions;
    private final @Nullable Flow _reverseFlow;

    public Key(
        @Nonnull Flow forwardFlow,
        @Nonnull Set<FirewallSessionTraceInfo> newSessions,
        @Nullable Flow reverseFlow) {
      _forwardFlow = forwardFlow;
      _newSessions = ImmutableSet.copyOf(newSessions);
      _reverseFlow = reverseFlow;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Key)) {
        return false;
      }
      Key key = (Key) o;
      return Objects.equals(_forwardFlow, key._forwardFlow)
          && Objects.equals(_newSessions, key._newSessions)
          && Objects.equals(_reverseFlow, key._reverseFlow);
    }

    @Override
    public int hashCode() {
      return Objects.hash(_forwardFlow, _newSessions, _reverseFlow);
    }

    @Nonnull
    public Flow getForwardFlow() {
      return _forwardFlow;
    }

    @Nonnull
    public Set<FirewallSessionTraceInfo> getNewSessions() {
      return _newSessions;
    }

    @Nullable
    public Flow getReverseFlow() {
      return _reverseFlow;
    }
  }

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
    return Objects.equals(_forwardFlow, that._forwardFlow)
        && Objects.equals(_forwardTrace, that._forwardTrace)
        && Objects.equals(_newSessions, that._newSessions)
        && Objects.equals(_reverseFlow, that._reverseFlow)
        && Objects.equals(_reverseTrace, that._reverseTrace);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_forwardFlow, _forwardTrace, _newSessions, _reverseFlow, _reverseTrace);
  }

  @Nonnull
  public Flow getForwardFlow() {
    return _forwardFlow;
  }

  @Nonnull
  public Trace getForwardTrace() {
    return _forwardTrace;
  }

  @Nonnull
  public Key getKey() {
    return new Key(_forwardFlow, _newSessions, _reverseFlow);
  }

  @Nonnull
  public Set<FirewallSessionTraceInfo> getNewSessions() {
    return _newSessions;
  }

  @Nullable
  public Flow getReverseFlow() {
    return _reverseFlow;
  }

  @Nullable
  public Trace getReverseTrace() {
    return _reverseTrace;
  }
}
