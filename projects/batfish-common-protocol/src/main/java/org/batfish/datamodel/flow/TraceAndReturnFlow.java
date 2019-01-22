package org.batfish.datamodel.flow;

import static org.batfish.specifier.DispositionSpecifier.SUCCESS_DISPOSITIONS;

import com.google.common.base.Preconditions;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Flow;

/**
 * A {@link Trace flow trace} and return {@link Flow}, if one exists for the trace. Traces that end
 * {@link org.batfish.specifier.DispositionSpecifier#SUCCESS successfully} have return flows; traces
 * that {@link org.batfish.specifier.DispositionSpecifier#FAILURE fail} do not.
 */
@ParametersAreNonnullByDefault
public final class TraceAndReturnFlow {
  private final @Nonnull Trace _trace;
  private final @Nullable Flow _returnFlow;

  public TraceAndReturnFlow(@Nonnull Trace trace, @Nullable Flow returnFlow) {
    Preconditions.checkArgument(
        !SUCCESS_DISPOSITIONS.contains(trace.getDisposition()) ^ returnFlow != null,
        "Flow may/must not be null if and only if Trace is successful");
    _trace = trace;
    _returnFlow = returnFlow;
  }

  public @Nullable Flow getReturnFlow() {
    return _returnFlow;
  }

  public @Nonnull Trace getTrace() {
    return _trace;
  }
}
