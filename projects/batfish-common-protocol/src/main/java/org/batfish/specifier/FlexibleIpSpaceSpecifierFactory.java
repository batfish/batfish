package org.batfish.specifier;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.auto.service.AutoService;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.IpWildcardSetIpSpace;

/**
 * An abstract IpSpaceSpecifierFactory that accepts the following types of inputs:
 *
 * <ul>
 *   <li>{@code null}, which returns a default factory provided by the subclass.
 *   <li>{@code ref.addressgroup(foo, bar)}, which returns {@link
 *       ReferenceAddressGroupIpSpaceSpecifier};
 *   <li>{@code ofLocation(...)}, which processes its input using {@link LocationIpSpaceSpecifier};
 *   <li>and constant ip wildcards in the format {@code w1,w2,...wn-wn+1,wn+2,...}. Wildcards to the
 *       left of - are * whitelisted (at least 1 is required). Wildcards to the right of the dash
 *       (-), if present, are * blacklisted. Can also use backslash (\) instead of dash.
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
    return new ConstantIpSpaceSpecifier(parseIpSpace(input));
  }

  @VisibleForTesting
  static IpWildcardSetIpSpace parseIpSpace(@Nonnull String input) {
    String[] strs = input.split("-|\\\\");

    if (strs.length == 1) {
      return IpWildcardSetIpSpace.builder().including(parseWildcards(strs[0])).build();
    }
    if (strs.length == 2) {
      return IpWildcardSetIpSpace.builder()
          .including(parseWildcards(strs[0]))
          .excluding(parseWildcards(strs[1]))
          .build();
    }
    throw new IllegalArgumentException(
        "Error parsing IpWildcards: only 1 subtraction operator ('-' or '\\') allowed");
  }

  @VisibleForTesting
  static Iterable<IpWildcard> parseWildcards(@Nonnull String wildcardsStr) {
    String[] wildcardStrs = wildcardsStr.split(",");
    return Arrays.stream(wildcardStrs)
        .map(String::trim)
        .map(IpWildcard::parse)
        .collect(ImmutableList.toImmutableList());
  }

  @Override
  public String getName() {
    return NAME;
  }
}
