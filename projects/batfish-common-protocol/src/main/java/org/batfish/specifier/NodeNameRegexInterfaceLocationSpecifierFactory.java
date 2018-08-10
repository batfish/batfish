package org.batfish.specifier;

import com.google.auto.service.AutoService;
import java.util.regex.Pattern;

/**
 * A {@link LocationSpecifierFactory} that builds {@link NodeNameRegexInterfaceLocationSpecifier}s.
 */
@AutoService(LocationSpecifierFactory.class)
public final class NodeNameRegexInterfaceLocationSpecifierFactory
    extends CaseInsensitiveRegexLocationSpecifierFactory {
  public static final String NAME =
      NodeNameRegexInterfaceLocationSpecifierFactory.class.getSimpleName();

  @Override
  public LocationSpecifier buildLocationSpecifier(Pattern pattern) {
    return new NodeNameRegexInterfaceLocationSpecifier(pattern);
  }

  @Override
  public String getName() {
    return getClass().getSimpleName();
  }
}
