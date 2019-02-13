package org.batfish.specifier.parboiled;

import static org.batfish.specifier.parboiled.Parser.initCompletionTypes;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.common.collect.ImmutableMap;
import org.batfish.specifier.parboiled.Completion.Type;
import org.junit.Test;
import org.parboiled.Rule;

public class CommonParserTest {

  @SuppressWarnings({
    "checkstyle:methodname", // this class uses idiomatic names
    "WeakerAccess", // access of Rule methods is needed for parser auto-generation.
  })
  static class TestParser extends CommonParser {

    public Rule input(Rule expression) {
      return Sequence(WhiteSpace(), expression, WhiteSpace(), EOI);
    }

    /**
     * Test grammar
     *
     * <pre>
     * testExpr := testTerm [, testTerm]*
     *
     * testTerm := specifier(argument)
     *               | (testTerm)
     *               | ! testTerm
     *               | testBase
     * </pre>
     */

    /* An Test expression is a comma-separated list of TestTerms */
    public Rule TestExpression() {
      return Sequence(TestTerm(), WhiteSpace(), ZeroOrMore(", ", TestTerm(), WhiteSpace(), EOI));
    }

    /* An Test term is one of these things */
    public Rule TestTerm() {
      return FirstOf(TestParens(), TestSpecifier(), TestNotOp(), TestBase());
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

    @Completion(Type.ADDRESS_GROUP_AND_BOOK)
    public Rule TestSpecifierInput() {
      return Sequence(ReferenceObjectNameLiteral(), WhiteSpace());
    }

    @Completion(Type.IP_ADDRESS)
    public Rule TestBase() {
      return IpAddressUnchecked();
    }
  }

  @Test
  public void testInitCompletionTypes() {
    assertThat(
        initCompletionTypes(TestParser.class),
        equalTo(
            ImmutableMap.of(
                "TestBase",
                Type.IP_ADDRESS,
                "TestSpecifierInput",
                Type.ADDRESS_GROUP_AND_BOOK,
                "fromStringLiteral",
                Type.STRING_LITERAL)));
  }
}
