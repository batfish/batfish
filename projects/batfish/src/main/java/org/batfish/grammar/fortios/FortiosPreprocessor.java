package org.batfish.grammar.fortios;

import javax.annotation.Nonnull;
import org.batfish.common.Warnings;

/** Produces final configuration lines actually used to build configuration. */
public final class FortiosPreprocessor extends FortiosParserBaseListener {

  public FortiosPreprocessor(FortiosCombinedParser parser, Warnings warnings) {
    _parser = parser;
    _w = warnings;
  }

  @SuppressWarnings("unused")
  private final @Nonnull FortiosCombinedParser _parser;

  @SuppressWarnings("unused")
  private final @Nonnull Warnings _w;
}
