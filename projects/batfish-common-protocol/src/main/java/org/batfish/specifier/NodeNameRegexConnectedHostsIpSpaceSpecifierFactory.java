package org.batfish.specifier;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.auto.service.AutoService;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

@AutoService(IpSpaceSpecifierFactory.class)
public final class NodeNameRegexConnectedHostsIpSpaceSpecifierFactory
    implements IpSpaceSpecifierFactory {
  public static final String NAME =
      NodeNameRegexConnectedHostsIpSpaceSpecifierFactory.class.getSimpleName();

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public IpSpaceSpecifier buildIpSpaceSpecifier(@Nullable Object input) {
    checkArgument(input instanceof String, getName() + " requires input of type String");
    return new NodeNameRegexConnectedHostsIpSpaceSpecifier(
        Pattern.compile((String) input, Pattern.CASE_INSENSITIVE));
  }
}
