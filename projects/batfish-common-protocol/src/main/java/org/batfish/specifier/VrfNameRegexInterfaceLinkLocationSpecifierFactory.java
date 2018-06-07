package org.batfish.specifier;

import com.google.auto.service.AutoService;
import java.util.regex.Pattern;

/**
 * A {@link LocationSpecifierFactory} that builds {@link
 * VrfNameRegexInterfaceLinkLocationSpecifier}s.
 */
@AutoService(LocationSpecifierFactory.class)
public final class VrfNameRegexInterfaceLinkLocationSpecifierFactory
    extends CaseInsensitiveRegexLocationSpecifierFactory {
  public static final String NAME =
      VrfNameRegexInterfaceLinkLocationSpecifierFactory.class.getSimpleName();

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  LocationSpecifier buildLocationSpecifier(Pattern pattern) {
    return new VrfNameRegexInterfaceLinkLocationSpecifier(pattern);
  }
}
