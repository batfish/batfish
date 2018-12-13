package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

/** Match a specific interface name */
@ParametersAreNonnullByDefault
public final class MatchSourceInterface extends BooleanExpr {

  private static final long serialVersionUID = 1L;
  private static final String PROP_INTERFACE = "interface";

  @Nonnull private final String _srcInterface;

  @JsonCreator
  private static MatchSourceInterface create(
      @Nullable @JsonProperty(PROP_INTERFACE) String srcInterface) {
    checkArgument(!Strings.isNullOrEmpty(srcInterface), "Missing %s", PROP_INTERFACE);
    return new MatchSourceInterface(srcInterface);
  }

  public MatchSourceInterface(String srcInterface) {
    _srcInterface = srcInterface;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof MatchSourceInterface)) {
      return false;
    }
    MatchSourceInterface other = (MatchSourceInterface) obj;
    return Objects.equals(_srcInterface, other._srcInterface);
  }

  @Override
  public Result evaluate(Environment environment) {
    Result result = new Result();
    Interface iface = environment.getConfiguration().getAllInterfaces().get(_srcInterface);
    if (iface == null) {
      // No such interface, return false
      result.setBooleanValue(false);
      return result;
    }

    // Check that route's network equals to the connected subnet of the interface
    result.setBooleanValue(
        environment.getOriginalRoute().getNetwork().equals(iface.getPrimaryNetwork()));
    return result;
  }

  @JsonProperty(PROP_INTERFACE)
  public String getInterfaceName() {
    return _srcInterface;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_srcInterface);
  }
}
