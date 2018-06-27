package org.batfish.specifier;

import com.google.auto.service.AutoService;
import java.util.regex.Pattern;

/**
 * A {@link LocationSpecifierFactory} for {@link DescriptionRegexInterfaceLinkLocationSpecifier}.
 */
@AutoService(LocationSpecifierFactory.class)
public final class DescriptionRegexInterfaceLinkLocationSpecifierFactory
    extends CaseInsensitiveRegexLocationSpecifierFactory {
  public static final String NAME =
      DescriptionRegexInterfaceLinkLocationSpecifierFactory.class.getSimpleName();

  @Override
  public LocationSpecifier buildLocationSpecifier(Pattern input) {
    return new DescriptionRegexInterfaceLinkLocationSpecifier(input);
  }

  @Override
  public String getName() {
    return NAME;
  }
}
