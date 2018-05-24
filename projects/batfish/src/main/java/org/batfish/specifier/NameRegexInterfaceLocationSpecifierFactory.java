package org.batfish.specifier;

import com.google.auto.service.AutoService;
import java.util.regex.Pattern;

/** A {@link LocationSpecifierFactory} that builds {@link NameRegexInterfaceLocationSpecifier}s. */
@AutoService(LocationSpecifierFactory.class)
public final class NameRegexInterfaceLocationSpecifierFactory
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
  public LocationSpecifier buildLocationSpecifierTyped(Pattern pattern) {
    return new NameRegexInterfaceLocationSpecifier(pattern);
  }
}
