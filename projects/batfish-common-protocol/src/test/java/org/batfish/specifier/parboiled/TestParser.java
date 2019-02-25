package org.batfish.specifier.parboiled;

import java.util.Map;
import org.batfish.specifier.parboiled.Anchor.Type;
import org.parboiled.Parboiled;
import org.parboiled.Rule;

/**
 * A shared test grammar that is used by several tests in this package. It has been designed to
 * include one instance of all types of features our real grammars use.
 */
@SuppressWarnings({
  "checkstyle:methodname", // this class uses idiomatic names
  "WeakerAccess", // access of Rule methods is needed for parser auto-generation.
})
class TestParser extends CommonParser {

  public static final TestParser INSTANCE = Parboiled.createParser(TestParser.class);

  public static final Map<String, Type> ANCHORS = initAnchors(TestParser.class);

  /**
   * Test grammar
   *
   * <pre>
   * testExpr := testTerm [, testTerm]*
   *
   * testTerm := @specifier(specifierInput)
   *               | (testTerm)
   *               | ! testTerm
   *               | testBase
   *
   * specifierInput := REFERENCE_OBJECT_NAME_LITERAL
   *
   * testBase := IP_ADDRESS
   * </pre>
   */

  /* An Test expression is a comma-separated list of TestTerms */
  public Rule TestExpression() {
    return Sequence(TestTerm(), WhiteSpace(), ZeroOrMore(", ", TestTerm(), WhiteSpace()));
  }

  /* An Test term is one of these things */
  public Rule TestTerm() {
    return FirstOf(
        TestParens(),
        TestSpecifier(),
        TestNotOp(),
        TestIpRange(),
        TestIpAddress(),
        TestNameRegexDeprecated(),
        TestNameRegex(),
        TestName());
  }

  public Rule TestParens() {
    return Sequence("( ", TestTerm(), ") ");
  }

  public Rule TestSpecifier() {
    return Sequence("@specifier ", "( ", TestSpecifierInput(), ") ");
  }

  public Rule TestNotOp() {
    return Sequence("! ", TestNot("! "), TestTerm());
  }

  @Anchor(Type.ADDRESS_GROUP_AND_BOOK)
  public Rule TestSpecifierInput() {
    return Sequence(
        ReferenceObjectNameLiteral(),
        WhiteSpace(),
        ", ",
        ReferenceObjectNameLiteral(),
        WhiteSpace());
  }

  /** An instance of base dynamic value */
  @Anchor(Type.IP_ADDRESS)
  public Rule TestIpAddress() {
    return IpAddressUnchecked();
  }

  /** An instance of complex dynamic value */
  @Anchor(Type.IP_RANGE)
  public Rule TestIpRange() {
    return Sequence(TestIpAddress(), WhiteSpace(), "- ", TestIpAddress());
  }

  @Anchor(Type.NODE_NAME)
  public Rule TestName() {
    return NameLiteral();
  }

  @Anchor(Type.NODE_NAME_REGEX)
  public Rule TestNameRegex() {
    return Regex();
  }

  @Anchor(Type.IGNORE)
  public Rule TestNameRegexDeprecated() {
    return RegexDeprecated();
  }
}
