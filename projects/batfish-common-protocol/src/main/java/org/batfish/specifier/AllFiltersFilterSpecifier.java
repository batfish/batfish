package org.batfish.specifier;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.IpAccessList;

/** A {@link FilterSpecifier} that matches all filters. */
@ParametersAreNonnullByDefault
public final class AllFiltersFilterSpecifier implements FilterSpecifier {
  public static final AllFiltersFilterSpecifier INSTANCE = new AllFiltersFilterSpecifier();

  private AllFiltersFilterSpecifier() {}

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    return obj instanceof AllFiltersFilterSpecifier;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  @Override
  public Set<IpAccessList> resolve(String node, SpecifierContext ctxt) {
    return ctxt.getConfigs().values().stream()
        .filter(c -> c.getHostname().equalsIgnoreCase(node))
        .map(c -> c.getIpAccessLists().values())
        .flatMap(Collection::stream)
        .collect(Collectors.toSet());
  }
}
