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
 * <p>They should also not contain any explicit of implicit anchors. Because character literals are
 * implicit anchors, we implement them using a character range below.
 */
@SuppressWarnings({
  "checkstyle:methodname", // this class uses idiomatic names
  "WeakerAccess", // access of Rule methods is needed for parser auto-generation.
})
abstract class CommonParser extends BaseParser<AstNode> {

  static Map<String, Type> initCompletionTypes(Class<?> parserClass) {
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
   * Shared entry point for all expressions.
   *
   * <p>The parameter {@code expression} specifies the type of expression we want to parse.
   */
  public Rule input(Rule expression) {
    return Sequence(WhiteSpace(), expression, WhiteSpace(), EOI);
  }

  public Rule Dash() {
    return CharRange('-', '-');
  }

  /** [a-z] + [A-Z] */
  public Rule AlphabetChar() {
    return FirstOf(CharRange('a', 'z'), CharRange('A', 'Z'));
  }

  /** [0-9] */
  public Rule Digit() {
    return CharRange('0', '9');
  }

  public Rule IpAddressUnchecked() {
    return Sequence(Number(), '.', Number(), '.', Number(), '.', Number());
  }

  public Rule IpPrefixUnchecked() {
    return Sequence(IpAddressUnchecked(), '/', Number());
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
