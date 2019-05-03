package org.batfish.specifier.parboiled;

import static org.batfish.datamodel.Names.SPECIAL_CHARS;
import static org.batfish.specifier.parboiled.Anchor.Type.STRING_LITERAL;

import com.google.common.collect.ImmutableMap;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.IpProtocol;
import org.batfish.specifier.parboiled.Anchor.Type;
import org.parboiled.BaseParser;
import org.parboiled.Context;
import org.parboiled.Parboiled;
import org.parboiled.Rule;
import org.parboiled.support.DefaultValueStack;
import org.parboiled.support.ValueStack;

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
@ParametersAreNonnullByDefault
public abstract class CommonParser extends BaseParser<AstNode> {

  /**
   * Parboiled parser runners reset the value stack for invalid inputs. We save it externally (by
   * overriding the push function) so we can examine it later, e.g., for context-sensitive auto
   * completion.
   */
  static class ShadowStack {
    private ValueStack<AstNode> _vs;
    private int _currentIndex;
    private boolean _repeatedRun;

    ShadowStack() {
      this(new DefaultValueStack<>());
    }

    ShadowStack(ValueStack<AstNode> vs) {
      _vs = vs;
      _currentIndex = 0;
      _repeatedRun = false;
    }

    public void save(Context<AstNode> context) {
      if (_repeatedRun) {
        return;
      }
      if (_currentIndex > context.getCurrentIndex()) {
        _repeatedRun = true;
        return;
      }
      _vs = new DefaultValueStack<>(context.getValueStack());
      _currentIndex = context.getCurrentIndex();
    }

    public ValueStack<AstNode> getValueStack() {
      return _vs;
    }
  }

  private ShadowStack _shadowStack = new ShadowStack();

  @Override
  public boolean push(AstNode node) {
    boolean returnValue = super.push(node);
    _shadowStack.save(getContext());
    return returnValue;
  }

  public ShadowStack getShadowStack() {
    return _shadowStack;
  }

  /** Useful for testing */
  void setShadowStack(ShadowStack shadowStack) {
    _shadowStack = shadowStack;
  }

  /** Get the main entry point for {@code grammar} */
  abstract Rule getInputRule(Grammar grammar);

  static CommonParser instance() {
    return Parboiled.createParser(CommonParser.class);
  }

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
   * Whether this anchor type supports escaped names, via {@link CommonParser#NameLiteral()}. This
   * information is used later--auto complete suggestions are escaped if needed.
   */
  static boolean isEscapableNameAnchor(Anchor.Type anchorType) {
    switch (anchorType) {
      case ADDRESS_GROUP_NAME:
      case FILTER_NAME:
      case INTERFACE_GROUP_NAME:
      case INTERFACE_NAME:
      case NODE_NAME:
      case NODE_ROLE_NAME:
      case NODE_ROLE_DIMENSION_NAME:
      case REFERENCE_BOOK_NAME:
      case ROUTING_POLICY_NAME:
      case VRF_NAME:
      case ZONE_NAME:
        return true;
      default:
        return false;
    }
  }

  /**
   * Whether {@code literal} represents an operator that has values on the right hand side. This
   * information is used to provide hints and description for auto completion suggestions
   */
  static boolean isOperatorWithRhs(String literal) {
    switch (literal) {
      case "(":
      case "/":
      case "!":
      case "-":
      case ":":
      case "[":
        return true;
      default:
        return false;
    }
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

  /** [0-9] */
  public Rule Digit() {
    return CharRange('0', '9');
  }

  /** See class JavaDoc for why this is a CharRange and not Ch */
  public Rule Dot() {
    return CharRange('.', '.');
  }

  /** See class JavaDoc for why this is a CharRange and not Ch */
  public Rule EscapeChar() {
    return CharRange('"', '"');
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
    return Sequence(Number(), Dot(), Number(), Dot(), Number(), Dot(), Number());
  }

  public Rule IpPrefixUnchecked() {
    return Sequence(IpAddressUnchecked(), Slash(), Number());
  }

  /**
   * A shared rule for a range of a names. Allow unquoted strings for names that 1) don't contain
   * one of the {@link org.batfish.datamodel.Names#SPECIAL_CHARS} in our grammar, 2) don't begin
   * with a digit (to avoid confusion with IP addresses), and 3) don't begin with '/' (to avoid
   * confusion with regexes). Otherwise, double quotes are needed.
   *
   * <p>This rule puts a {@link StringAstNode} with the parsed name on the stack.
   */
  public Rule NameLiteral() {
    return FirstOf(
        Sequence(
            TestNot(EscapeChar()),
            TestNot(Digit()),
            TestNot(Slash()),
            OneOrMore(AsciiButNot(SPECIAL_CHARS)),
            push(new StringAstNode(match()))),
        Sequence(
            EscapeChar(),
            OneOrMore(FirstOf(EscapedQuote(), AsciiButNot("\""))),
            push(new StringAstNode(match())),
            EscapeChar()));
  }

  /** [0-9]+ */
  public Rule Number() {
    return OneOrMore(Digit());
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
        push(new RegexAstNode(match())),
        '/');
  }

  /**
   * We infer deprecated (non-enclosed) regexes as strings that: 1) don't begin with double quote,
   * digit, slash; 2) do not contain {@link org.batfish.datamodel.Names#SPECIAL_CHARS}; and 3)
   * contain '*'.
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
        push(new RegexAstNode(match())));
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
