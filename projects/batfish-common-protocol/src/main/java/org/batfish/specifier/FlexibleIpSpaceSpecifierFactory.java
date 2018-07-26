package org.batfish.specifier;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.auto.service.AutoService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An IpSpaceSpecifierFactory that accepts three types of inputs:
 *
 * <ul>
 *   <li>null, which returns {@link InferFromLocationIpSpaceSpecifier};
 *   <li>ref.addressgroup(foo, bar), which returns {@link ReferenceAddressGroupIpSpaceSpecifier};
 *   <li>inputs accepted by {@link ConstantWildcardSetIpSpaceSpecifierFactory}
 * </ul>
 */
@AutoService(IpSpaceSpecifierFactory.class)
public class FlexibleIpSpaceSpecifierFactory implements IpSpaceSpecifierFactory {
  public static final String NAME = FlexibleIpSpaceSpecifierFactory.class.getSimpleName();

  Pattern REF_PATTERN = Pattern.compile("ref\\.addressgroup\\((.*)\\)", Pattern.CASE_INSENSITIVE);

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
    String str = ((String) input).trim();
    Matcher matcher = REF_PATTERN.matcher(str);
    if (matcher.find()) {
      return new ReferenceAddressGroupIpSpaceSpecifierFactory()
          .buildIpSpaceSpecifier(matcher.group(1));
    }
    return new ConstantWildcardSetIpSpaceSpecifierFactory().buildIpSpaceSpecifier(str);
  }
}
