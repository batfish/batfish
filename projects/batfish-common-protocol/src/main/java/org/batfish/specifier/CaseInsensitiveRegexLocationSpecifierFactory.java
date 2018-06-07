package org.batfish.specifier;

import java.util.regex.Pattern;

public abstract class CaseInsensitiveRegexLocationSpecifierFactory
    implements LocationSpecifierFactory {
  @Override
  public LocationSpecifier buildLocationSpecifier(Object input) {
    if (!(input instanceof String)) {
      throw new IllegalArgumentException(getName() + " requires input of type String");
    }

    return buildLocationSpecifier(Pattern.compile((String) input, Pattern.CASE_INSENSITIVE));
  }

  abstract LocationSpecifier buildLocationSpecifier(Pattern pattern);
}
