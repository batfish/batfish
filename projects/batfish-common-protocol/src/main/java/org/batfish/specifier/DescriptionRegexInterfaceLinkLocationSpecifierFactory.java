package org.batfish.specifier;

import com.google.auto.service.AutoService;
import java.util.regex.Pattern;

/**
 * A {@link LocationSpecifierFactory} for {@link DescriptionRegexInterfaceLinkLocationSpecifier}.
 */
@AutoService(LocationSpecifierFactory.class)
public class DescriptionRegexInterfaceLinkLocationSpecifierFactory
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
  public LocationSpecifier buildLocationSpecifierTyped(Pattern input) {
    return new DescriptionRegexInterfaceLinkLocationSpecifier(input);
  }
}
