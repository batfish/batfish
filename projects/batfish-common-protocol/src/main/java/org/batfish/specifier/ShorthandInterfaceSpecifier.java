package org.batfish.specifier;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.questions.InterfacesSpecifier;

/**
 * A {@link InterfaceSpecifier} based on the original {@link InterfacesSpecifier} which used a
 * shorthand notation.
 */
@ParametersAreNonnullByDefault
public final class ShorthandInterfaceSpecifier implements InterfaceSpecifier {
  @Nonnull private final InterfacesSpecifier _shorthandSpecifier;

  public ShorthandInterfaceSpecifier(InterfacesSpecifier shorthandSpecifier) {
    _shorthandSpecifier = shorthandSpecifier;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ShorthandInterfaceSpecifier)) {
      return false;
    }
    ShorthandInterfaceSpecifier that = (ShorthandInterfaceSpecifier) o;
    return Objects.equals(_shorthandSpecifier, that._shorthandSpecifier);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_shorthandSpecifier);
  }

  @Override
  public Set<Interface> resolve(Set<String> nodes, SpecifierContext ctxt) {
    return ctxt.getConfigs()
        .values()
        .stream()
        .filter(c -> nodes.contains(c.getHostname()))
        .map(c -> c.getAllInterfaces().values())
        .flatMap(Collection::stream)
        .filter(_shorthandSpecifier::matches)
        .collect(Collectors.toSet());
  }
}
