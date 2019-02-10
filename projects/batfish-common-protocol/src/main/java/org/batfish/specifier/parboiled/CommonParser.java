package org.batfish.specifier.parboiled;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.parboiled.BaseParser;
import org.parboiled.Rule;

/**
 * This class contains common matchers for different types of expressions.
 *
 * <p>The rules in this class have two invariants:
 *
 * <ol>
 *   <li>They should not put anything on the stack.
 *   <li>They are ignored for the purposes of error reporting and completion suggestion (via
 *       inclusion in the {@link #COMMON_LABELS} set).
 * </ol>
 *
 * <p>Any newly-defined rules should be added to the {@link #COMMON_LABELS} set.
 */
@SuppressWarnings({
  "InfiniteRecursion",
  "checkstyle:methodname", // this class uses idiomatic names
  "WeakerAccess", // access of Rule methods is needed for parser auto-generation.
})
public abstract class CommonParser extends BaseParser<Object> {

  // TODO: Can we populate this list automatically?
  public static Set<String> COMMON_LABELS =
      ImmutableSet.of(
          "AlphabetChar",
          "Digit",
          "IpAddressUnchecked",
          "IpPrefixUnchecked",
          "Number",
          "ReferenceObjectNameLiteral");

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
    return ZeroOrMore(AnyOf(" \t\f"));
  }

  /**
   * Redefine rule creation for string literals.
   *
   * <p>We automatically match trailing whitespace, so we don't have to insert extra Whitespace()
   * rules after each character or string literal
   */
  @Override
  protected Rule fromStringLiteral(String string) {
    return string.endsWith(" ")
        ? Sequence(String(string.substring(0, string.length() - 1)), WhiteSpace())
        : String(string);
  }
}
