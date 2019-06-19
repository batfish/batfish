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
public final class BatfishLexerErrorListenerTest {

  @Test
  public void testNonRecoveryLexerErrorNode() {
    String recoveryText = CommonUtil.readResource("org/batfish/grammar/non_recovery_lexer_error");
    GrammarSettings settings = new MockGrammarSettings(false, 0, 0, 0, false, false, false, false);
    NonRecoveryCombinedParser cp = new NonRecoveryCombinedParser(recoveryText, settings);
    RecoveryContext ctx = cp.parse();
    RecoveryExtractor extractor = new RecoveryExtractor();
    ParseTreeWalker walker = new BatfishParseTreeWalker(cp);
    walker.walk(extractor, ctx);

    assertThat(extractor.getFirstErrorLine(), equalTo(2));
    assertThat(extractor.getNumSimpleStatements(), equalTo(1));
    /*
     * There should be 4 lexer errors for the word 'error':
     * 'er' (requires two chars since 'en...' is valid
     * 'r' (nothing starts with 'r')
     * 'o' (nothing starts with 'o')
     * 'r' (nothing starts with 'r')
     */
    assertThat(extractor.getNumErrorNodes(), equalTo(4));
  }
}
