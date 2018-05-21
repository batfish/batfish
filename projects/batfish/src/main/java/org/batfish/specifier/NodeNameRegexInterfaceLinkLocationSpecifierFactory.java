package org.batfish.specifier;

import java.util.regex.Pattern;

/**
 * A {@link LocationSpecifierFactory} that builds {@link
 * NodeNameRegexInterfaceLinkLocationSpecifier}s.
 */
public class NodeNameRegexInterfaceLinkLocationSpecifierFactory
    extends TypedLocationSpecifierFactory<Pattern> {
  @Override
  protected Class<Pattern> getInputClass() {
    return Pattern.class;
  }

  @Override
  public String getName() {
    return getClass().getSimpleName();
  }

  @Override
  public LocationSpecifier buildLocationSpecifierTyped(Pattern pattern) {
    return new NodeNameRegexInterfaceLocationSpecifier(pattern);
  }
}
