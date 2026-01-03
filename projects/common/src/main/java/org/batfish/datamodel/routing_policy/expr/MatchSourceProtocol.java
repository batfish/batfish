package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.HasReadableSourceProtocol;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

/**
 * Boolean expression that evaluates whether an {@link Environment} has a {@link
 * HasReadableSourceProtocol route} that matches a given source {@link RoutingProtocol}.
 */
@ParametersAreNonnullByDefault
public final class MatchSourceProtocol extends BooleanExpr {
  private static final String PROP_PROTOCOLS = "protocols";

  private final @Nonnull EnumSet<RoutingProtocol> _protocols;

  public MatchSourceProtocol(RoutingProtocol... protocols) {
    this(Arrays.asList(protocols));
  }

  public MatchSourceProtocol(Collection<RoutingProtocol> protocols) {
    checkArgument(!protocols.isEmpty(), "Must match at least 1 protocol");
    _protocols = EnumSet.copyOf(protocols);
  }

  @JsonCreator
  private static MatchSourceProtocol create(
      @JsonProperty(PROP_PROTOCOLS) @Nullable Set<RoutingProtocol> protocols) {
    checkArgument(protocols != null, "Missing %s", PROP_PROTOCOLS);
    return new MatchSourceProtocol(protocols);
  }

  @Override
  public <T, U> T accept(BooleanExprVisitor<T, U> visitor, U arg) {
    return visitor.visitMatchSourceProtocol(this, arg);
  }

  @Override
  public Result evaluate(Environment environment) {
    AbstractRoute route = environment.getOriginalRoute();
    if (route instanceof HasReadableSourceProtocol) {
      RoutingProtocol protocol = ((HasReadableSourceProtocol) route).getSrcProtocol();
      return new Result(protocol != null && _protocols.contains(protocol));
    }
    // TODO: what to do here?
    return new Result(false);
  }

  @JsonProperty(PROP_PROTOCOLS)
  public @Nonnull Set<RoutingProtocol> getProtocols() {
    return _protocols;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof MatchSourceProtocol)) {
      return false;
    }
    MatchSourceProtocol that = (MatchSourceProtocol) o;
    return _protocols.equals(that._protocols);
  }

  @Override
  public int hashCode() {
    // Consistent hash for a set of enums: the Set hashcode algorithm but on ordinal value.
    int hash = 0;
    for (RoutingProtocol p : _protocols) {
      hash += p.ordinal();
    }
    return hash;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(MatchSourceProtocol.class)
        .add(PROP_PROTOCOLS, _protocols)
        .toString();
  }
}
