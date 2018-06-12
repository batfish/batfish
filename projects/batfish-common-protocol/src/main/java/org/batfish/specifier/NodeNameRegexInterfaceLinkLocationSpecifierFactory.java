package org.batfish.specifier;

import com.google.auto.service.AutoService;
import java.util.regex.Pattern;

/**
 * A {@link LocationSpecifierFactory} that builds {@link
 * NodeNameRegexInterfaceLinkLocationSpecifier}s.
 */
@AutoService(LocationSpecifierFactory.class)
public final class NodeNameRegexInterfaceLinkLocationSpecifierFactory
    extends CaseInsensitiveRegexLocationSpecifierFactory {
  public static final String NAME =
      NodeNameRegexInterfaceLinkLocationSpecifierFactory.class.getSimpleName();

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public LocationSpecifier buildLocationSpecifier(Pattern pattern) {
    return new NodeNameRegexInterfaceLinkLocationSpecifier(pattern);
  }
}
