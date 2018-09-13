package org.batfish.specifier;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;

/**
 * An {@link InterfaceSpecifier} specifying interfaces that belong to Zones with names matching the
 * input regex.
 */
public final class ZoneNameRegexInterfaceSpecifier implements InterfaceSpecifier {
  Pattern _pattern;

  public ZoneNameRegexInterfaceSpecifier(Pattern pattern) {
    _pattern = pattern;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ZoneNameRegexInterfaceSpecifier)) {
      return false;
    }
    ZoneNameRegexInterfaceSpecifier that = (ZoneNameRegexInterfaceSpecifier) o;
    return Objects.equals(_pattern.pattern(), that._pattern.pattern());
  }

  @Override
  public int hashCode() {
    return Objects.hash(_pattern.pattern());
  }

  @Override
  public Set<Interface> resolve(Set<String> nodes, SpecifierContext ctxt) {
    return nodes
        .stream()
        .map(n -> resolve(n, ctxt))
        .flatMap(Collection::stream)
        .collect(Collectors.toSet());
  }

  // This helper could be avoided if Zones stored Interfaces and not (just) interface names
  private Set<Interface> resolve(String node, SpecifierContext ctxt) {
    Configuration config = ctxt.getConfigs().get(node);
    Set<String> interfaceNamesInMatchingZones =
        config
            .getZones()
            .values()
            .stream()
            .filter(z -> _pattern.matcher(z.getName()).matches())
            .map(z -> z.getInterfaces())
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());
    return config
        .getAllInterfaces()
        .values()
        .stream()
        .filter(i -> interfaceNamesInMatchingZones.contains(i.getName()))
        .collect(Collectors.toSet());
  }
}
