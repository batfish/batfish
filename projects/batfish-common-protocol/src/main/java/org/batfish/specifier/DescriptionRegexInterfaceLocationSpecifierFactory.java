package org.batfish.specifier;

import com.google.auto.service.AutoService;
import java.util.regex.Pattern;

/** A {@link LocationSpecifierFactory} for {@link DescriptionRegexInterfaceLocationSpecifier}. */
@AutoService(LocationSpecifierFactory.class)
public final class DescriptionRegexInterfaceLocationSpecifierFactory
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
    return new DescriptionRegexInterfaceLocationSpecifier(input);
  }
}
