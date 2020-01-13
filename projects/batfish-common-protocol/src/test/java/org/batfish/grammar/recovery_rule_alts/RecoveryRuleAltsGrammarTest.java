package org.batfish.grammar.recovery_rule_alts;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.batfish.common.util.CommonUtil;
import org.batfish.grammar.BatfishParseTreeWalker;
import org.batfish.grammar.GrammarSettings;
import org.batfish.grammar.MockGrammarSettings;
import org.junit.Test;

/** Test of recovery using grammar that puts lists of alternatives in separate rules. */
public final class RecoveryRuleAltsGrammarTest {

  @Test
  public void testRecoveryDepth1() {
    RecoveryRuleAltsExtractor extractor = parse("errors_at_depth1");

    assertThat(extractor.getInterfaceCount(), equalTo(1));
    assertThat(extractor.getInterfaceIpOspfCostCount(), equalTo(1));
    assertThat(extractor.getIpRoutingCount(), equalTo(5));
  }

  @Test
  public void testRecoveryRuleAlts() {
    RecoveryRuleAltsExtractor extractor = parse("errors_at_depth2");

    assertThat(extractor.getInterfaceCount(), equalTo(1));
    assertThat(extractor.getInterfaceIpOspfCostCount(), equalTo(1));
    assertThat(extractor.getInterfaceMtuCount(), equalTo(4));
    assertThat(extractor.getIpRoutingCount(), equalTo(2));
  }

  private static final String RESOURCE_PREFIX = "org/batfish/grammar/recovery_alts/";

  private static final GrammarSettings SETTINGS =
      MockGrammarSettings.builder().setThrowOnLexerError(true).setThrowOnParserError(true).build();

  private static RecoveryRuleAltsExtractor parse(String filename) {
    String text = CommonUtil.readResource(RESOURCE_PREFIX + filename);
    RecoveryRuleAltsCombinedParser cp = new RecoveryRuleAltsCombinedParser(text, SETTINGS);
    ParserRuleContext ctx = cp.parse();
    RecoveryRuleAltsExtractor extractor = new RecoveryRuleAltsExtractor();
    ParseTreeWalker walker = new BatfishParseTreeWalker(cp);
    walker.walk(extractor, ctx);
    return extractor;
  }
}
