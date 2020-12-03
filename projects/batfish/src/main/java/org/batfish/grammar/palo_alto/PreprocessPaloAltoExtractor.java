package org.batfish.grammar.palo_alto;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.batfish.common.Warnings;
import org.batfish.grammar.BatfishParseTreeWalker;

/**
 * Parse tree extractor used for generating pre-processed PaloAlto configuration text from an
 * initial unprocessed flat PaloAlto parse tree.
 */
public class PreprocessPaloAltoExtractor {

  public static void preprocess(
      ParserRuleContext tree, String text, PaloAltoCombinedParser parser, Warnings w) {
    ParseTreeWalker walker = new BatfishParseTreeWalker(parser);
    Deleter d = new Deleter();
    walker.walk(d, tree);
  }
}
