package org.batfish.specifier;

import com.google.auto.service.AutoService;
import java.util.regex.Pattern;

/** A {@link LocationSpecifierFactory} that builds {@link NameRegexInterfaceLocationSpecifier}s. */
@AutoService(LocationSpecifierFactory.class)
public final class NameRegexInterfaceLocationSpecifierFactory
    extends CaseInsensitiveRegexLocationSpecifierFactory {
  public static final String NAME =
      NameRegexInterfaceLocationSpecifierFactory.class.getSimpleName();

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public LocationSpecifier buildLocationSpecifier(Pattern pattern) {
    return new NameRegexInterfaceLocationSpecifier(pattern);
  }
}
