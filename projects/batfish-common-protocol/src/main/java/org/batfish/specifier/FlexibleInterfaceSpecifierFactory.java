package org.batfish.specifier;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.auto.service.AutoService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.batfish.datamodel.questions.InterfacesSpecifier;

/**
 * A {@link InterfaceSpecifierFactory} that accepts three forms of input
 *
 * <ul>
 *   <li>null: returns ShorthandInterfaceSpecifier(InterfacesSpecifier.ALL)
 *   <li>connectedTo(ip, prefix, or wildcard): returns {@link InterfaceWithConnectedIpsSpecifier}
 *   <li>{@code ref.interfaceGroup(foo, bar)}, which returns {@link
 *       ReferenceInterfaceGroupInterfaceSpecifier};
 *   <li>vrf(regex): returns {@link VrfNameRegexInterfaceSpecifier}
 *   <li>zone(regex): returns {@link ZoneNameRegexInterfaceSpecifier}
 *   <li>type(regex): returns {@link TypeNameRegexInterfaceSpecifier}
 *   <li>all other inputs go directly to {@link ShorthandInterfaceSpecifier}
 * </ul>
 */
@AutoService(InterfaceSpecifierFactory.class)
public class FlexibleInterfaceSpecifierFactory implements InterfaceSpecifierFactory {
  public static final String NAME = FlexibleInterfaceSpecifierFactory.class.getSimpleName();

  private static final Pattern CONNECTED_TO_PATTERN =
      Pattern.compile("connectedTo\\((.*)\\)", Pattern.CASE_INSENSITIVE);

  private static final Pattern REF_PATTERN =
      Pattern.compile("ref\\.interfacegroup\\((.*)\\)", Pattern.CASE_INSENSITIVE);

  private static final Pattern TYPE_PATTERN =
      Pattern.compile("type\\((.*)\\)", Pattern.CASE_INSENSITIVE);

  private static final Pattern VRF_PATTERN =
      Pattern.compile("vrf\\((.*)\\)", Pattern.CASE_INSENSITIVE);

  private static final Pattern ZONE_PATTERN =
      Pattern.compile("zone\\((.*)\\)", Pattern.CASE_INSENSITIVE);

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public InterfaceSpecifier buildInterfaceSpecifier(@Nullable Object input) {
    if (input == null) {
      return new ShorthandInterfaceSpecifier(InterfacesSpecifier.ALL);
    }
    checkArgument(input instanceof String, NAME + " requires String input");
    String str = ((String) input).trim();

    // connected to subnet pattern
    Matcher matcher = CONNECTED_TO_PATTERN.matcher(str);
    if (matcher.find()) {
      String ipWildcard = matcher.group(1).trim();
      return new InterfaceWithConnectedIpsSpecifier.Factory().buildInterfaceSpecifier(ipWildcard);
    }

    // interface group subnet pattern
    matcher = REF_PATTERN.matcher(str);
    if (matcher.find()) {
      String refInput = matcher.group(1).trim();
      String[] words = refInput.split(",");
      checkArgument(
          words.length == 2,
          "ref.interfaceGroup() needs interface group and reference book names separated by ','");

      return new ReferenceInterfaceGroupInterfaceSpecifier(words[0], words[1]);
    }

    // interface type pattern
    matcher = TYPE_PATTERN.matcher(str);
    if (matcher.find()) {
      Pattern typeRegex = Pattern.compile(matcher.group(1).trim(), Pattern.CASE_INSENSITIVE);
      return new TypeNameRegexInterfaceSpecifier(typeRegex);
    }

    // VRF pattern
    matcher = VRF_PATTERN.matcher(str);
    if (matcher.find()) {
      String[] words = matcher.group(1).trim().split(",");
      checkArgument(
          words.length == 1,
          "Parameter(s) to vrf() should be a regex over the vrf names. Got ("
              + matcher.group(1)
              + ")");
      return new VrfNameRegexInterfaceSpecifier(
          Pattern.compile(words[0].trim(), Pattern.CASE_INSENSITIVE));
    }

    // Zone pattern
    matcher = ZONE_PATTERN.matcher(str);
    if (matcher.find()) {
      String[] words = matcher.group(1).trim().split(",");
      checkArgument(
          words.length == 1,
          "Parameter(s) to zone() should be a regex over the zone names. Got ("
              + matcher.group(1)
              + ")");
      return new ZoneNameRegexInterfaceSpecifier(
          Pattern.compile(words[0].trim(), Pattern.CASE_INSENSITIVE));
    }

    return new ShorthandInterfaceSpecifier(new InterfacesSpecifier(str));
  }
}
