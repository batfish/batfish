package org.batfish.specifier;

import com.google.auto.service.AutoService;
import java.util.regex.Pattern;

/**
 * A {@link LocationSpecifierFactory} that builds {@link
 * VrfNameRegexInterfaceLinkLocationSpecifier}s.
 */
@AutoService(LocationSpecifierFactory.class)
public final class VrfNameRegexInterfaceLocationSpecifierFactory
    extends CaseInsensitiveRegexLocationSpecifierFactory {
  public static final String NAME =
      VrfNameRegexInterfaceLocationSpecifierFactory.class.getSimpleName();

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public LocationSpecifier buildLocationSpecifier(Pattern pattern) {
    return new VrfNameRegexInterfaceLocationSpecifier(pattern);
  }
}
