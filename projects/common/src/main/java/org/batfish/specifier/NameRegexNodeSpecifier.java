package org.batfish.specifier;

import com.google.common.collect.ImmutableSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/** A {@link NodeSpecifier} that specifies the set of nodes whose names match the input regex. */
public final class NameRegexNodeSpecifier implements NodeSpecifier {
  private final Pattern _namePattern;

  public NameRegexNodeSpecifier(Pattern namePattern) {
    _namePattern = namePattern;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NameRegexNodeSpecifier)) {
      return false;
    }
    NameRegexNodeSpecifier that = (NameRegexNodeSpecifier) o;
    return Objects.equals(_namePattern.pattern(), that._namePattern.pattern());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_namePattern.pattern());
  }

  @Override
  public Set<String> resolve(SpecifierContext ctxt) {
    return ctxt.getConfigs().keySet().stream()
        .filter(n -> _namePattern.matcher(n).find())
        .collect(ImmutableSet.toImmutableSet());
  }
}
