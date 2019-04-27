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

  static TestParser instance() {
    return Parboiled.createParser(TestParser.class);
  }

  public static final Map<String, Type> ANCHORS = initAnchors(TestParser.class);

  Rule getInputRule() {
    return input(TestSpec());
  }
  /**
   * Test grammar
   *
   * <pre>
   * testSpec := testTerm [, testTerm]*
   *
   * testTerm := @specifier(address-group, reference-book)
   *               | (testSpec)
   *               | ! testTerm
   *               | ip range
   *               | ip address
   *               | node name
   *               | node name regex
   *               | node name regex deprecated
   * </pre>
   */

  /* An Test expression is a comma-separated list of TestTerms */
  public Rule TestSpec() {
    return Sequence(TestTerm(), WhiteSpace(), ZeroOrMore(", ", TestTerm(), WhiteSpace()));
  }

  /* An Test term is one of these things */
  public Rule TestTerm() {
    return FirstOf(
        TestParens(),
        TestFunc(),
        TestNotOp(),
        TestIpRange(),
        TestIpAddress(),
        TestNameRegexDeprecated(),
        TestNameRegex(),
        TestName());
  }

  public Rule TestParens() {
    return Sequence("( ", TestSpec(), ") ");
  }

  public Rule TestFunc() {
    return Sequence(IgnoreCase("@specifier"), WhiteSpace(), "( ", TestSpecifierInput(), ") ");
  }

  public Rule TestNotOp() {
    return Sequence("! ", TestNot("! "), TestTerm());
  }

  @Anchor(Type.ADDRESS_GROUP_AND_REFERENCE_BOOK)
  public Rule TestSpecifierInput() {
    return Sequence(TestAddressGroupName(), ", ", TestReferenceBookName());
  }

  @Anchor(Type.ADDRESS_GROUP_NAME)
  public Rule TestAddressGroupName() {
    return Sequence(NameLiteral(), WhiteSpace());
  }

  @Anchor(Type.REFERENCE_BOOK_NAME)
  public Rule TestReferenceBookName() {
    return Sequence(NameLiteral(), WhiteSpace());
  }

  /** An instance of base dynamic value */
  @Anchor(Type.IP_ADDRESS)
  public Rule TestIpAddress() {
    return Sequence(IpAddressUnchecked(), push(new IpAstNode(match())));
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
