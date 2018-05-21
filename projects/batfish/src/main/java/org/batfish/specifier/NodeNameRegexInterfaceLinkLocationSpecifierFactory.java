package org.batfish.specifier;

import java.util.regex.Pattern;

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
