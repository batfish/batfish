package org.batfish.specifier;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.auto.service.AutoService;
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
 *   <li>{@code ofLocation(...)}, which processes its input using {@link LocationIpSpaceSpecifier};
 *   <li>and inputs accepted by {@link ConstantWildcardSetIpSpaceSpecifierFactory}.
 * </ul>
 */
@AutoService(IpSpaceSpecifierFactory.class)
public final class FlexibleIpSpaceSpecifierFactory implements IpSpaceSpecifierFactory {
  public static final String NAME = FlexibleIpSpaceSpecifierFactory.class.getSimpleName();

  private static final Pattern REF_PATTERN =
      Pattern.compile("ref\\.addressgroup\\((.*)\\)", Pattern.CASE_INSENSITIVE);
  private static final Pattern LOCATION_PATTERN =
      Pattern.compile("ofLocation\\((.*)\\)", Pattern.CASE_INSENSITIVE);

  @Override
  public IpSpaceSpecifier buildIpSpaceSpecifier(@Nullable Object input) {
    checkArgument(input != null, getName() + " requires non-null input");
    checkArgument(input instanceof String, getName() + " requires String input");
    String str = ((String) input).trim();
    return parse(str);
  }

  @VisibleForTesting
  static IpSpaceSpecifier parse(String input) {
    Matcher matcher = REF_PATTERN.matcher(input);
    if (matcher.find()) {
      String[] words = matcher.group(1).split(",");
      checkArgument(
          words.length == 2, "Arguments to ref.addressgroup should be two words separated by ','");
      return new ReferenceAddressGroupIpSpaceSpecifier(words[0].trim(), words[1].trim());
    }
    matcher = LOCATION_PATTERN.matcher(input);
    if (matcher.find()) {
      return new LocationIpSpaceSpecifier(
          new FlexibleLocationSpecifierFactory().buildLocationSpecifier(matcher.group(1)));
    }
    return new ConstantWildcardSetIpSpaceSpecifierFactory().buildIpSpaceSpecifier(input);
  }

  @Override
  public String getName() {
    return NAME;
  }
}
