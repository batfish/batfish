package org.batfish.specifier;

import java.util.regex.Pattern;

/**
 * A {@link LocationSpecifierFactory} that builds {@link NodeNameRegexInterfaceLocationSpecifier}s.
 */
public class NodeNameRegexInterfaceLocationSpecifierFactory
    extends TypedLocationSpecifierFactory<Pattern> {
  @Override
  protected Class<Pattern> getInputClass() {
    return Pattern.class;
  }

  @Override
  public LocationSpecifier buildLocationSpecifierTyped(Pattern pattern) {
    return new NodeNameRegexInterfaceLocationSpecifier(pattern);
  }

  @Override
  public String getName() {
    return getClass().getSimpleName();
  }
}
