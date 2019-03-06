package org.batfish.specifier;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.auto.service.AutoService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

/**
 * A {@link NodeSpecifierFactory} that accepts three forms of input
 *
 * <ul>
 *   <li>null: returns AllNodesNodeSpecifier
 *   <li>ref.noderole(roleRegex, roleDim): returns {@link RoleRegexNodeSpecifier} (dim is optional)
 *   <li>a - b: difference of nodes denoted by a and b
 *   <li>name regex
 * </ul>
 */
@AutoService(NodeSpecifierFactory.class)
public class FlexibleNodeSpecifierFactory implements NodeSpecifierFactory {
  public static final String NAME = FlexibleNodeSpecifierFactory.class.getSimpleName();

  private static final Pattern DIFFERENCE_PATTERN = Pattern.compile("^(.*)\\s+-\\s+(.*)$");

  private static final Pattern REF_PATTERN =
      Pattern.compile("ref\\.noderole\\((.*)\\)", Pattern.CASE_INSENSITIVE);

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public NodeSpecifier buildNodeSpecifier(@Nullable Object input) {
    if (input == null) {
      return AllNodesNodeSpecifier.INSTANCE;
    }
    checkArgument(input instanceof String, NAME + " requires String input");

    String trimmedInput = ((String) input).trim();

    // ref pattern
    Matcher matcher = REF_PATTERN.matcher(trimmedInput);
    if (matcher.find()) {
      String[] words = matcher.group(1).trim().split(",");
      checkArgument(
          words.length == 1 || words.length == 2,
          "Parameter(s) to ref.noderole should be (roleRegex) or (roleRegex, roleDimension). Got ("
              + matcher.group(1)
              + ")");
      Pattern rolePattern = Pattern.compile(words[0].trim(), Pattern.CASE_INSENSITIVE);
      String roleDimension = words.length == 1 ? null : words[1].trim();
      return new RoleRegexNodeSpecifier(rolePattern, roleDimension);
    }

    // difference pattern
    Matcher differenceMatcher = DIFFERENCE_PATTERN.matcher(trimmedInput);
    if (differenceMatcher.find()) {
      return new DifferenceNodeSpecifier(
          buildNodeSpecifier(differenceMatcher.group(1)),
          buildNodeSpecifier(differenceMatcher.group(2)));
    }

    return new NameRegexNodeSpecifier(Pattern.compile(trimmedInput, Pattern.CASE_INSENSITIVE));
  }
}
