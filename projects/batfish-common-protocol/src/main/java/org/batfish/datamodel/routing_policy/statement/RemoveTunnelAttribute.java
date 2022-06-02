package org.batfish.datamodel.routing_policy.statement;

import com.fasterxml.jackson.annotation.JsonCreator;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.TunnelAttribute;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

/** Removes {@link TunnelAttribute} on a BGP route */
@ParametersAreNonnullByDefault
public final class RemoveTunnelAttribute extends Statement {
  private static final RemoveTunnelAttribute INSTANCE = new RemoveTunnelAttribute();

  @JsonCreator
  public static @Nonnull RemoveTunnelAttribute instance() {
    return INSTANCE;
  }

  @Override
  public <T, U> T accept(StatementVisitor<T, U> visitor, U arg) {
    return visitor.visitRemoveTunnelAttribute(this, arg);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    return o instanceof RemoveTunnelAttribute;
  }

  @Override
  public int hashCode() {
    return getClass().getCanonicalName().hashCode();
  }

  @Override
  @Nonnull
  public Result execute(Environment environment) {
    if (!(environment.getOutputRoute() instanceof BgpRoute.Builder<?, ?>)) {
      // Do nothing for non-BGP routes
      return new Result();
    }
    BgpRoute.Builder<?, ?> outputRoute = (BgpRoute.Builder<?, ?>) environment.getOutputRoute();
    outputRoute.setTunnelAttribute(null);
    if (environment.getWriteToIntermediateBgpAttributes()) {
      environment.getIntermediateBgpAttributes().setTunnelAttribute(null);
    }
    return new Result();
  }

  private RemoveTunnelAttribute() {}
}
