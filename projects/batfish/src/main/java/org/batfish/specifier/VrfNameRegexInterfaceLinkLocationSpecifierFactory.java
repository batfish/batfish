package org.batfish.specifier;

import java.util.regex.Pattern;

public class VrfNameRegexInterfaceLinkLocationSpecifierFactory
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
  public LocationSpecifier buildLocationSpecifierTyped(Pattern pattern) {
    return new VrfNameRegexInterfaceLinkLocationSpecifier(pattern);
  }
}
