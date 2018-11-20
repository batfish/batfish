package org.batfish.specifier;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;

/**
 * An {@link InterfaceSpecifier} that specifies interfaces by {@link InterfaceType type}. Uses a
 * regex to specify which types to include.
 */
public final class TypeNameRegexInterfaceSpecifier implements InterfaceSpecifier {
  private final Set<InterfaceType> _interfaceTypes;

  public TypeNameRegexInterfaceSpecifier(Pattern typeNameRegex) {
    _interfaceTypes =
        Stream.of(InterfaceType.values())
            .filter(type -> typeNameRegex.matcher(type.name()).matches())
            .collect(ImmutableSet.toImmutableSet());
    Preconditions.checkArgument(
        !_interfaceTypes.isEmpty(),
        String.format("Interface type regex %s matches no types", typeNameRegex.pattern()));
  }

  @Override
  public Set<Interface> resolve(Set<String> nodes, SpecifierContext ctxt) {
    Map<String, Configuration> configs = ctxt.getConfigs();

    return nodes
        .stream()
        .map(configs::get)
        .flatMap(config -> config.getAllInterfaces().values().stream())
        .filter(iface -> _interfaceTypes.contains(iface.getInterfaceType()))
        .collect(ImmutableSet.toImmutableSet());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof TypeNameRegexInterfaceSpecifier)) {
      return false;
    }
    TypeNameRegexInterfaceSpecifier that = (TypeNameRegexInterfaceSpecifier) o;
    return Objects.equals(_interfaceTypes, that._interfaceTypes);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_interfaceTypes);
  }
}
