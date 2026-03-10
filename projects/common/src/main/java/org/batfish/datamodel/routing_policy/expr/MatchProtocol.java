package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

/**
 * Boolean expression that evaluates whether an {@link Environment} has a route that matches a given
 * {@link RoutingProtocol}.
 */
@ParametersAreNonnullByDefault
public final class MatchProtocol extends BooleanExpr {
  private static final String PROP_PROTOCOLS = "protocols";

  private final @Nonnull Set<RoutingProtocol> _protocols;

  public MatchProtocol(RoutingProtocol... protocols) {
    checkArgument(protocols.length > 0, "Must match at least 1 protocol");
    _protocols = Arrays.stream(protocols).sorted().collect(ImmutableSet.toImmutableSet());
  }

  public MatchProtocol(Collection<RoutingProtocol> protocols) {
    checkArgument(!protocols.isEmpty(), "Must match at least 1 protocol");
    _protocols = protocols.stream().sorted().collect(ImmutableSet.toImmutableSet());
  }

  @JsonCreator
  private static MatchProtocol create(
      @JsonProperty(PROP_PROTOCOLS) @Nullable Set<RoutingProtocol> protocols) {
    checkArgument(protocols != null, "Missing %s", PROP_PROTOCOLS);
    return new MatchProtocol(protocols);
  }

  @Override
  public <T, U> T accept(BooleanExprVisitor<T, U> visitor, U arg) {
    return visitor.visitMatchProtocol(this, arg);
  }

  @Override
  public Result evaluate(Environment environment) {
    return new Result(_protocols.contains(environment.getOriginalRoute().getProtocol()));
  }

  @JsonProperty(PROP_PROTOCOLS)
  public @Nonnull Set<RoutingProtocol> getProtocols() {
    return _protocols;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof MatchProtocol)) {
      return false;
    }
    MatchProtocol that = (MatchProtocol) o;
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
    return MoreObjects.toStringHelper(MatchProtocol.class)
        .add(PROP_PROTOCOLS, _protocols)
        .toString();
  }
}
