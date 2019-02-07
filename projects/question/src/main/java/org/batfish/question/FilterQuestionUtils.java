package org.batfish.question;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.Map;
import java.util.Set;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IpAccessList;
import org.batfish.specifier.FilterSpecifier;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.SpecifierContext;

/** Utilities for questions about {@link IpAccessList Filters}. */
public final class FilterQuestionUtils {
  private FilterQuestionUtils() {}

  /** Get filters specified by the given filter specifier. */
  public static Multimap<String, String> getSpecifiedFilters(
      Map<String, Configuration> configs,
      NodeSpecifier nodeSpecifier,
      FilterSpecifier filterSpecifier,
      SpecifierContext specifierContext) {
    Set<String> nodes = nodeSpecifier.resolve(specifierContext);
    ImmutableMultimap.Builder<String, String> filters = ImmutableMultimap.builder();
    nodes.stream()
        .map(configs::get)
        .forEach(
            config ->
                filterSpecifier
                    .resolve(config.getHostname(), specifierContext)
                    .forEach(filter -> filters.put(config.getHostname(), filter.getName())));
    return filters.build();
  }
}
