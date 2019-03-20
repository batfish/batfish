package org.batfish.specifier.parboiled;

import static org.batfish.specifier.parboiled.Anchor.Type.STRING_LITERAL;

import com.google.common.collect.ImmutableMap;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import org.batfish.datamodel.IpProtocol;
import org.batfish.specifier.parboiled.Anchor.Type;
import org.parboiled.BaseParser;
import org.parboiled.Parboiled;
import org.parboiled.Rule;

/**
 * This class contains common matchers for different types of expressions.
 *
 * <p>As a general rule, the rules in this class should not put anything on the stack. In cases
 * where they do, make sure that the rule javadoc says so.
 *
 * <p>They should also not contain any explicit of implicit anchors. One implication of this:
 * because character literals are implicit anchors, we implement them using a character range below.
 */
@SuppressWarnings({
  "checkstyle:methodname", // this class uses idiomatic names
  "WeakerAccess", // access of Rule methods is needed for parser auto-generation.
})
public class CommonParser extends BaseParser<AstNode> {

  /**
   * Characters that we deem special in our grammar and cannot appear in unquoted names. We are
   * currently using the first bunch and setting aside some more for future use.
   *
   * <p>Once we stop supporting now-deprecated regexes, '*' should probably added to the reserved
   * list.
   */
  private static final String SPECIAL_CHARS = " \t,\\&()[]@" + "!#$%^;?<>={}";

  public static final CommonParser INSTANCE = Parboiled.createParser(CommonParser.class);

  static Map<String, Type> initAnchors(Class<?> parserClass) {
    ImmutableMap.Builder<String, Anchor.Type> completionTypes = ImmutableMap.builder();
    // Explicitly add EOI
    completionTypes.put("EOI", Anchor.Type.EOI);
    for (Method method : parserClass.getMethods()) {
      Anchor annotation = method.getAnnotation(Anchor.class);
      if (annotation != null) {
        completionTypes.put(method.getName(), annotation.value());
      }
    }
    return completionTypes.build();
  }

  /**
   * Initialize an array of case-insenstive rules that match the array of provided values (e.g.,
   * those belonging to an Enum).
   */
  Rule[] initEnumRules(Object[] values) {
    return Arrays.stream(values).map(Object::toString).map(this::IgnoreCase).toArray(Rule[]::new);
  }

  /** Initialize an array of rules that match known IpProtocol names */
  Rule[] initIpProtocolNameRules() {
    return Arrays.stream(IpProtocol.values())
        .map(Object::toString)
        .filter(p -> !p.startsWith("UNNAMED"))
        .map(this::IgnoreCase)
        .toArray(Rule[]::new);
  }

  /**
   * Shared entry point for all expressions.
   *
   * <p>The parameter {@code expression} specifies the type of expression we want to parse.
   */
  public Rule input(Rule expression) {
    return Sequence(WhiteSpace(), expression, WhiteSpace(), EOI);
  }

  /** [a-z] + [A-Z] */
  public Rule AlphabetChar() {
    return FirstOf(CharRange('a', 'z'), CharRange('A', 'Z'));
  }

  /** A rule that allows for all ASCII characters except for those in {@code characters} */
  @Anchor(Type.IGNORE)
  public Rule AsciiButNot(String characters) {
    return Sequence(Test(CharRange((char) 0, (char) 127)), NoneOf(characters));
  }

  /** See class JavaDoc for why this is a CharRange and not Ch */
  public Rule At() {
    return CharRange('@', '@');
  }

  /** See class JavaDoc for why this is a CharRange and not Ch */
  public Rule Dash() {
    return CharRange('-', '-');
  }

  /** [0-9] */
  public Rule Digit() {
    return CharRange('0', '9');
  }

  @Anchor(Type.IGNORE)
  public Rule EscapedSlash() {
    return String("\\/");
  }

  @Anchor(Type.IGNORE)
  public Rule EscapedQuote() {
    return String("\\\"");
  }

  public Rule IpAddressUnchecked() {
    return Sequence(Number(), '.', Number(), '.', Number(), '.', Number());
  }

