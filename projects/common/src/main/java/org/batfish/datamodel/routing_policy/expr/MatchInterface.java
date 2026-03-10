package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

/**
 * Matches routes that have next hop as one of the configured interfaces.
 *
 * <p>Note: this only matches interfaces in the route, not resolved interface.
 */
@ParametersAreNonnullByDefault
public final class MatchInterface extends BooleanExpr {
  private static final String PROP_INTERFACES = "interfaces";

  private final @Nonnull Set<String> _interfaces;

  @JsonCreator
  private static MatchInterface jsonCreator(
      @JsonProperty(PROP_INTERFACES) @Nullable Set<String> interfaces) {
    return new MatchInterface(firstNonNull(interfaces, ImmutableSet.of()));
  }

  public MatchInterface(Iterable<String> interfaces) {
    _interfaces = ImmutableSet.copyOf(interfaces);
  }

  @Override
  public <T, U> T accept(BooleanExprVisitor<T, U> visitor, U arg) {
    return visitor.visitMatchInterface(this, arg);
  }

  @Override
  public Result evaluate(Environment environment) {
    String iname =
        environment.getUseOutputAttributes()
            ? environment.getOutputRoute().getNextHopInterface()
            : environment.getOriginalRoute().getNextHopInterface();
    boolean matches = !iname.equals(Route.UNSET_NEXT_HOP_INTERFACE) && _interfaces.contains(iname);
    return Result.builder().setBooleanValue(matches).build();
  }

  @JsonProperty(PROP_INTERFACES)
  public @Nonnull Set<String> getInterfaces() {
    return _interfaces;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof MatchInterface)) {
      return false;
    }
    MatchInterface other = (MatchInterface) obj;
    return _interfaces.equals(other._interfaces);
  }

  @Override
  public int hashCode() {
    return _interfaces.hashCode();
  }
}
