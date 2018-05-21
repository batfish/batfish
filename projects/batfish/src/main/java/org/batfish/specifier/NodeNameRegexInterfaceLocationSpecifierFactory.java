package org.batfish.specifier;

import java.util.regex.Pattern;

public class NodeNameRegexInterfaceLocationSpecifierFactory
    extends TypedLocationSpecifierFactory<Pattern> {
  @Override
  protected Class<Pattern> getInputClass() {
    return Pattern.class;
  }

  @Override
  public LocationSpecifier specifierTyped(Pattern pattern) {
    return new NodeNameRegexInterfaceLocationSpecifier(pattern);
  }

  @Override
  public String getName() {
    return getClass().getSimpleName();
  }
}