  public Rule IpPrefixUnchecked() {
    return Sequence(IpAddressUnchecked(), '/', Number());
  }

  /**
   * A shared rule for a range of a names. Allow unquoted strings for names that 1) don't contain
   * one of the {@link #SPECIAL_CHARS} in our grammar, 2) don't begin with a digit (to avoid
   * confusion with IP addresses), and 3) don't begin with '/' (to avoid confusion with regexes).
   * Otherwise, double quotes are needed.
   *
   * <p>This rule puts a {@link StringAstNode} with the parsed name on the stack.
   */
  public Rule NameLiteral() {
    return FirstOf(
        Sequence(
            TestNot('"'),
            TestNot(Digit()),
            TestNot(Slash()),
            OneOrMore(AsciiButNot(SPECIAL_CHARS)),
            push(new StringAstNode(match()))),
        Sequence(
            '"',
            ZeroOrMore(FirstOf(EscapedQuote(), AsciiButNot("\""))),
            push(new StringAstNode(match())),
            '"'));
  }

  /** Keep in sync with {@link org.batfish.datamodel.Names.Type#NODE_ROLE} */
  public Rule NodeRoleNameLiteral() {
    return Sequence(
        FirstOf(AlphabetChar(), Digit(), Underscore()),
        ZeroOrMore(FirstOf(AlphabetChar(), Digit(), Underscore(), Dash())));
  }

  /** [0-9]+ */
  public Rule Number() {
    return OneOrMore(Digit());
  }

  /** Keep in sync with {@link org.batfish.datamodel.Names.Type#REFERENCE_OBJECT} */
  public Rule ReferenceObjectNameLiteral() {
    return Sequence(
        FirstOf(AlphabetChar(), Underscore()),
        ZeroOrMore(FirstOf(AlphabetChar(), Underscore(), Digit(), Dash())));
  }

  /**
   * A rule for regexes enclosed in slashes. Anything can appear in the interior of a regex except
   * that '/' (47) should be escaped.
   *
   * <p>This rule puts in the regex on the stack (without the enclosing '/'s)
   */
  public Rule Regex() {
    return Sequence(
        '/',
        OneOrMore(FirstOf(EscapedSlash(), AsciiButNot("/"))),
        push(new StringAstNode(match())),
        '/');
  }

  public Rule ContainsChar(char character) {
    return Sequence(
        ZeroOrMore(AsciiButNot(Character.toString(character))),
        character,
        ZeroOrMore(AsciiButNot(Character.toString(character))));
  }

  /**
   * We infer deprecated (non-enclosed) regexes as strings that: 1) don't begin with double quote,
   * digit, slash; 2) do not contain {@link #SPECIAL_CHARS}; and 3) contain '*'.
   */
  public Rule RegexDeprecated() {
    return Sequence(
        TestNot('"'),
        TestNot(Digit()),
        TestNot(Slash()),
        Sequence(
            OneOrMore(AsciiButNot(SPECIAL_CHARS + "*")),
            "*",
            ZeroOrMore(AsciiButNot(SPECIAL_CHARS))),
        push(new StringAstNode(match())));
  }

  /** See class JavaDoc for why this is a CharRange and not Ch */
  public Rule Slash() {
    return CharRange('/', '/');
  }

  /** See class JavaDoc for why this is a CharRange and not Ch */
  public Rule Tilde() {
    return CharRange('~', '~');
  }

  /** See class JavaDoc for why this is a CharRange and not Ch */
  public Rule Underscore() {
    return CharRange('_', '_');
  }

  @Anchor(Type.WHITESPACE)
  public Rule WhiteSpace() {
    return ZeroOrMore(AnyOf(" \t"));
  }

  /**
   * Redefine rule creation for string literals.
   *
   * <p>We automatically match trailing whitespace, so we don't have to insert extra Whitespace()
   * rules after each character or string literal
   */
  @Anchor(STRING_LITERAL)
  @Override
  protected Rule fromStringLiteral(String string) {
    return string.endsWith(" ")
        ? Sequence(String(string.substring(0, string.length() - 1)), WhiteSpace())
        : String(string);
  }
}
