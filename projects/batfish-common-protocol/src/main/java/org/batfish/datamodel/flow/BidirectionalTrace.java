package org.batfish.datamodel.flow;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Flow;

/** A pair of forward and reverse {@link Trace Traces}. */
@ParametersAreNonnullByDefault
public final class BidirectionalTrace {
  private final @Nonnull Flow _forwardFlow;
  private final @Nonnull Trace _forwardTrace;
  private final @Nullable Flow _reverseFlow;
  private final @Nullable Trace _reverseTrace;

  public BidirectionalTrace(
      Flow forwardFlow,
      Trace forwardTrace,
      @Nullable Flow reverseFlow,
      @Nullable Trace reverseTrace) {
    _forwardFlow = forwardFlow;
    _forwardTrace = forwardTrace;
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
        && Objects.equals(_reverseFlow, that._reverseFlow)
        && Objects.equals(_reverseTrace, that._reverseTrace);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_forwardFlow, _forwardTrace, _reverseFlow, _reverseTrace);
  }

  public Flow getForwardFlow() {
    return _forwardFlow;
  }

  public Trace getForwardTrace() {
    return _forwardTrace;
  }

  public Flow getReverseFlow() {
    return _reverseFlow;
  }

  public Trace getReverseTrace() {
    return _reverseTrace;
  }
}
