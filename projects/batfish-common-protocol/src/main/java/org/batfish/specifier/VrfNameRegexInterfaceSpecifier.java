package org.batfish.specifier;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.batfish.datamodel.Interface;

/**
 * An {@link InterfaceSpecifier} specifying interfaces that belong to VRFs with names matching the
 * input regex.
 */
public final class VrfNameRegexInterfaceSpecifier implements InterfaceSpecifier {
  Pattern _pattern;

  public VrfNameRegexInterfaceSpecifier(Pattern pattern) {
    _pattern = pattern;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof VrfNameRegexInterfaceSpecifier)) {
      return false;
    }
    VrfNameRegexInterfaceSpecifier that = (VrfNameRegexInterfaceSpecifier) o;
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
        .map(n -> ctxt.getConfigs().get(n).getVrfs().values())
        .flatMap(Collection::stream)
        // we have a stream of VRFs now
        .filter(v -> _pattern.matcher(v.getName()).matches())
        .map(v -> v.getInterfaces().values())
        .flatMap(Collection::stream)
        .collect(Collectors.toSet());
  }
}
