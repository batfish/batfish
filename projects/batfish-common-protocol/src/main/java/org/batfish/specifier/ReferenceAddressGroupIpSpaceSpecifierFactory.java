package org.batfish.specifier;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.auto.service.AutoService;
import javax.annotation.Nullable;

/**
 * A factory for {@link ReferenceAddressGroupIpSpaceSpecifier}. Takes input of the form "groupName,
 * bookName"
 */
@AutoService(IpSpaceSpecifierFactory.class)
public final class ReferenceAddressGroupIpSpaceSpecifierFactory implements IpSpaceSpecifierFactory {
  public static final String NAME =
      ReferenceAddressGroupIpSpaceSpecifierFactory.class.getSimpleName();

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public IpSpaceSpecifier buildIpSpaceSpecifier(@Nullable Object input) {
    checkArgument(input instanceof String, getName() + " requires input of type String");
    String[] words = ((String) input).split(",");
    checkArgument(words.length == 2, getName() + "requires two words separated by ','");
    return new ReferenceAddressGroupIpSpaceSpecifier(words[0].trim(), words[1].trim());
  }
}
