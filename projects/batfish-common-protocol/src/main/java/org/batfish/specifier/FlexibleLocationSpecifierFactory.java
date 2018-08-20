package org.batfish.specifier;

/**
 * A {@link LocationSpecifierFactory} that parses a specification from a string.
 *
 * <p>Example values:
 *
 * <pre>
 * rtr-.*[Eth.*] --> at interfaces matching the second regex on nodes matching the first
 * rtr-.* --> equivalent to rtr-.*[.*]
 * [Eth.*] --> equivalent to .*[Eth.*]
 * [vrf(default)] --> at all interface in the default VRF
 * enter(.....) --> …. Can be any of the above; enter changes the meaning to inbound
 * ref.noderole(border.*, dim) -> border nodes in dimension dim
 * a + b --> union of locations denoted by a and b
 * a - b --> difference of locations denoted a and b
 * </pre>
 *
 * <p>Grammar
 *
 * <pre>
 * VALUE ::= ATOMIC_VALUE | COMBINATION | FUNC
 * ATOMIC_VALUE ::= NS?([IS])?
 * COMBINATION ::= VALUE [+-] VALUE
 * FUNC ::= enter(VALUE) | exit(VALUE)
 * NS ::= <Grammar of FlexibleNodesSpecifierFactory>
 * IS ::= <Grammar of FlexibleInterfaceSpecifierFactory>
 * </pre>
 *
 * <p>NB: The space around '+' and '-' is mandatory and is used to split the string properly, and
 * nested expressions a + (b + c) are not currently supported. Further, 'enter(..)' and 'exit(...)'
 * are assumed to not be valid FlexibleNodesSpecifierFactory inputs. We need a more complex *
 * parsing approach to effectively support those (which we can do if this overall approach seems *
 * useful).
 */
import static com.google.common.base.Preconditions.checkArgument;

import com.google.auto.service.AutoService;
import com.google.common.base.Strings;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.commons.lang3.StringUtils;

@AutoService(LocationSpecifierFactory.class)
@ParametersAreNonnullByDefault
public class FlexibleLocationSpecifierFactory implements LocationSpecifierFactory {
  public static final String NAME = FlexibleLocationSpecifierFactory.class.getSimpleName();

  private static final String FUNC_ENTER = "enter";
  private static final String FUNC_EXIT = "exit";
  private static final String FUNC_REGEX = FUNC_ENTER + "|" + FUNC_EXIT;

  private static final Pattern ATOMIC_PATTERN = Pattern.compile("^([^\\[]*)\\s*(\\[.*\\])?$");
  private static final Pattern COMBINATION_PATTERN = Pattern.compile("^(.*)\\s+([-+])\\s+(.*)$");
  private static final Pattern FUNC_PATTERN =
      Pattern.compile("^(" + FUNC_REGEX + ")\\s*\\(\\s*(.*)\\s*\\)$");

  @Override
  public LocationSpecifier buildLocationSpecifier(@Nullable Object input) {
    if (input == null) {
      return AllInterfacesLocationSpecifier.INSTANCE;
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
    checkArgument(
        StringUtils.countMatches(input, '[') == StringUtils.countMatches(input, ']'),
        "Unbalanced brackets in input: " + input);

    String trimmedInput = input.trim();

    Matcher funcMatcher = FUNC_PATTERN.matcher(trimmedInput);
    if (funcMatcher.find()) {
      return parseFunc(funcMatcher.group(1), funcMatcher.group(2));
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

    Matcher atomicMatcher = ATOMIC_PATTERN.matcher(trimmedInput);
    if (atomicMatcher.find()) {
      String nodesInput = atomicMatcher.group(1).trim();
      String interfacesInput = atomicMatcher.group(2);
      if (Strings.isNullOrEmpty(interfacesInput)) {
        checkArgument(
            !nodesInput.isEmpty(),
            "Both nodes and interfaces part of the flexible location specifier cannot be empty");
        return new NodeSpecifierInterfaceLocationSpecifier(
            new FlexibleNodeSpecifierFactory().buildNodeSpecifier(nodesInput));
      }
      // remove '[' and ']'
      interfacesInput = interfacesInput.trim();
      interfacesInput = interfacesInput.substring(1, interfacesInput.length() - 1);
      if (nodesInput.isEmpty()) {
        return new InterfaceSpecifierInterfaceLocationSpecifier(
            new FlexibleInterfaceSpecifierFactory().buildInterfaceSpecifier(interfacesInput));
      }

      return new IntersectionLocationSpecifier(
          new NodeSpecifierInterfaceLocationSpecifier(
              new FlexibleNodeSpecifierFactory().buildNodeSpecifier(nodesInput)),
          new InterfaceSpecifierInterfaceLocationSpecifier(
              new FlexibleInterfaceSpecifierFactory().buildInterfaceSpecifier(interfacesInput)));
    }

    throw new IllegalArgumentException("Could not process input: " + trimmedInput);
  }

  private static LocationSpecifier parseFunc(String funcName, String specifierInput) {

    if (funcName.equalsIgnoreCase(FUNC_ENTER)) {
      return new ToInterfaceLinkLocationSpecifier(parse(specifierInput));
    }
    if (funcName.equalsIgnoreCase(FUNC_EXIT)) {
      throw new UnsupportedOperationException(FUNC_EXIT + "() is not currently supported");
    }
    throw new IllegalArgumentException("Unknown function type: " + funcName);
  }
}
