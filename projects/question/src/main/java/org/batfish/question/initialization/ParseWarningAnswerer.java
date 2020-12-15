package org.batfish.question.initialization;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.batfish.question.initialization.IssueAggregation.aggregateDuplicateParseWarnings;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Multimap;
import java.util.Comparator;
import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.common.Answerer;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.Warnings;
import org.batfish.common.Warnings.ParseWarning;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.collections.FileLines;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;
import org.batfish.question.initialization.IssueAggregation.ParseWarningTriplet;

/** Answers {@link ParseWarningQuestion}. */
class ParseWarningAnswerer extends Answerer {

  @Override
  public TableAnswerElement answer(NetworkSnapshot snapshot) {
    ParseWarningQuestion question = (ParseWarningQuestion) _question;
    TableMetadata metadata = createMetadata(question);
    Map<String, ColumnMetadata> columnMetadataMap = metadata.toColumnMap();

    ParseVendorConfigurationAnswerElement pvcae =
        _batfish.loadParseVendorConfigurationAnswerElement(snapshot);

    Map<String, Warnings> fileWarnings = pvcae.getWarnings();

    ImmutableList.Builder<Row> rows = ImmutableList.builder();

    if (question.getAggregateDuplicates()) {
      aggregateDuplicateParseWarnings(fileWarnings)
          .forEach(
              (triplet, fileLines) ->
                  rows.add(getAggregateRow(triplet, fileLines, columnMetadataMap)));

    } else {
      fileWarnings.forEach(
          (filename, warnings) -> {
            for (ParseWarning w : warnings.getParseWarnings()) {
              rows.add(getRow(filename, w, columnMetadataMap));
            }
          });
    }

    TableAnswerElement answerElement = new TableAnswerElement(createMetadata(question));
    answerElement.postProcessAnswer(_question, rows.build());
    return answerElement;
  }

  ParseWarningAnswerer(ParseWarningQuestion question, IBatfish batfish) {
    super(question, batfish);
  }

  @Nonnull
  @VisibleForTesting
  static Row getAggregateRow(
      ParseWarningTriplet triplet,
      Multimap<String, Integer> fileLines,
      Map<String, ColumnMetadata> columnMetadataMap) {
    return Row.builder(columnMetadataMap)
        .put(
            COL_FILELINES,
            fileLines.asMap().entrySet().stream()
                .map(
                    e ->
                        new FileLines(
                            e.getKey(),
                            e.getValue().stream()
                                .collect(
                                    ImmutableSortedSet.toImmutableSortedSet(
                                        Comparator.naturalOrder()))))
                .collect(ImmutableList.toImmutableList()))
        .put(COL_TEXT, triplet._text)
        .put(COL_PARSER_CONTEXT, triplet._parserContext)
        .put(COL_COMMENT, firstNonNull(triplet._comment, "(not provided)"))
        .build();
  }

  @Nonnull
  @VisibleForTesting
  static Row getRow(
      String filename, ParseWarning warning, Map<String, ColumnMetadata> columnMetadataMap) {
    return Row.builder(columnMetadataMap)
        .put(COL_FILENAME, filename)
        .put(COL_LINE, warning.getLine())
        .put(COL_TEXT, warning.getText())
        .put(COL_PARSER_CONTEXT, warning.getParserContext())
        .put(COL_COMMENT, firstNonNull(warning.getComment(), "(not provided)"))
        .build();
  }

  static final String COL_FILELINES = "Source_Lines"; // with aggregation
  static final String COL_FILENAME = "Filename"; // without aggregation
  static final String COL_LINE = "Line"; // without aggregation
  static final String COL_TEXT = "Text";
  static final String COL_COMMENT = "Comment";
  static final String COL_PARSER_CONTEXT = "Parser_Context";

  @Nonnull
  @VisibleForTesting
  static TableMetadata createMetadata(ParseWarningQuestion question) {

    ImmutableList.Builder<ColumnMetadata> columnMetadata = ImmutableList.builder();

    if (question.getAggregateDuplicates()) {
      columnMetadata.add(
          new ColumnMetadata(
              COL_FILELINES,
              Schema.list(Schema.FILE_LINES),
              "The files and lines that caused the warning",
              true,
              false));
    } else {
      columnMetadata.add(
          new ColumnMetadata(COL_FILENAME, Schema.STRING, "The file that was parsed", true, false));
      columnMetadata.add(
          new ColumnMetadata(
              COL_LINE,
              Schema.INTEGER,
              "The line number in the input file that caused the warning",
              true,
              false));
    }

    columnMetadata.add(
        new ColumnMetadata(
            COL_TEXT, Schema.STRING, "The text of the input that caused the warning", true, false));

    columnMetadata
        .add(
            new ColumnMetadata(
                COL_PARSER_CONTEXT,
                Schema.STRING,
                "The context of the Batfish parser when the warning occurred",
                false,
                false))
        .add(
            new ColumnMetadata(
                COL_COMMENT,
                Schema.STRING,
                "An optional comment explaining more information about the warning",
                false,
                false));

    String textDesc =
        question.getAggregateDuplicates()
            ? String.format(
                "Warning for ${%s} when the Batfish parser was in state ${%s}. Optional comment:"
                    + " ${%s}.",
                COL_TEXT, COL_PARSER_CONTEXT, COL_COMMENT)
            : String.format(
                "File ${%s}: warning at line ${%s}: ${%s} when the Batfish parser was in state"
                    + " ${%s}. Optional comment: ${%s}.",
                COL_FILENAME, COL_LINE, COL_TEXT, COL_PARSER_CONTEXT, COL_COMMENT);

    return new TableMetadata(columnMetadata.build(), textDesc);
  }
}
