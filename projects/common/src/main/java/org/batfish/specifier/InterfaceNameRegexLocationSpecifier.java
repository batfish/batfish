package org.batfish.specifier;

import com.google.common.collect.ImmutableSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import org.batfish.datamodel.Interface;

/**
 * An abstract {@link LocationSpecifier} specifying interfaces with names matching the input regex.
 */
public abstract class InterfaceNameRegexLocationSpecifier implements LocationSpecifier {
  private final Pattern _pattern;

  public InterfaceNameRegexLocationSpecifier(Pattern pattern) {
    _pattern = pattern;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    InterfaceNameRegexLocationSpecifier that = (InterfaceNameRegexLocationSpecifier) o;
    return Objects.equals(_pattern.pattern(), that._pattern.pattern());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_pattern);
  }

  protected abstract Location getLocation(Interface iface);

  @Override
  public Set<Location> resolve(SpecifierContext ctxt) {
    return ctxt.getConfigs().values().stream()
        .flatMap(node -> node.getAllInterfaces().values().stream())
        .filter(iface -> _pattern.matcher(iface.getName()).find())
        .map(this::getLocation)
        .collect(ImmutableSet.toImmutableSet());
  }
}
