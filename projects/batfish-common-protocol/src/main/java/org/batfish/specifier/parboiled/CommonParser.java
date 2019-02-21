package org.batfish.specifier.parboiled;

import static org.batfish.specifier.parboiled.Anchor.Type.STRING_LITERAL;

import com.google.common.collect.ImmutableMap;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import org.batfish.specifier.parboiled.Anchor.Type;
import org.parboiled.BaseParser;
import org.parboiled.Parboiled;
import org.parboiled.Rule;

/**
 * This class contains common matchers for different types of expressions.
 *
 * <p>The rules in this class should not put anything on the stack.
 *
 * <p>They should also not contain any explicit of implicit anchors. One implication of this:
 * because character literals are implicit anchors, we implement them using a character range below.
 */
@SuppressWarnings({
  "checkstyle:methodname", // this class uses idiomatic names
  "WeakerAccess", // access of Rule methods is needed for parser auto-generation.
})
public class CommonParser extends BaseParser<AstNode> {

  public static final CommonParser INSTANCE = Parboiled.createParser(CommonParser.class);

  static Map<String, Type> initAnchors(Class<?> parserClass) {
    ImmutableMap.Builder<String, Anchor.Type> completionTypes = ImmutableMap.builder();
    // Explicitly add WHITESPACE and EOI
    completionTypes.put("WhiteSpace", Type.WHITESPACE);
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

  /** See class JavaDoc for why this is a CharRange and not Ch */
  public Rule Colon() {
    return CharRange(':', ':');
  }

  /** See class JavaDoc for why this is a CharRange and not Ch */
  public Rule Dash() {
    return CharRange('-', '-');
  }

  /** [0-9] */
  public Rule Digit() {
    return CharRange('0', '9');
  }

  /** See class JavaDoc for why this is a CharRange and not Ch */
  public Rule Dot() {
    return CharRange('.', '.');
  }

  /** Keep in Sync with {@link org.batfish.datamodel.Names.ObjectType#FILTER} */
  public Rule FilterNameLiteral() {
    return Sequence(
        FirstOf(AlphabetChar(), Tilde(), Underscore()),
        ZeroOrMore(FirstOf(AlphabetChar(), Tilde(), Underscore(), Dash(), Digit(), Dot())));
  }

  /** Keep in sync with {@link org.batfish.datamodel.Names.ObjectType#INTERFACE} */
  public Rule InterfaceNameLiteral() {
    return Sequence(
        AlphabetChar(),
        ZeroOrMore(FirstOf(AlphabetChar(), Colon(), Dash(), Digit(), Dot(), Slash())));
  }

  public Rule IpAddressUnchecked() {
    return Sequence(Number(), '.', Number(), '.', Number(), '.', Number());
  }

  public Rule IpPrefixUnchecked() {
    return Sequence(IpAddressUnchecked(), '/', Number());
  }

  /** Keep in sync with {@link org.batfish.datamodel.Names.ObjectType#NODE} */
  public Rule NodeNameLiteral() {
    return Sequence(
        AlphabetChar(), ZeroOrMore(FirstOf(AlphabetChar(), Dash(), Dot(), Digit(), Underscore())));
  }

  /** [0-9]+ */
  public Rule Number() {
    return OneOrMore(Digit());
  }

  /** Keep in sync with {@link org.batfish.datamodel.Names.ObjectType#REFERENCE_OBJECT} */
  public Rule ReferenceObjectNameLiteral() {
    return Sequence(
        FirstOf(AlphabetChar(), Underscore()),
        ZeroOrMore(FirstOf(AlphabetChar(), Underscore(), Digit(), Dash())));
  }

  /** Anything can appear in the interior of a regex except that '/' (47) should be escaped */
  public Rule Regex() {
    return OneOrMore(
        FirstOf("\\/", CharRange((char) 0, (char) 46), CharRange((char) 48, (char) 127)));
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

  /** Keep in Sync with {@link org.batfish.datamodel.Names.ObjectType#VRF} */
  public Rule VrfNameLiteral() {
    return Sequence(
        FirstOf(AlphabetChar(), Underscore()),
        ZeroOrMore(FirstOf(AlphabetChar(), Underscore(), Dash(), Digit())));
  }

  public Rule WhiteSpace() {
    return ZeroOrMore(AnyOf(" \t"));
  }

  /** Keep in Sync with {@link org.batfish.datamodel.Names.ObjectType#ZONE} */
  public Rule ZoneNameLiteral() {
    return Sequence(
        FirstOf(AlphabetChar(), Underscore()),
        ZeroOrMore(FirstOf(AlphabetChar(), Underscore(), Dash(), Digit())));
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
