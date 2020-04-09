package org.batfish.specifier;

import com.google.common.collect.ImmutableSet;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A {@link LocationSpecifier} specifying all {@link Location Locations} marked as a {@link
 * LocationInfo#isSource() source}.
 */
public final class AllSources implements LocationSpecifier {
  public static final AllSources ALL_SOURCES = new AllSources();

  private AllSources() {}

  @Override
  public Set<Location> resolve(SpecifierContext ctxt) {
    return ctxt.getLocationInfo().entrySet().stream()
        .filter(entry -> entry.getValue().isSource())
        .map(Entry::getKey)
        .collect(ImmutableSet.toImmutableSet());
  }
}
