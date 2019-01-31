package org.batfish.question.initialization;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import org.batfish.common.Answerer;
import org.batfish.common.BatfishException.BatfishStackTrace;
import org.batfish.common.Warning;
import org.batfish.common.Warnings;
import org.batfish.common.Warnings.ParseWarning;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.Rows;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;
import org.batfish.question.initialization.ParseWarningAnswerer.WarningTriplet;

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
    convertWarnings.forEach(
        (nodeName, nodeWarnings) -> {
          for (Warning w : nodeWarnings.getRedFlagWarnings()) {
            rows.add(getRow(IssueType.ConvertWarningRedFlag, nodeName, w));
          }
          for (Warning w : nodeWarnings.getUnimplementedWarnings()) {
            rows.add(getRow(IssueType.ConvertWarningUnimplemented, nodeName, w));
          }
        });
    ccae.getErrors()
        .forEach(
            (nodeName, stackTrace) ->
                rows.add(getRow(IssueType.ConvertError, nodeName, null, "", stackTrace)));

    Map<String, Warnings> fileWarnings = pvcae.getWarnings();

    fileWarnings.forEach(
        (filename, warnings) -> {
          for (ParseWarning w : warnings.getParseWarnings()) {
            rows.add(getRow(IssueType.ParseWarning, filename, w));
          }
        });

    pvcae
        .getErrors()
        .forEach(
            (fileName, stackTrace) ->
                rows.add(getRow(IssueType.ParseError, null, fileName, "", stackTrace)));

    TableAnswerElement answerElement = new TableAnswerElement(TABLE_METADATA);
    answerElement.postProcessAnswer(_question, rows.getData());
    return answerElement;
  }

  InitIssuesAnswerer(InitIssuesQuestion question, IBatfish batfish) {
    super(question, batfish);
  }

  /** Remove everything before caused by */
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
      IssueType issueType, String nodeName, WarningTriplet triplet, SortedSet<Integer> lines) {
    return Row.builder(TABLE_METADATA.toColumnMap())
        .put(COL_NODE, nodeName)
        .put(COL_ISSUE_TYPE, issueType.toString())
        .put(COL_PARSER_CONTEXT, triplet._parserContext)
        .put(
            COL_COMMENT,
            String.format(
                "%s (lines %s: %s)",
                firstNonNull(triplet._comment, "(details not provided)"),
                lines.toString(),
                triplet._text))
        .build();
  }

  private static Row getRow(
      IssueType issueType,
      String nodeName,
      String fileName,
      String comment,
      BatfishStackTrace stackTrace) {
    return Row.builder(TABLE_METADATA.toColumnMap())
        .put(COL_NODE, nodeName)
        .put(COL_FILE, fileName)
        .put(COL_ISSUE_TYPE, issueType.toString())
        // .put(COL_COMMENT, comment)
        .put(COL_STACK_TRACE, trimStackTrace(stackTrace))
        .build();
  }

  private static Row getRow(IssueType issueType, String nodeName, Warning warning) {
    return Row.builder(TABLE_METADATA.toColumnMap())
        .put(COL_NODE, nodeName)
        .put(COL_ISSUE_TYPE, issueType.toString())
        .put(COL_COMMENT, /*"Conversion warning: " +*/ warning.getText())
        .build();
  }

  private static Row getRow(IssueType issueType, String nodeName, ParseWarning warning) {
    return Row.builder(TABLE_METADATA.toColumnMap())
        .put(COL_NODE, nodeName)
        .put(COL_ISSUE_TYPE, issueType.toString())
        .put(COL_PARSER_CONTEXT, warning.getParserContext())
        .put(
            COL_COMMENT,
            String.format(
                "%s (line %d: %s)",
                firstNonNull(warning.getComment(), "(details not provided)"),
                warning.getLine(),
                warning.getText()))
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

  static final String COL_NODE = "Node";
  static final String COL_FILE = "File";
  static final String COL_ISSUE_TYPE = "Issue_Type";
  static final String COL_COMMENT = "Comment";
  static final String COL_PARSER_CONTEXT = "Parser_Context";
  static final String COL_STACK_TRACE = "Stack_Trace";

  private static final List<ColumnMetadata> METADATA =
      ImmutableList.of(
          new ColumnMetadata(COL_NODE, Schema.STRING, "The node that was converted", true, false),
          new ColumnMetadata(COL_FILE, Schema.STRING, "The file that was parsed", true, false),
          new ColumnMetadata(
              COL_ISSUE_TYPE, Schema.STRING, "The type of issue identified", true, false),
          new ColumnMetadata(
              COL_COMMENT, Schema.STRING, "Comment regarding the issue", false, true),
          new ColumnMetadata(
              COL_PARSER_CONTEXT,
              Schema.STRING,
              "Parser context where the issue occurred",
              false,
              true),
          new ColumnMetadata(
              COL_STACK_TRACE, Schema.STRING, "Stack trace from the error", false, true));

  private static final String TEXT_DESC = String.format("Placeholder ${%s}", COL_NODE);

  private static final TableMetadata TABLE_METADATA = new TableMetadata(METADATA, TEXT_DESC);
}
