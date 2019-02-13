package org.batfish.specifier.parboiled;

import static org.batfish.specifier.parboiled.Completion.Type.STRING_LITERAL;

import com.google.common.collect.ImmutableMap;
import java.lang.reflect.Method;
import java.util.Map;
import org.batfish.specifier.parboiled.Completion.Type;
import org.parboiled.BaseParser;
import org.parboiled.Rule;

/**
 * This class contains common matchers for different types of expressions.
 *
 * <p>The rules in this class should not put anything on the stack.
 */
@SuppressWarnings({
  "checkstyle:methodname", // this class uses idiomatic names
  "WeakerAccess", // access of Rule methods is needed for parser auto-generation.
})
abstract class CommonParser extends BaseParser<AstNode> {

  static Map<String, Type> initCompletionTypes(Class<?> parserClass) {
    ImmutableMap.Builder<String, Completion.Type> completionTypes = ImmutableMap.builder();
    // fromStringLiteral is not enumerated because its protected; so we add explicitly
    completionTypes.put("fromStringLiteral", Completion.Type.STRING_LITERAL);
    // EOI does not have an explicit rule; so we add explicitly
    completionTypes.put("EOI", Completion.Type.EOI);
    for (Method method : parserClass.getMethods()) {
      Completion annotation = method.getAnnotation(Completion.class);
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
        FirstOf(AlphabetChar(), Ch('_')),
        ZeroOrMore(FirstOf(AlphabetChar(), Ch('_'), Digit(), Ch('-'))));
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
  @Completion(STRING_LITERAL)
  @Override
  protected Rule fromStringLiteral(String string) {
    return string.endsWith(" ")
        ? Sequence(String(string.substring(0, string.length() - 1)), WhiteSpace())
        : String(string);
  }
}
