package org.batfish.specifier;

import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.routing_policy.RoutingPolicy;

/** A {@link RoutingPolicySpecifier} that matches routing policy names based on a regex pattern. */
@ParametersAreNonnullByDefault
public final class NameRegexRoutingPolicySpecifier implements RoutingPolicySpecifier {
  public static final NameRegexRoutingPolicySpecifier ALL_ROUTING_POLICIES =
      new NameRegexRoutingPolicySpecifier(Pattern.compile(".*"));

  private final @Nonnull Pattern _pattern;

  public NameRegexRoutingPolicySpecifier(Pattern pattern) {
    _pattern = pattern;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NameRegexRoutingPolicySpecifier)) {
      return false;
    }
    NameRegexRoutingPolicySpecifier that = (NameRegexRoutingPolicySpecifier) o;
    return Objects.equals(_pattern.pattern(), that._pattern.pattern());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_pattern.pattern());
  }

  @Override
  public Set<RoutingPolicy> resolve(String node, SpecifierContext ctxt) {
    return ctxt.getConfigs().values().stream()
        .filter(c -> c.getHostname().equalsIgnoreCase(node))
        .map(c -> c.getRoutingPolicies().values())
        .flatMap(Collection::stream)
        .filter(f -> _pattern.matcher(f.getName()).find())
        .collect(ImmutableSet.toImmutableSet());
  }
}
