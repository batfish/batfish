package org.batfish.specifier;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.regex.Pattern;

/** A {@link NodeSpecifier} that specifies the set of nodes whose names match the input regex. */
public final class NameRegexNodeSpecifier implements NodeSpecifier {
  private final Pattern _namePattern;

  public NameRegexNodeSpecifier(Pattern namePattern) {
    _namePattern = namePattern;
  }

  @Override
  public Set<String> resolve(SpecifierContext ctxt) {
    return ctxt.getConfigs()
        .keySet()
        .stream()
        .filter(n -> _namePattern.matcher(n).matches())
        .collect(ImmutableSet.toImmutableSet());
  }
}
