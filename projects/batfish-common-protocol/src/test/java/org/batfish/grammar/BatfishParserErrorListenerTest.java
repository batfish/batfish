package org.batfish.grammar;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.batfish.common.util.CommonUtil;
import org.batfish.grammar.recovery.NonRecoveryCombinedParser;
import org.batfish.grammar.recovery.RecoveryExtractor;
import org.batfish.grammar.recovery.RecoveryParser.RecoveryContext;
import org.junit.Test;

/* Test of {@link BatfishLexerErrorListener} */
public final class BatfishParserErrorListenerTest {

  @Test
  public void testNonRecoveryParserErrorNode() {
    String recoveryText = CommonUtil.readResource("org/batfish/grammar/non_recovery_parser_error");
    GrammarSettings settings = new MockGrammarSettings(false, 0, 0, 0, false, false, false, false);
    NonRecoveryCombinedParser cp = new NonRecoveryCombinedParser(recoveryText, settings);
    RecoveryContext ctx = cp.parse();
    RecoveryExtractor extractor = new RecoveryExtractor();
    ParseTreeWalker walker = new BatfishParseTreeWalker(cp);
    walker.walk(extractor, ctx);

    // There should be 1 parser error for the token OTHER:'other' on the 2nd line:
    assertThat(extractor.getFirstErrorLine(), equalTo(2));
    assertThat(extractor.getNumErrorNodes(), equalTo(1));
    // The block statement following the error should be processed
    assertThat(extractor.getNumBlockStatements(), equalTo(1));
  }
}
