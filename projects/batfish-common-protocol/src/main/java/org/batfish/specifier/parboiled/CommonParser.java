package org.batfish.specifier.parboiled;

import static org.batfish.specifier.parboiled.Anchor.Type.STRING_LITERAL;

import com.google.common.collect.ImmutableMap;
import java.lang.reflect.Method;
import java.util.Map;
import org.batfish.specifier.parboiled.Anchor.Type;
import org.parboiled.BaseParser;
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
abstract class CommonParser extends BaseParser<AstNode> {

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
   *
   * <p>We use an imperative approach because lambdas run into access violations.
   */
  Rule[] initEnumRules(Object[] values) {
    Rule[] rules = new Rule[values.length];
    int index = 0;
    for (Object value : values) {
      rules[index] = IgnoreCase(value.toString());
      index++;
    }
    return rules;
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

  public Rule IpAddressUnchecked() {
    return Sequence(Number(), '.', Number(), '.', Number(), '.', Number());
  }

  public Rule IpPrefixUnchecked() {
    return Sequence(IpAddressUnchecked(), '/', Number());
  }

  /** A generic rule for non-threatening characters. */
  public Rule NameChars() {
    return FirstOf(AlphabetChar(), Dot(), Digit(), Underscore());
  }

  public Rule NameCharsAndDash() {
    return FirstOf(NameChars(), Dash());
  }

  /** [0-9]+ */
  public Rule Number() {
    return OneOrMore(Digit());
  }

  /**
   * [a-zA-Z_][-a-zA-Z0-9_]*
   *
   * <p>This spec is based on {@link org.batfish.referencelibrary.ReferenceLibrary#NAME_PATTERN}
   */
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
  public Rule Underscore() {
    return CharRange('_', '_');
  }

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
