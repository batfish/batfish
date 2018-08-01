package org.batfish.specifier;

/**
 * A {@link LocationSpecifierFactory} that parses a specification from a string.
 *
 * <p>Examples include:
 *
 * <pre>
 *   rtr-.*              -&gt; nodes matching this regex
 *   rtr-.*:Eth.*        -&gt; interfaces matching the second regex on nodes matching the first
 *   interface(Eth.*)    -&gt; interfaces matching the regex
 *   enter(vrf(default)) -&gt; inbound into all interfaces in VRF default
 *   vrf(default)        -&gt; interfaces in VRF default
 *   ref.noderole(border.*, dim) -&gt; nodes in roles matching the first parameter in dimension dim
 *   a + b               -&gt; union of locations denoted by a and b
 *   a - b               -&gt; difference of locations denoted a and b
 * </pre>
 *
 * <p>More formally, the grammar is:
 *
 * <pre>
 *  VALUE ::= CONSTANT | SPECIFIER | COMBINATION
 *  CONSTANT ::= [a-zA-Z0-9/:.+*_-]+
 *  COMBINATION ::= VALUE [+-] VALUE
 *  NAME ::= [a-zA-Z.]+
 *  SPECIFIER ::= NAME ‘(‘ VALUE (‘,’ VALUE)* ‘)’
 * </pre>
 *
 * <p>NB: The space around '+' and '-' is mandatory and is used to split the string properly.
 * Further, nested expressions a + (b + c) are not currently supported. We need a more complex
 * parsing approach to effectively support those (which we can do if this overall approach seems
 * useful).
 */
import static com.google.common.base.Preconditions.checkArgument;

import com.google.auto.service.AutoService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.commons.lang3.StringUtils;

@AutoService(LocationSpecifierFactory.class)
@ParametersAreNonnullByDefault
public class AltFlexibleLocationSpecifierFactory implements LocationSpecifierFactory {
  public static final String NAME = AltFlexibleLocationSpecifierFactory.class.getSimpleName();

  private static final Pattern SPECIFIER_PATTERN =
      Pattern.compile("^([a-z\\.]+)\\s*\\(\\s*(.*)\\)$");
  private static final Pattern COMBINATION_PATTERN =
      Pattern.compile("^\\s*(.*)\\s+([-+])\\s+(.*)\\s*$");

  private static final String SPECIFIER_ENTER = "enter";
  private static final String SPECIFIER_INTERFACE = "interface";
  private static final String SPECIFIER_NODE_ROLE = "ref.noderole";
  private static final String SPECIFIER_VRF = "vrf";

  @Override
  public LocationSpecifier buildLocationSpecifier(@Nullable Object input) {
    if (input == null) {
      return AllInterfaceLinksLocationSpecifier.INSTANCE;
    }
    checkArgument(input instanceof String, NAME + " input must be a String");
    return parse((String) input);
  }

  @Override
  public String getName() {
    return NAME;
  }

  /** Parses the string to return a {@link LocationSpecifier}. */
  static LocationSpecifier parse(String input) {
    checkArgument(
        StringUtils.countMatches(input, '(') == StringUtils.countMatches(input, ')'),
        "Unbalanced parenthesis in input: " + input);

    String trimmedInput = input.trim();

    Matcher specifierMatcher = SPECIFIER_PATTERN.matcher(trimmedInput);
    if (specifierMatcher.find()) {
      return parseSpecifier(specifierMatcher.group(1), specifierMatcher.group(2));
    }

    Matcher combinationMatcher = COMBINATION_PATTERN.matcher(trimmedInput);
    if (combinationMatcher.find()) {
      String operator = combinationMatcher.group(2);
      if (operator.equals("+")) {
        return new UnionLocationSpecifier(
            parse(combinationMatcher.group(1)), parse(combinationMatcher.group(3)));
      } else {
        return new DifferenceLocationSpecifier(
            parse(combinationMatcher.group(1)), parse(combinationMatcher.group(3)));
      }
    }

    if (!trimmedInput.contains(":")) { // must be a node name (regex)
      return new NodeNameRegexInterfaceLocationSpecifier(
          Pattern.compile(trimmedInput, Pattern.CASE_INSENSITIVE));
    } else {
      String[] words = trimmedInput.split(":", 2);
      return new IntersectionLocationSpecifier(
          new NodeNameRegexInterfaceLocationSpecifier(
              Pattern.compile(words[0].trim(), Pattern.CASE_INSENSITIVE)),
          new NameRegexInterfaceLocationSpecifier(
              Pattern.compile(words[1].trim(), Pattern.CASE_INSENSITIVE)));
    }
  }

  private static LocationSpecifier parseSpecifier(String specifierName, String specifierInput) {

    if (specifierName.equalsIgnoreCase(SPECIFIER_ENTER)) {
      return new ToInterfaceLinkLocationSpecifier(parse(specifierInput));
    }
    if (specifierName.equalsIgnoreCase(SPECIFIER_INTERFACE)) {
      return new NameRegexInterfaceLocationSpecifier(
          Pattern.compile(specifierInput, Pattern.CASE_INSENSITIVE));
    }
    if (specifierName.equalsIgnoreCase(SPECIFIER_VRF)) {
      return new VrfNameRegexInterfaceLocationSpecifier(
          Pattern.compile(specifierInput, Pattern.CASE_INSENSITIVE));
    }
    if (specifierName.equalsIgnoreCase(SPECIFIER_NODE_ROLE)) {
      String[] words = specifierInput.split(",");
      checkArgument(
          words.length == 2,
          "Input to ref.noderole must have two strings. Yours was " + specifierInput);
      return new NodeRoleRegexInterfaceLocationSpecifier(
          words[1].trim(), Pattern.compile(words[0], Pattern.CASE_INSENSITIVE));
    }
    throw new IllegalArgumentException("Unknown specifier type: " + specifierName);
  }
}
