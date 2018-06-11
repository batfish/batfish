package org.batfish.specifier;

import com.google.auto.service.AutoService;
import java.util.regex.Pattern;

/** A {@link LocationSpecifierFactory} for {@link DescriptionRegexInterfaceLocationSpecifier}. */
@AutoService(LocationSpecifierFactory.class)
public final class DescriptionRegexInterfaceLocationSpecifierFactory
    extends CaseInsensitiveRegexLocationSpecifierFactory {
  public static final String NAME =
      DescriptionRegexInterfaceLocationSpecifierFactory.class.getSimpleName();

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public LocationSpecifier buildLocationSpecifier(Pattern input) {
    return new DescriptionRegexInterfaceLocationSpecifier(input);
  }
}
