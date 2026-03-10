package org.batfish.grammar;

import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.common.NetworkSnapshot;

public interface BatfishExtractor {

  void processParseTree(NetworkSnapshot snapshot, ParserRuleContext tree);
}
