package org.batfish.specifier;

import com.google.common.collect.ImmutableSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/** A {@link LocationSpecifier} specifying interfaces with names matching the input regex. */
public class NameRegexInterfaceLocationSpecifier implements LocationSpecifier {
  private final Pattern _pattern;

  public NameRegexInterfaceLocationSpecifier(Pattern pattern) {
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
    NameRegexInterfaceLocationSpecifier that = (NameRegexInterfaceLocationSpecifier) o;
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
        .entrySet()
        .stream()
        .flatMap(
            entry -> {
              String node = entry.getKey();
              return entry
                  .getValue()
                  .getInterfaces()
                  .keySet()
                  .stream()
                  .filter(iface -> _pattern.matcher(iface).matches())
                  .map(iface -> makeLocation(node, iface));
            })
        .collect(ImmutableSet.toImmutableSet());
  }
}
