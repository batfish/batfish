package org.batfish.specifier;

import java.util.regex.Pattern;

public class DescriptionRegexInterfaceLocationSpecifierFactory
    extends TypedLocationSpecifierFactory<Pattern> {
  @Override
  public String getName() {
    return getClass().getSimpleName();
  }

  @Override
  protected Class<Pattern> getInputClass() {
    return Pattern.class;
  }

  @Override
  public LocationSpecifier specifierTyped(Pattern input) {
    return new DescriptionRegexInterfaceLocationSpecifier(input);
  }
}
