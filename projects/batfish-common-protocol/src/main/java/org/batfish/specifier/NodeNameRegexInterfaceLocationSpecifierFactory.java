package org.batfish.specifier;

import com.google.auto.service.AutoService;
import java.util.regex.Pattern;

/**
 * A {@link LocationSpecifierFactory} that builds {@link NodeNameRegexInterfaceLocationSpecifier}s.
 */
@AutoService(LocationSpecifierFactory.class)
public final class NodeNameRegexInterfaceLocationSpecifierFactory
    implements LocationSpecifierFactory {
  public static final String NAME =
      NodeNameRegexInterfaceLocationSpecifierFactory.class.getSimpleName();

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

    return new NodeNameRegexInterfaceLocationSpecifier(pattern);
  }

  @Override
  public String getName() {
    return getClass().getSimpleName();
  }
}
