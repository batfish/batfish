package org.batfish.specifier;

import com.google.common.collect.ImmutableSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import org.batfish.datamodel.Interface;

/**
 * A {@link LocationSpecifier} specifying all interfaces whose description matches the input regex.
 */
public abstract class InterfaceDescriptionRegexLocationSpecifier implements LocationSpecifier {
  private final Pattern _pattern;

  public InterfaceDescriptionRegexLocationSpecifier(Pattern pattern) {
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
    InterfaceDescriptionRegexLocationSpecifier that =
        (InterfaceDescriptionRegexLocationSpecifier) o;
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
        .flatMap(config -> config.getAllInterfaces().values().stream())
        .filter(iface -> _pattern.matcher(iface.getDescription()).matches())
        .map(this::getLocation)
        .collect(ImmutableSet.toImmutableSet());
  }
}
