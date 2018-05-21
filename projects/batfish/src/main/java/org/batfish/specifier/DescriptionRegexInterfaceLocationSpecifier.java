package org.batfish.specifier;

import com.google.common.collect.ImmutableSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * A {@link LocationSpecifier} specifying all interfaces whose description matches the input regex.
 */
public class DescriptionRegexInterfaceLocationSpecifier implements LocationSpecifier {
  private final Pattern _pattern;

  public DescriptionRegexInterfaceLocationSpecifier(Pattern pattern) {
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
    DescriptionRegexInterfaceLocationSpecifier that =
        (DescriptionRegexInterfaceLocationSpecifier) o;
    return Objects.equals(_pattern, that._pattern);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_pattern);
  }

  protected Location makeLocation(String node, String iface) {
    return new InterfaceLocation(node, iface);
  }

  @Override
  public Set<Location> resolve(SpecifierContext ctxt) {
    return ctxt.getConfigs()
        .values()
        .stream()
        .flatMap(
            config ->
                config
                    .getInterfaces()
                    .values()
                    .stream()
                    .filter(iface -> _pattern.matcher(iface.getDescription()).matches())
                    .map(iface -> makeLocation(iface.getOwner().getName(), iface.getName())))
        .collect(ImmutableSet.toImmutableSet());
  }
}
