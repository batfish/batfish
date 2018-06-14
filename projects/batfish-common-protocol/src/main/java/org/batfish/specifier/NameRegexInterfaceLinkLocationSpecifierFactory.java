package org.batfish.specifier;

import com.google.auto.service.AutoService;
import java.util.regex.Pattern;

@AutoService(LocationSpecifierFactory.class)
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
