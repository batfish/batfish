package org.batfish.specifier;

import com.google.auto.service.AutoService;
import java.util.regex.Pattern;

/**
 * A {@link LocationSpecifierFactory} that builds {@link NodeNameRegexInterfaceLocationSpecifier}s.
 */
@AutoService(LocationSpecifierFactory.class)
public final class NodeNameRegexInterfaceLocationSpecifierFactory
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
