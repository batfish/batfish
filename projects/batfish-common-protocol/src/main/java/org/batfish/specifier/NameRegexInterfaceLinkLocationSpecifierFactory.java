package org.batfish.specifier;

import java.util.regex.Pattern;

public class NameRegexInterfaceLinkLocationSpecifierFactory
    extends CaseInsensitiveRegexLocationSpecifierFactory {
  public static final String NAME =
      NameRegexInterfaceLinkLocationSpecifierFactory.class.getSimpleName();

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  LocationSpecifier buildLocationSpecifier(Pattern pattern) {
    return new NameRegexInterfaceLinkLocationSpecifier(pattern);
  }
}
