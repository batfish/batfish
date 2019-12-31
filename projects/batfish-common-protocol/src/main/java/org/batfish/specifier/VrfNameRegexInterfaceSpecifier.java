package org.batfish.specifier;

import com.google.common.collect.ImmutableSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import org.batfish.datamodel.collections.NodeInterfacePair;

/**
 * An {@link InterfaceSpecifier} specifying interfaces that belong to VRFs with names matching the
 * input regex.
 */
public final class VrfNameRegexInterfaceSpecifier implements InterfaceSpecifier {
  private final Pattern _pattern;

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
    return Objects.hashCode(_pattern.pattern());
  }

  @Override
  public Set<NodeInterfacePair> resolve(Set<String> nodes, SpecifierContext ctxt) {
    return nodes.stream()
        .flatMap(n -> ctxt.getConfigs().get(n).getAllInterfaces().values().stream())
        .filter(i -> _pattern.matcher(i.getVrfName()).matches())
        .map(NodeInterfacePair::of)
        .collect(ImmutableSet.toImmutableSet());
  }
}
