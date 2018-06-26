package org.batfish.specifier;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.auto.service.AutoService;

/**
 * An IpSpaceSpecifierFactory that delegates to {@link ConstantWildcardSetIpSpaceSpecifierFactory}
 * for non-null inputs, and defaults to {@link InferFromLocationIpSpaceSpecifier} for null input.
 */
@AutoService(IpSpaceSpecifierFactory.class)
public class FlexibleIpSpaceSpecifierFactory implements IpSpaceSpecifierFactory {
  public static final String NAME = FlexibleIpSpaceSpecifierFactory.class.getSimpleName();

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public IpSpaceSpecifier buildIpSpaceSpecifier(Object input) {
    if (input == null) {
      return InferFromLocationIpSpaceSpecifier.INSTANCE;
    }
    checkArgument(input instanceof String, NAME + " requires String input");
    String str = (String) input;
    return new ConstantWildcardSetIpSpaceSpecifierFactory().buildIpSpaceSpecifier(str);
  }
}
