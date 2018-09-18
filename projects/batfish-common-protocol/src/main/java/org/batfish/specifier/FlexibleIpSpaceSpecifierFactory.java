package org.batfish.specifier;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.VisibleForTesting;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

/**
 * An abstract IpSpaceSpecifierFactory that accepts the following types of inputs:
 *
 * <ul>
 *   <li>{@code null}, which returns a default factory provided by the subclass.
 *   <li>{@code ref.addressgroup(foo, bar)}, which returns {@link
 *       ReferenceAddressGroupIpSpaceSpecifier};
 *   <li>{@code ofLocation(...)}, which processes its input using {@link
 *       FlexibleLocationIpSpaceSpecifierFactory};
 *   <li>and inputs accepted by {@link ConstantWildcardSetIpSpaceSpecifierFactory}.
 * </ul>
 */
public abstract class FlexibleIpSpaceSpecifierFactory implements IpSpaceSpecifierFactory {
  private static final Pattern REF_PATTERN =
      Pattern.compile("ref\\.addressgroup\\((.*)\\)", Pattern.CASE_INSENSITIVE);
  private static final Pattern LOCATION_PATTERN =
      Pattern.compile("ofLocation\\((.*)\\)", Pattern.CASE_INSENSITIVE);

  protected abstract IpSpaceSpecifier defaultIpSpaceSpecifier();

  @Override
  public IpSpaceSpecifier buildIpSpaceSpecifier(@Nullable Object input) {
    if (input == null) {
      return defaultIpSpaceSpecifier();
    }
    checkArgument(input instanceof String, getName() + " requires String input");
    String str = ((String) input).trim();
    return parse(str);
  }

  @VisibleForTesting
  static IpSpaceSpecifier parse(String input) {
    Matcher matcher = REF_PATTERN.matcher(input);
    if (matcher.find()) {
      return new ReferenceAddressGroupIpSpaceSpecifierFactory()
          .buildIpSpaceSpecifier(matcher.group(1));
    }
    matcher = LOCATION_PATTERN.matcher(input);
    if (matcher.find()) {
      return new FlexibleLocationIpSpaceSpecifierFactory().buildIpSpaceSpecifier(matcher.group(1));
    }
    return new ConstantWildcardSetIpSpaceSpecifierFactory().buildIpSpaceSpecifier(input);
  }
}
