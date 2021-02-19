package org.batfish.grammar.palo_alto;

import static com.google.common.base.Preconditions.checkState;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.batfish.common.Warnings;
import org.batfish.grammar.BatfishParseTreeWalker;
import org.batfish.grammar.PreprocessExtractor;
import org.batfish.grammar.palo_alto.PaloAltoParser.Set_lineContext;

/**
 * Parse tree extractor used for generating pre-processed PaloAlto configuration text from an
 * initial unprocessed flat PaloAlto parse tree.
 */
public class PreprocessPaloAltoExtractor implements PreprocessExtractor {

  public PreprocessPaloAltoExtractor(PaloAltoCombinedParser parser, Warnings w) {
    _parser = parser;
    _w = w;
  }

  public static void preprocess(ParserRuleContext tree, PaloAltoCombinedParser parser, Warnings w) {
    new PreprocessPaloAltoExtractor(parser, w).processParseTree(tree);
  }

  @Override
  public void processParseTree(ParserRuleContext tree) {
    ParseTreeWalker walker = new BatfishParseTreeWalker(_parser);
    InsertDeleteApplicator d = new InsertDeleteApplicator(_parser, _w);
    walker.walk(d, tree);
    _preprocessedConfigurationText =
        Stream.concat(
                Stream.of(HEADER),
                tree.children.stream()
                    .filter(c -> c instanceof Set_lineContext)
                    .map(ParserRuleContext.class::cast)
                    .map(s -> d.getFullText(s).trim()))
            .collect(Collectors.joining("\n"));
  }

  @Nonnull
  @Override
  public String getPreprocessedConfigurationText() {
    checkState(_preprocessedConfigurationText != null, "Must first run processParseTree");
    return _preprocessedConfigurationText;
  }

  private static final String HEADER = "####BATFISH PRE-PROCESSED PALO ALTO CONFIG####";

  private String _preprocessedConfigurationText;
  private final @Nonnull PaloAltoCombinedParser _parser;
  private final @Nonnull Warnings _w;
}
