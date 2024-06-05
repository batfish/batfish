package org.batfish.grammar.recovery_inline_alts;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.batfish.grammar.BatfishParseTreeWalker;
import org.batfish.grammar.GrammarSettings;
import org.batfish.grammar.MockGrammarSettings;
import org.junit.Test;

/**
 * Test of recovery using grammar that inlines lists of alternatives in the rule in which they are
 * used.
 */
public final class RecoveryInlineAltsGrammarTest {

  @Test
  public void testRecoveryDepth1() {
    RecoveryInlineAltsExtractor extractor = parse("errors_at_depth1");

    assertThat(extractor.getInterfaceCount(), equalTo(1));
    assertThat(extractor.getInterfaceIpOspfCostCount(), equalTo(1));
    assertThat(extractor.getIpRoutingCount(), equalTo(5));
    assertThat(extractor.getPermitCount(), equalTo(5));
  }

  @Test
  public void testRecoveryDepth2() {
    RecoveryInlineAltsExtractor extractor = parse("errors_at_depth2");

    assertThat(extractor.getInterfaceCount(), equalTo(1));
    assertThat(extractor.getInterfaceIpOspfCostCount(), equalTo(1));
    assertThat(extractor.getInterfaceMtuCount(), equalTo(4));
    assertThat(extractor.getInterfacePermitCount(), equalTo(6));
    assertThat(extractor.getIpRoutingCount(), equalTo(2));
  }

  private static final String RESOURCE_PREFIX = "org/batfish/grammar/recovery_alts/";

  private static final GrammarSettings SETTINGS =
      MockGrammarSettings.builder().setThrowOnLexerError(true).setThrowOnParserError(true).build();

  private static RecoveryInlineAltsExtractor parse(String filename) {
    String text = readResource(RESOURCE_PREFIX + filename, UTF_8);
    RecoveryInlineAltsCombinedParser cp = new RecoveryInlineAltsCombinedParser(text, SETTINGS);
    ParserRuleContext ctx = cp.parse();
    RecoveryInlineAltsExtractor extractor = new RecoveryInlineAltsExtractor();
    ParseTreeWalker walker = new BatfishParseTreeWalker(cp);
    walker.walk(extractor, ctx);
    return extractor;
  }
}
