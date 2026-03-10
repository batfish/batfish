package org.batfish.specifier;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
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
        .flatMap(c -> c.getIpAccessLists().values().stream())
        .collect(ImmutableSet.toImmutableSet());
  }
}
