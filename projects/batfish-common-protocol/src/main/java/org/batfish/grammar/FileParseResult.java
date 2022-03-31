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
  @Nonnull private ParseTreeSentences _parseTreeSentences;
  @Nonnull private final SilentSyntaxCollection _silentSyntax;
  @Nonnull private final Warnings _warnings;
  @Nullable private ParseStatus _parseStatus;

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

  @Nonnull
  public FileParseResult setParseTreeSentences(ParseTreeSentences parseTreeSentences) {
    _parseTreeSentences = parseTreeSentences;
    return this;
  }

  @Nonnull
  public ParseTreeSentences getParseTreeSentences() {
    return _parseTreeSentences;
  }

  @Nonnull
  public SilentSyntaxCollection getSilentSyntax() {
    return _silentSyntax;
  }

  @Nonnull
  public Warnings getWarnings() {
    return _warnings;
  }

  @Nullable
  public ParseStatus getParseStatus() {
    return _parseStatus;
  }
}
