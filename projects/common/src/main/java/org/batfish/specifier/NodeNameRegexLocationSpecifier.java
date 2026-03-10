package org.batfish.specifier;

import com.google.common.collect.ImmutableSet;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.batfish.datamodel.Configuration;

public abstract class NodeNameRegexLocationSpecifier implements LocationSpecifier {
  private final Pattern _pattern;

  public NodeNameRegexLocationSpecifier(Pattern pattern) {
    _pattern = pattern;
  }

  protected abstract Stream<Location> getNodeLocations(Configuration node);

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NodeNameRegexLocationSpecifier that = (NodeNameRegexLocationSpecifier) o;
    return Objects.equals(_pattern.pattern(), that._pattern.pattern());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_pattern);
  }

  @Override
  public Set<Location> resolve(SpecifierContext ctxt) {
    return ctxt.getConfigs().entrySet().stream()
        .filter(entry -> _pattern.matcher(entry.getKey()).matches())
        .map(Entry::getValue)
        .flatMap(this::getNodeLocations)
        .collect(ImmutableSet.toImmutableSet());
  }
}
