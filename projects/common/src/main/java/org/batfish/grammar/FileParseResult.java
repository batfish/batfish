package org.batfish.grammar;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.ParseTreeSentences;
import org.batfish.common.Warnings;
import org.batfish.datamodel.answers.ParseStatus;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;

/**
 * Represents results of parsing a file (which may be one of many in parsing job for a device). The
 * warnings object will have file-specific warnings, not job-level warnings that are not file
 * specific.
 */
@ParametersAreNonnullByDefault
public class FileParseResult implements Serializable {
  private @Nonnull ParseTreeSentences _parseTreeSentences;
  private final @Nonnull SilentSyntaxCollection _silentSyntax;
  private final @Nonnull Warnings _warnings;
  private @Nullable ParseStatus _parseStatus;

  public FileParseResult(
      ParseTreeSentences parseTreeSentences,
      SilentSyntaxCollection silentSyntax,
      Warnings warnings) {
    _parseTreeSentences = parseTreeSentences;
    _silentSyntax = silentSyntax;
    _warnings = warnings;
  }

  public @Nonnull FileParseResult setParseStatus(ParseStatus parseStatus) {
    _parseStatus = parseStatus;
    return this;
  }

  public @Nonnull FileParseResult setParseTreeSentences(ParseTreeSentences parseTreeSentences) {
    _parseTreeSentences = parseTreeSentences;
    return this;
  }

  public @Nonnull ParseTreeSentences getParseTreeSentences() {
    return _parseTreeSentences;
  }

  public @Nonnull SilentSyntaxCollection getSilentSyntax() {
    return _silentSyntax;
  }

  public @Nonnull Warnings getWarnings() {
    return _warnings;
  }

  public @Nullable ParseStatus getParseStatus() {
    return _parseStatus;
  }
}
