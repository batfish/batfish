package org.batfish.question.initialization;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.batfish.question.initialization.IssueAggregation.aggregateDuplicateErrors;
import static org.batfish.question.initialization.IssueAggregation.aggregateDuplicateParseWarnings;
import static org.batfish.question.initialization.IssueAggregation.aggregateDuplicateRedflagWarnings;
import static org.batfish.question.initialization.IssueAggregation.aggregateDuplicateUnimplementedWarnings;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.util.List;
import java.util.Map;
import org.batfish.common.Answerer;
import org.batfish.common.BatfishException.BatfishStackTrace;
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
    aggregateDuplicateRedflagWarnings(convertWarnings)
        .forEach(
            (warning, nodes) ->
                rows.add(getRow(nodes, null, IssueType.ConvertWarningRedFlag, warning.getText())));
    aggregateDuplicateUnimplementedWarnings(convertWarnings)
        .forEach(
            (warning, nodes) ->
                rows.add(
                    getRow(nodes, null, IssueType.ConvertWarningUnimplemented, warning.getText())));

    Map<String, Warnings> fileWarnings = pvcae.getWarnings();
    aggregateDuplicateParseWarnings(fileWarnings)
        .forEach(
            (triplet, filelines) ->
                rows.add(
                    getRow(
                        filelines.keySet().stream().collect(ImmutableSet.toImmutableSet()),
                        filelines.entrySet().stream()
                            .map(e -> new FileLines(e.getKey(), e.getValue()))
                            .collect(ImmutableList.toImmutableList()),
                        IssueType.ParseWarning,
                        String.format(
                            "%s (%s)",
                            firstNonNull(triplet._comment, "(details not provided)"),
                            triplet._text),
                        triplet._parserContext)));

    aggregateDuplicateErrors(ccae.getErrors())
        .forEach(
            (stackTrace, nodeNames) ->
                rows.add(
                    getRow(nodeNames, null, IssueType.ConvertError, trimStackTrace(stackTrace))));
    aggregateDuplicateErrors(pvcae.getErrors())
        .forEach(
            (stackTrace, fileNames) ->
                rows.add(
                    getRow(
                        null,
                        fileNames.stream()
                            .map(n -> new FileLines(n, ImmutableSortedSet.of()))
                            .collect(ImmutableList.toImmutableList()),
                        IssueType.ParseError,
                        trimStackTrace(stackTrace))));

    TableAnswerElement answerElement = new TableAnswerElement(TABLE_METADATA);
    answerElement.postProcessAnswer(_question, rows.getData());
    return answerElement;
  }

  InitIssuesAnswerer(InitIssuesQuestion question, IBatfish batfish) {
    super(question, batfish);
  }

  /** Remove everything from stack trace before line containing "Caused by" */
  @VisibleForTesting
  static String trimStackTrace(BatfishStackTrace stackTrace) {
    List<String> lines = stackTrace.getLineMap();
    StringBuilder sb = new StringBuilder();
    for (String line : lines) {
      if (line.contains("Caused by") || sb.length() > 0) {
        sb.append(line);
        sb.append("\n");
      }
    }
    return sb.length() > 0
        ? sb.deleteCharAt(sb.length() - 1).toString()
        : String.join("\n", stackTrace.getLineMap());
  }

  private static Row getRow(
      Iterable<String> nodes, Iterable<FileLines> filelines, IssueType issueType, String issue) {
    return getRow(nodes, filelines, issueType, issue, null);
  }

  private static Row getRow(
      Iterable<String> nodes,
      Iterable<FileLines> filelines,
      IssueType issueType,
      String issue,
      String parserContext) {
    return Row.builder(TABLE_METADATA.toColumnMap())
        .put(COL_NODES, nodes)
        .put(COL_FILELINES, filelines)
        .put(COL_ISSUE_TYPE, issueType.toString())
        .put(COL_ISSUE, issue)
        .put(COL_PARSER_CONTEXT, parserContext)
        .build();
  }

  public enum IssueType {
    ConvertError("Convert error"),
    ConvertWarningRedFlag("Convert warning (redflag)"),
    ConvertWarningUnimplemented("Convert warning (unimplemented)"),
    ParseError("Parse error"),
    ParseWarning("Parse warning");

    private final String _value;

    IssueType(String value) {
      _value = value;
    }

    @Override
    public String toString() {
      return _value;
    }
  }

  static final String COL_NODES = "Nodes";
  static final String COL_FILELINES = "Source_Lines";
  static final String COL_ISSUE_TYPE = "Issue_Type";
  static final String COL_ISSUE = "Issue";
  static final String COL_PARSER_CONTEXT = "Parser_Context";

  private static final List<ColumnMetadata> METADATA =
      ImmutableList.of(
          new ColumnMetadata(
              COL_NODES, Schema.list(Schema.STRING), "The node that was converted", true, false),
          new ColumnMetadata(
              COL_FILELINES,
              Schema.list(Schema.FILE_LINES),
              "The files and lines that caused the issue",
              true,
              false),
          new ColumnMetadata(
              COL_ISSUE_TYPE, Schema.STRING, "The type of issue identified", true, false),
          new ColumnMetadata(COL_ISSUE, Schema.STRING, "The issue identified", false, true),
          new ColumnMetadata(
              COL_PARSER_CONTEXT,
              Schema.STRING,
              "Batfish parser state when issue was encountered",
              false,
              true));

  private static final String TEXT_DESC = String.format("Placeholder ${%s}", COL_NODES);

  private static final TableMetadata TABLE_METADATA = new TableMetadata(METADATA, TEXT_DESC);
}
