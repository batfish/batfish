package org.batfish.specifier;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.IpAccessList;

/** A {@link FilterSpecifier} that matches no filters. */
@ParametersAreNonnullByDefault
public final class NoFiltersFilterSpecifier implements FilterSpecifier {
  public static final NoFiltersFilterSpecifier INSTANCE = new NoFiltersFilterSpecifier();

  private NoFiltersFilterSpecifier() {}

  @Override
  public boolean equals(@Nullable Object obj) {
    return obj instanceof NoFiltersFilterSpecifier;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  @Override
  public Set<IpAccessList> resolve(String node, SpecifierContext ctxt) {
    return ImmutableSet.of();
  }
}
