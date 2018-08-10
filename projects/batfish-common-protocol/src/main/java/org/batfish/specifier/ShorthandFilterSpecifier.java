package org.batfish.specifier;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.questions.FiltersSpecifier;

/**
 * A {@link FilterSpecifier} based on the original {@link
 * org.batfish.datamodel.questions.FiltersSpecifier} which used a shorthand notation.
 */
@ParametersAreNonnullByDefault
public final class ShorthandFilterSpecifier implements FilterSpecifier {
  @Nonnull private final FiltersSpecifier _shorthandSpecifier;

  public ShorthandFilterSpecifier(FiltersSpecifier shorthandSpecifier) {
    _shorthandSpecifier = shorthandSpecifier;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ShorthandFilterSpecifier)) {
      return false;
    }
    ShorthandFilterSpecifier that = (ShorthandFilterSpecifier) o;
    return Objects.equals(_shorthandSpecifier, that._shorthandSpecifier);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_shorthandSpecifier);
  }

  @Override
  public Set<IpAccessList> resolve(String node, SpecifierContext ctxt) {
    checkArgument(
        ctxt.getConfigs().containsKey(node),
        "SpecifierContext does not have configs for node: " + node);
    Configuration config = ctxt.getConfigs().get(node);
    return config
        .getIpAccessLists()
        .values()
        .stream()
        .filter(filter -> _shorthandSpecifier.matches(filter, config))
        .collect(Collectors.toSet());
  }
}
