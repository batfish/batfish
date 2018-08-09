package org.batfish.specifier;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.auto.service.AutoService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.batfish.datamodel.questions.InterfacesSpecifier;

/**
 * A {@link InterfaceSpecifierFactory} that accepts three forms of input
 *
 * <ul>
 *   <li>null: returns ShorthandInterfaceSpecifier(InterfacesSpecifier.ALL)
 *   <li>vrf(regex): returns {@link VrfNameRegexInterfaceSpecifier} *
 *   <li>all other inputs go directly to {@link ShorthandInterfaceSpecifier}
 * </ul>
 */
@AutoService(InterfaceSpecifierFactory.class)
public class FlexibleInterfaceSpecifierFactory implements InterfaceSpecifierFactory {
  public static final String NAME = FlexibleInterfaceSpecifierFactory.class.getSimpleName();

  private static final Pattern VRF_PATTERN =
      Pattern.compile("vrf\\((.*)\\)", Pattern.CASE_INSENSITIVE);

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public InterfaceSpecifier buildInterfaceSpecifier(Object input) {
    if (input == null) {
      return new ShorthandInterfaceSpecifier(InterfacesSpecifier.ALL);
    }
    checkArgument(input instanceof String, NAME + " requires String input");
    String str = ((String) input).trim();

    // ref pattern
    Matcher matcher = VRF_PATTERN.matcher(str);
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

    return new ShorthandInterfaceSpecifier(new InterfacesSpecifier(str));
  }
}
