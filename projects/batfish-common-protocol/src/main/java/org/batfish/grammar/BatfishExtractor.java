package org.batfish.grammar;

import org.antlr.v4.runtime.ParserRuleContext;

public interface BatfishExtractor {

  void processParseTree(ParserRuleContext tree);
}
