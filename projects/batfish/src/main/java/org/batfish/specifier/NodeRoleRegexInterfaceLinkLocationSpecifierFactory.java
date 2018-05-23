package org.batfish.specifier;

import com.google.auto.service.AutoService;
import java.util.regex.Pattern;

/**
 * A {@link LocationSpecifierFactory} that builds {@link
 * org.batfish.specifier.NodeRoleRegexInterfaceLinkLocationSpecifier}s.
 */
@AutoService(LocationSpecifierFactory.class)
public final class NodeRoleRegexInterfaceLinkLocationSpecifierFactory
    extends NodeRoleRegexLocationSpecifierFactory {

  @Override
  public LocationSpecifier buildLocationSpecifier(String roleDimension, Pattern rolePattern) {
    return new NodeRoleRegexInterfaceLinkLocationSpecifier(roleDimension, rolePattern);
  }

  @Override
  public String getName() {
    return getClass().getSimpleName();
  }
}
