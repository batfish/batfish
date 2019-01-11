package org.batfish.question.initialization;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import org.batfish.common.Answerer;
import org.batfish.common.Warnings;
import org.batfish.common.Warnings.ParseWarning;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.collections.FileLines;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.Rows;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;

/** Answers {@link ParseWarningQuestion}. */
class ParseWarningAnswerer extends Answerer {

  static class WarningTriplet {
    String _text;
    String _parserContext;
    String _comment;

    public WarningTriplet(ParseWarning w) {
      this(w.getText(), w.getParserContext(), w.getComment());
    }

    public WarningTriplet(String text, String parserContext, String comment) {
      _text = text;
      _parserContext = parserContext;
      _comment = comment;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof WarningTriplet)) {
        return false;
      }
      return Objects.equals(_text, ((WarningTriplet) o)._text)
          && Objects.equals(_parserContext, ((WarningTriplet) o)._parserContext)
          && Objects.equals(_comment, ((WarningTriplet) o)._comment);
    }

    @Override
    public int hashCode() {
      return Objects.hash(_text, _parserContext, _comment);
    }
  }

  @Override
  public TableAnswerElement answer() {
    ParseWarningQuestion question = (ParseWarningQuestion) _question;
    TableMetadata metadata = createMetadata(question);
    Map<String, ColumnMetadata> columnMetadataMap = metadata.toColumnMap();

    ParseVendorConfigurationAnswerElement pvcae =
        _batfish.loadParseVendorConfigurationAnswerElement();

    Map<String, Warnings> fileWarnings = pvcae.getWarnings();

    Rows rows = new Rows();

    if (question.getAggregateDuplicates()) {
      aggregateDuplicateWarnings(fileWarnings)
          .forEach(
              (triplet, filelines) ->
                  rows.add(getAggregateRow(triplet, filelines, columnMetadataMap)));

    } else {
      fileWarnings.forEach(
          (filename, warnings) -> {
            for (ParseWarning w : warnings.getParseWarnings()) {
              rows.add(getRow(filename, w, columnMetadataMap));
            }
          });
    }

    TableAnswerElement answerElement = new TableAnswerElement(createMetadata(question));
    answerElement.postProcessAnswer(_question, rows.getData());
    return answerElement;
  }

  ParseWarningAnswerer(ParseWarningQuestion question, IBatfish batfish) {
    super(question, batfish);
  }

  @Nonnull
  @VisibleForTesting
  // triplet -> filename -> lines
  static Map<WarningTriplet, Map<String, SortedSet<Integer>>> aggregateDuplicateWarnings(
      Map<String, Warnings> fileWarnings) {
    Map<WarningTriplet, Map<String, SortedSet<Integer>>> map = new HashMap<>();
    fileWarnings.forEach(
        (filename, warnings) -> {
          for (ParseWarning w : warnings.getParseWarnings()) {
            WarningTriplet triplet = new WarningTriplet(w);
            map.computeIfAbsent(triplet, k -> new HashMap<>())
                .computeIfAbsent(filename, k -> new TreeSet<>())
                .add(w.getLine());
          }
        });
    return map;
  }

  @Nonnull
  @VisibleForTesting
  static Row getAggregateRow(
      WarningTriplet triplet,
      Map<String, SortedSet<Integer>> filelines,
      Map<String, ColumnMetadata> columnMetadataMap) {
    return Row.builder(columnMetadataMap)
        .put(
            COL_FILELINES,
            filelines
                .entrySet()
                .stream()
                .map(e -> new FileLines(e.getKey(), e.getValue()))
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

  static final String COL_FILELINES = "Filelines"; // with aggregation
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
              "The files and files that were parsed",
              true,
              false));
    } else {
      columnMetadata.add(
          new ColumnMetadata(COL_FILENAME, Schema.STRING, "The file that was parsed", true, false));
    }

    columnMetadata.add(
        new ColumnMetadata(
            COL_TEXT, Schema.STRING, "The text of the input that caused the warning", true, false));

    if (!question.getAggregateDuplicates()) {
      columnMetadata.add(
          new ColumnMetadata(
              COL_LINE,
              Schema.INTEGER,
              "The line number in the input file that caused the warning",
              false,
              false));
    }
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
                "Warning at line ${%s} when the Batfish parser was in state ${%s}. Optional comment: ${%s}.",
                COL_FILENAME, COL_LINE, COL_TEXT, COL_PARSER_CONTEXT, COL_COMMENT)
            : String.format(
                "File ${%s}: warning at line ${%s}: ${%s} when the Batfish parser was in state ${%s}."
                    + " Optional comment: ${%s}.",
                COL_FILENAME, COL_LINE, COL_TEXT, COL_PARSER_CONTEXT, COL_COMMENT);

    return new TableMetadata(columnMetadata.build(), textDesc);
  }
}
