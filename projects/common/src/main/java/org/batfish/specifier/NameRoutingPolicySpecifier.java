package org.batfish.specifier;

import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.routing_policy.RoutingPolicy;

/** A {@link RoutingPolicySpecifier} that matches RoutingPolicy names (case insensitive). */
@ParametersAreNonnullByDefault
public final class NameRoutingPolicySpecifier implements RoutingPolicySpecifier {
  private final @Nonnull String _name;

  public NameRoutingPolicySpecifier(String name) {
    _name = name;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NameRoutingPolicySpecifier)) {
      return false;
    }
    NameRoutingPolicySpecifier that = (NameRoutingPolicySpecifier) o;
    return Objects.equals(_name, that._name);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_name);
  }

  @Override
  public Set<RoutingPolicy> resolve(String node, SpecifierContext ctxt) {
    return ctxt.getConfigs().values().stream()
        .filter(c -> c.getHostname().equalsIgnoreCase(node))
        .map(c -> c.getRoutingPolicies().values())
        .flatMap(Collection::stream)
        .filter(f -> f.getName().equalsIgnoreCase(_name))
        .collect(ImmutableSet.toImmutableSet());
  }
}
