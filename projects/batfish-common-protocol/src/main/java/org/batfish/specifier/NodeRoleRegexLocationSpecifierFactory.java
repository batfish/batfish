package org.batfish.specifier;

import java.util.regex.Pattern;
import org.batfish.common.BatfishException;

/**
 * A {@link LocationSpecifierFactory} that builds {@link NodeNameRegexInterfaceLocationSpecifier}s.
 */
public abstract class NodeRoleRegexLocationSpecifierFactory
    extends TypedLocationSpecifierFactory<String> {

  @Override
  protected final Class<String> getInputClass() {
    return String.class;
  }

  @Override
  public final LocationSpecifier buildLocationSpecifierTyped(String input) {
    String[] parts = input.split(":");

    if (parts.length != 2) {
      throw new BatfishException("required input format is <role dimension>:<role name regex>");
    }

    return buildLocationSpecifier(parts[0], Pattern.compile(parts[1], Pattern.CASE_INSENSITIVE));
  }

  public abstract LocationSpecifier buildLocationSpecifier(
      String roleDimension, Pattern rolePattern);
}
