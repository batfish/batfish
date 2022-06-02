package org.batfish.datamodel.routing_policy.statement;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.TunnelAttribute;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

/** Sets {@link org.batfish.datamodel.TunnelAttribute} on a BGP route */
@ParametersAreNonnullByDefault
public final class SetTunnelAttribute extends Statement {
  private static final String PROP_NAME = "name";

  @Nonnull private final String _name;

  @JsonCreator
  private static SetTunnelAttribute jsonCreator(@Nullable @JsonProperty(PROP_NAME) String name) {
    checkArgument(name != null, "Missing %s", PROP_NAME);
    return new SetTunnelAttribute(name);
  }

  public SetTunnelAttribute(String tunnelAttrName) {
    _name = tunnelAttrName;
  }

  @Override
  public <T, U> T accept(StatementVisitor<T, U> visitor, U arg) {
    return visitor.visitSetTunnelAttribute(this, arg);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SetTunnelAttribute)) {
      return false;
    }
    SetTunnelAttribute other = (SetTunnelAttribute) o;
    return _name.equals(other._name);
  }

  @Override
  public int hashCode() {
    return _name.hashCode();
  }

  @Override
  @Nonnull
  public Result execute(Environment environment) {
    if (!(environment.getOutputRoute() instanceof BgpRoute.Builder<?, ?>)) {
      // Do nothing for non-BGP routes
      return new Result();
    }
    BgpRoute.Builder<?, ?> outputRoute = (BgpRoute.Builder<?, ?>) environment.getOutputRoute();
    TunnelAttribute tunnelAttribute = environment.getTunnelAttributes().get(_name);
    if (tunnelAttribute == null) {
      return new Result();
    }
    outputRoute.setTunnelAttribute(tunnelAttribute);
    if (environment.getWriteToIntermediateBgpAttributes()) {
      environment.getIntermediateBgpAttributes().setTunnelAttribute(tunnelAttribute);
    }
    return new Result();
  }

  @JsonProperty(PROP_NAME)
  public String getTunnelAttributeName() {
    return _name;
  }
}
