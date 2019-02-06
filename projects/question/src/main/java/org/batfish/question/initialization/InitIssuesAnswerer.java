package org.batfish.question.initialization;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.batfish.question.initialization.IssueAggregation.aggregateDuplicateParseWarnings;
import static org.batfish.question.initialization.IssueAggregation.aggregateDuplicateStrings;
import static org.batfish.question.initialization.IssueAggregation.aggregateDuplicateWarnings;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.batfish.common.Answerer;
import org.batfish.common.Warnings;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.collections.FileLines;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.Rows;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;

/** Implements {@link InitIssuesQuestion}. */
public class InitIssuesAnswerer extends Answerer {
  @Override
  public TableAnswerElement answer() {
    ConvertConfigurationAnswerElement ccae =
        _batfish.loadConvertConfigurationAnswerElementOrReparse();
    ParseVendorConfigurationAnswerElement pvcae =
        _batfish.loadParseVendorConfigurationAnswerElement();

    Rows rows = new Rows();

    Map<String, Warnings> convertWarnings = ccae.getWarnings();
    aggregateDuplicateWarnings(convertWarnings, Warnings::getRedFlagWarnings)
        .forEach(
            (warning, nodes) ->
                rows.add(getRow(nodes, null, IssueType.ConvertWarningRedFlag, warning.getText())));
    aggregateDuplicateWarnings(convertWarnings, Warnings::getUnimplementedWarnings)
        .forEach(
            (warning, nodes) ->
                rows.add(
                    getRow(nodes, null, IssueType.ConvertWarningUnimplemented, warning.getText())));

    Map<String, Warnings> fileWarnings = pvcae.getWarnings();
    aggregateDuplicateParseWarnings(fileWarnings)
        .forEach(
            (triplet, fileLines) ->
                rows.add(
                    getRow(
                        fileLines.keySet().stream()
                            .collect(
                                ImmutableSortedSet.toImmutableSortedSet(Comparator.naturalOrder())),
                        fileLines.asMap().entrySet().stream()
                            .map(
                                e ->
                                    new FileLines(
                                        e.getKey(),
                                        e.getValue().stream()
                                            .collect(
                                                ImmutableSortedSet.toImmutableSortedSet(
                                                    Comparator.naturalOrder()))))
                            .collect(ImmutableList.toImmutableList()),
                        IssueType.ParseWarning,
                        String.format(
                            "%s (%s)",
                            firstNonNull(triplet._comment, "(details not provided)"),
                            triplet._text),
                        triplet._parserContext)));

    // Aggregate stack traces after trimming to:
    // 1) remove the redundant info like "Conversion error for node 'xyz'"
    // 2) allow useful aggregation across similar issues (e.g. on node 'xyz' and 'abc')
    aggregateDuplicateStrings(ccae.getErrorMessages())
        .forEach(
            (stackTrace, nodeNames) ->
                rows.add(getRow(nodeNames, null, IssueType.ConvertError, stackTrace)));
    aggregateDuplicateStrings(pvcae.getErrorMessages())
        .forEach(
            (stackTrace, fileNames) ->
                rows.add(
                    getRow(
                        null,
                        fileNames.stream()
                            .map(n -> new FileLines(n, ImmutableSortedSet.of()))
                            .collect(ImmutableList.toImmutableList()),
                        IssueType.ParseError,
                        stackTrace)));

    TableAnswerElement answerElement = new TableAnswerElement(TABLE_METADATA);
    answerElement.postProcessAnswer(_question, rows.getData());
    return answerElement;
  }

  InitIssuesAnswerer(InitIssuesQuestion question, IBatfish batfish) {
    super(question, batfish);
  }

  private static Row getRow(
      @Nullable Iterable<String> nodes,
      @Nullable Iterable<FileLines> fileLines,
      IssueType issueType,
      String issue) {
    return getRow(nodes, fileLines, issueType, issue, null);
  }

  private static Row getRow(
      Iterable<String> nodes,
      Iterable<FileLines> fileLines,
      IssueType issueType,
      String issue,
      @Nullable String parserContext) {
    return Row.builder(TABLE_METADATA.toColumnMap())
        .put(COL_NODES, nodes)
        .put(COL_FILELINES, fileLines)
        .put(COL_ISSUE_TYPE, issueType.toString())
        .put(COL_ISSUE, issue)
        .put(COL_PARSER_CONTEXT, parserContext)
        .build();
  }

  static final String COL_NODES = "Nodes";
  static final String COL_FILELINES = "Source_Lines";
  static final String COL_ISSUE_TYPE = "Issue_Type";
  static final String COL_ISSUE = "Issue";
  static final String COL_PARSER_CONTEXT = "Parser_Context";

  private static final List<ColumnMetadata> METADATA =
      ImmutableList.of(
          new ColumnMetadata(
              COL_NODES,
              Schema.list(Schema.STRING),
              "The nodes that were converted (if applicable)",
              true,
              false),
          new ColumnMetadata(
              COL_FILELINES,
              Schema.list(Schema.FILE_LINES),
              "The files and lines that caused the issues (if applicable)",
              true,
              false),
          new ColumnMetadata(
              COL_ISSUE_TYPE, Schema.STRING, "The type of issues identified", true, false),
          new ColumnMetadata(COL_ISSUE, Schema.STRING, "The issues identified", false, true),
          new ColumnMetadata(
              COL_PARSER_CONTEXT,
              Schema.STRING,
              "Batfish parser state when issues were encountered (if applicable)",
              false,
              true));

  private static final TableMetadata TABLE_METADATA = new TableMetadata(METADATA);
}
