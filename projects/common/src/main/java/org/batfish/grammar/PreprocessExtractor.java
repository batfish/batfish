package org.batfish.grammar;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.ParserRuleContext;

@ParametersAreNonnullByDefault
public interface PreprocessExtractor {

  /**
   * Pre-process a flat parse {@code tree}, after which pre-processed configuration text will be
   * available via {@link #getPreprocessedConfigurationText}.
   */
  void processParseTree(ParserRuleContext tree);

  @Nonnull
  String getPreprocessedConfigurationText();
}
