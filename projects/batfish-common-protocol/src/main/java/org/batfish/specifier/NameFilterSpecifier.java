package org.batfish.specifier;

import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.IpAccessList;

/** A {@link FilterSpecifier} that matches filter names (case insensitive). */
@ParametersAreNonnullByDefault
public final class NameFilterSpecifier implements FilterSpecifier {
  private final @Nonnull String _name;

  public NameFilterSpecifier(String name) {
    _name = name;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NameFilterSpecifier)) {
      return false;
    }
    NameFilterSpecifier that = (NameFilterSpecifier) o;
    return Objects.equals(_name, that._name);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_name);
  }

  @Override
  public Set<IpAccessList> resolve(String node, SpecifierContext ctxt) {
    return ctxt.getConfigs().values().stream()
        .filter(c -> c.getHostname().equalsIgnoreCase(node))
        .map(c -> c.getIpAccessLists().values())
        .flatMap(Collection::stream)
        .filter(f -> f.getName().equalsIgnoreCase(_name))
        .collect(ImmutableSet.toImmutableSet());
  }
}
