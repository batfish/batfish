package org.batfish.datamodel.routing_policy.statement;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.bgp.TunnelEncapsulationAttribute;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.expr.TunnelEncapsulationAttributeExpr;

/** Sets {@link TunnelEncapsulationAttribute} on a BGP route */
@ParametersAreNonnullByDefault
public final class SetTunnelEncapsulationAttribute extends Statement {
  private static final String PROP_TUNNEL_ENCAPSULATION_ATTRIBUTE_EXPR =
      "tunnelEncapsulationAttributeExpr";

  private final @Nonnull TunnelEncapsulationAttributeExpr _expr;

  @JsonCreator
  private static SetTunnelEncapsulationAttribute jsonCreator(
      @JsonProperty(PROP_TUNNEL_ENCAPSULATION_ATTRIBUTE_EXPR) @Nullable
          TunnelEncapsulationAttributeExpr expr) {
    checkArgument(expr != null, "Missing %s", PROP_TUNNEL_ENCAPSULATION_ATTRIBUTE_EXPR);
    return new SetTunnelEncapsulationAttribute(expr);
  }

  public SetTunnelEncapsulationAttribute(@Nonnull TunnelEncapsulationAttributeExpr expr) {
    _expr = expr;
  }

  @Override
  public <T, U> T accept(StatementVisitor<T, U> visitor, U arg) {
    return visitor.visitSetTunnelEncapsulationAttribute(this, arg);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SetTunnelEncapsulationAttribute)) {
      return false;
    }
    SetTunnelEncapsulationAttribute other = (SetTunnelEncapsulationAttribute) o;
    return _expr.equals(other._expr);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_expr);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("expr", _expr).toString();
  }

  @Override
  public @Nonnull Result execute(Environment environment) {
    if (!(environment.getOutputRoute() instanceof BgpRoute.Builder<?, ?>)) {
      // Do nothing for non-BGP routes
      return new Result();
    }
    BgpRoute.Builder<?, ?> outputRoute = (BgpRoute.Builder<?, ?>) environment.getOutputRoute();
    TunnelEncapsulationAttribute tunnelEncapsulationAttribute = _expr.evaluate(environment);
    if (tunnelEncapsulationAttribute == null) {
      // TODO Currently this shouldn't be possible. If it becomes possible, update with comment.
      return new Result();
    }
    outputRoute.setTunnelEncapsulationAttribute(tunnelEncapsulationAttribute);
    if (environment.getWriteToIntermediateBgpAttributes()) {
      environment
          .getIntermediateBgpAttributes()
          .setTunnelEncapsulationAttribute(tunnelEncapsulationAttribute);
    }
    return new Result();
  }

  @JsonProperty(PROP_TUNNEL_ENCAPSULATION_ATTRIBUTE_EXPR)
  public TunnelEncapsulationAttributeExpr getTunnelEncapsulationAttributeExpr() {
    return _expr;
  }
}
