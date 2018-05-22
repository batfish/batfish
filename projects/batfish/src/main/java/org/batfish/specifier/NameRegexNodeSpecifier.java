package org.batfish.specifier;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.regex.Pattern;

public class NameRegexNodeSpecifier implements NodeSpecifier {
  private final Pattern _namePattern;

  public NameRegexNodeSpecifier(Pattern namePattern) {
    _namePattern = namePattern;
  }

  @Override
  public Set<String> resolve(SpecifierContext specifierContext) {
    return specifierContext
        .getConfigs()
        .keySet()
        .stream()
        .filter(n -> _namePattern.matcher(n).matches())
        .collect(ImmutableSet.toImmutableSet());
  }
}
