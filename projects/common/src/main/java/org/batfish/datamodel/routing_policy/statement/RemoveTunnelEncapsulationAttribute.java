package org.batfish.datamodel.routing_policy.statement;

import com.fasterxml.jackson.annotation.JsonCreator;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

/** Removes {@link org.batfish.datamodel.bgp.TunnelEncapsulationAttribute} on a BGP route */
@ParametersAreNonnullByDefault
public final class RemoveTunnelEncapsulationAttribute extends Statement {
  private static final RemoveTunnelEncapsulationAttribute INSTANCE =
      new RemoveTunnelEncapsulationAttribute();

  @JsonCreator
  public static @Nonnull RemoveTunnelEncapsulationAttribute instance() {
    return INSTANCE;
  }

  @Override
  public <T, U> T accept(StatementVisitor<T, U> visitor, U arg) {
    return visitor.visitRemoveTunnelEncapsulationAttribute(this, arg);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    return o instanceof RemoveTunnelEncapsulationAttribute;
  }

  @Override
  public int hashCode() {
    return getClass().getCanonicalName().hashCode();
  }

  @Override
  public @Nonnull Result execute(Environment environment) {
    if (!(environment.getOutputRoute() instanceof BgpRoute.Builder<?, ?>)) {
      // Do nothing for non-BGP routes
      return new Result();
    }
    BgpRoute.Builder<?, ?> outputRoute = (BgpRoute.Builder<?, ?>) environment.getOutputRoute();
    outputRoute.setTunnelEncapsulationAttribute(null);
    if (environment.getWriteToIntermediateBgpAttributes()) {
      environment.getIntermediateBgpAttributes().setTunnelEncapsulationAttribute(null);
    }
    return new Result();
  }

  private RemoveTunnelEncapsulationAttribute() {}

  @Override
  public String toString() {
    return RemoveTunnelEncapsulationAttribute.class.getSimpleName();
  }
}
