package org.batfish.specifier;

import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.IpAccessList;

/** A {@link FilterSpecifier} that matches filter names based on a regex pattern. */
@ParametersAreNonnullByDefault
public final class NameRegexFilterSpecifier implements FilterSpecifier {
  private final @Nonnull Pattern _pattern;

  public NameRegexFilterSpecifier(Pattern pattern) {
    _pattern = pattern;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NameRegexFilterSpecifier)) {
      return false;
    }
    NameRegexFilterSpecifier that = (NameRegexFilterSpecifier) o;
    return Objects.equals(_pattern.pattern(), that._pattern.pattern());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_pattern.pattern());
  }

  @Override
  public Set<IpAccessList> resolve(String node, SpecifierContext ctxt) {
    return ctxt.getConfigs().values().stream()
        .filter(c -> c.getHostname().equalsIgnoreCase(node))
        .map(c -> c.getIpAccessLists().values())
        .flatMap(Collection::stream)
        .filter(f -> _pattern.matcher(f.getName()).find())
        .collect(ImmutableSet.toImmutableSet());
  }
}
