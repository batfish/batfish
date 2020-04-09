package org.batfish.specifier;

import com.google.common.collect.ImmutableSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.batfish.datamodel.Configuration;

/**
 * An abstract {@link LocationSpecifier} specifying interfaces that belong to VRFs with names
 * matching the input regex.
 */
public abstract class VrfNameRegexLocationSpecifier implements LocationSpecifier {
  private final Pattern _pattern;

  public VrfNameRegexLocationSpecifier(Pattern pattern) {
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
    VrfNameRegexLocationSpecifier that = (VrfNameRegexLocationSpecifier) o;
    return Objects.equals(_pattern.pattern(), that._pattern.pattern());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_pattern);
  }

  protected abstract Stream<Location> getVrfLocations(Configuration c, String vrfName);

  @Override
  public Set<Location> resolve(SpecifierContext ctxt) {
    return ctxt.getConfigs().values().stream()
        .flatMap(
            node ->
                node.getVrfs().values().stream()
                    .filter(vrf -> _pattern.matcher(vrf.getName()).matches())
                    .flatMap(v -> getVrfLocations(node, v.getName())))
        .collect(ImmutableSet.toImmutableSet());
  }
}
