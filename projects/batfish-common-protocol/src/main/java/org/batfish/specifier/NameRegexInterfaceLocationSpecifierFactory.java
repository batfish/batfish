package org.batfish.specifier;

import com.google.auto.service.AutoService;
import java.util.regex.Pattern;

/** A {@link LocationSpecifierFactory} that builds {@link NameRegexInterfaceLocationSpecifier}s. */
@AutoService(LocationSpecifierFactory.class)
public final class NameRegexInterfaceLocationSpecifierFactory implements LocationSpecifierFactory {
  public static final String NAME =
      NameRegexInterfaceLocationSpecifierFactory.class.getSimpleName();

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public LocationSpecifier buildLocationSpecifier(Object input) {
    Pattern pattern;

    if (input instanceof Pattern) {
      pattern = (Pattern) input;
    } else if (input instanceof String) {
      pattern = Pattern.compile((String) input);
    } else {
      throw new IllegalArgumentException(NAME + " requires input of type Pattern or String");
    }

    return new NameRegexInterfaceLocationSpecifier(pattern);
  }
}
