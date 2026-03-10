package org.batfish.specifier;

import com.google.common.collect.ImmutableSet;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DeviceType;

/**
 * A {@link NodeSpecifier} that matches nodes by a list of {@link org.batfish.datamodel.DeviceType}.
 */
@ParametersAreNonnullByDefault
public final class TypesNodeSpecifier implements NodeSpecifier {
  private final @Nonnull Set<DeviceType> _types;

  public TypesNodeSpecifier(Set<DeviceType> types) {
    _types = types;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof TypesNodeSpecifier)) {
      return false;
    }
    TypesNodeSpecifier that = (TypesNodeSpecifier) o;
    return Objects.equals(_types, that._types);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_types);
  }

  @Override
  public Set<String> resolve(SpecifierContext ctxt) {
    return ctxt.getConfigs().values().stream()
        .filter(c -> _types.contains(c.getDeviceType()))
        .map(Configuration::getHostname)
        .collect(ImmutableSet.toImmutableSet());
  }
}
