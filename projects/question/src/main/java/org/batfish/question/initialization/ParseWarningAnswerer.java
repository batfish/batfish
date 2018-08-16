package org.batfish.question.initialization;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.common.Answerer;
import org.batfish.common.Warnings;
import org.batfish.common.Warnings.ParseWarning;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.Rows;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;

/** Implements {@link ParseWarningQuestion}. */
class ParseWarningAnswerer extends Answerer {
  @Override
  public TableAnswerElement answer() {
    ParseVendorConfigurationAnswerElement pvcae =
        _batfish.loadParseVendorConfigurationAnswerElement();

    Map<String, Warnings> fileWarnings = pvcae.getWarnings();

    Rows rows = new Rows();
    fileWarnings.forEach(
        (filename, warnings) -> {
          for (ParseWarning w : warnings.getParseWarnings()) {
            rows.add(getRow(filename, w));
          }
        });

    TableAnswerElement answerElement = new TableAnswerElement(TABLE_METADATA);
    answerElement.postProcessAnswer(_question, rows.getData());
    return answerElement;
  }

  ParseWarningAnswerer(ParseWarningQuestion question, IBatfish batfish) {
    super(question, batfish);
  }

  @Nonnull
  @VisibleForTesting
  static Row getRow(String filename, ParseWarning warning) {
    return Row.builder(TABLE_METADATA.toColumnMap())
        .put(COL_FILENAME, filename)
        .put(COL_LINE, warning.getLine())
        .put(COL_TEXT, warning.getText())
        .put(COL_PARSER_CONTEXT, warning.getParserContext())
        .put(COL_COMMENT, firstNonNull(warning.getComment(), "(not provided)"))
        .build();
  }

  static final String COL_FILENAME = "Filename";
  static final String COL_LINE = "Line";
  static final String COL_TEXT = "Text";
  static final String COL_COMMENT = "Comment";
  static final String COL_PARSER_CONTEXT = "Parser_Context";

  private static final List<ColumnMetadata> METADATA =
      ImmutableList.of(
          new ColumnMetadata(COL_FILENAME, Schema.STRING, "The file that was parsed", true, false),
          new ColumnMetadata(
              COL_TEXT,
              Schema.STRING,
              "The text of the input that caused the warning",
              true,
              false),
          new ColumnMetadata(
              COL_LINE,
              Schema.INTEGER,
              "The line number in the input file that caused the warning",
              false,
              false),
          new ColumnMetadata(
              COL_PARSER_CONTEXT,
              Schema.STRING,
              "The context of the Batfish parser when the warning occurred",
              false,
              false),
          new ColumnMetadata(
              COL_COMMENT,
              Schema.STRING,
              "An optional comment explaining more information about the warning",
              false,
              false));

  private static final String TEXT_DESC =
      String.format(
          "File ${%s}: warning at line ${%s}: ${%s} when the Batfish parser was in state ${%s}."
              + " Optional comment: ${%s}.",
          COL_FILENAME, COL_LINE, COL_TEXT, COL_PARSER_CONTEXT, COL_COMMENT);

  private static final TableMetadata TABLE_METADATA = new TableMetadata(METADATA, TEXT_DESC);
}
