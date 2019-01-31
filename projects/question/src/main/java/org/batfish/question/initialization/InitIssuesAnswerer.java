package org.batfish.question.initialization;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.batfish.question.initialization.IssueAggregation.aggregateDuplicateErrors;
import static org.batfish.question.initialization.IssueAggregation.aggregateDuplicateParseWarnings;
import static org.batfish.question.initialization.IssueAggregation.aggregateDuplicateWarnings;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Multiset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
import org.batfish.datamodel.collections.FileLines;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.Rows;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;
import org.batfish.question.initialization.IssueAggregation.WarningTriplet;

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
    /*
    convertWarnings.forEach(
        (nodeName, nodeWarnings) -> {
          for (Warning w : nodeWarnings.getRedFlagWarnings()) {
            rows.add(getRow(IssueType.ConvertWarningRedFlag, nodeName, w));
          }
          for (Warning w : nodeWarnings.getUnimplementedWarnings()) {
            rows.add(getRow(IssueType.ConvertWarningUnimplemented, nodeName, w));
          }
        });
    */
    aggregateDuplicateWarnings(convertWarnings)
        .forEach(
            (type, warningMap) -> {
              warningMap.forEach(
                  (warning, nodes) -> {
                    rows.add(getRow(type, nodes, warning));
                  });
            });

    aggregateDuplicateErrors(ccae.getErrors())
        .forEach(
            (stackTrace, nodeNames) -> {
              rows.add(getRow(IssueType.ConvertError, nodeNames, null, stackTrace));
            });

    Map<String, Warnings> fileWarnings = pvcae.getWarnings();
    aggregateDuplicateParseWarnings(fileWarnings)
        .forEach(
            (triplet, filelines) -> {
              rows.add(getRow(IssueType.ParseWarning, triplet, filelines));
            });

    aggregateDuplicateErrors(pvcae.getErrors())
        .forEach(
            (stackTrace, fileNames) -> {
              rows.add(getRow(IssueType.ParseError, null, fileNames, stackTrace));
            });

    TableAnswerElement answerElement = new TableAnswerElement(TABLE_METADATA);
    answerElement.postProcessAnswer(_question, aggregateNodes(rows.getData()));
    return answerElement;
  }

  InitIssuesAnswerer(InitIssuesQuestion question, IBatfish batfish) {
    super(question, batfish);
  }

  /** Combine rows with the same issue type, fi */
  static Multiset<Row> aggregateNodes(Multiset<Row> rows) {
    Multiset<Row> aggrRows = HashMultiset.create();
    Map<List<Object>, Set<Row>> groupedRows = new HashMap<>();

    for (Row row : rows) {
      @SuppressWarnings("unchecked")
      Set<String> nodes = (Set<String>) row.get(COL_NODES, Schema.set(Schema.STRING));
      if (nodes == null) {
        // Add the row as is, since we won't need to aggregate rows with null nodes
        aggrRows.add(row);
      } else {
        ImmutableList.Builder<Object> listBuilder = ImmutableList.builder();
        listBuilder.add(row.getObject(COL_ISSUE_TYPE), row.getObject(COL_ISSUE));
        Object filelines = row.getObject(COL_FILELINES);
        if (filelines != null) {
          listBuilder.add(filelines);
        }
        Object details = row.getObject(COL_DETAILS);
        if (details != null) {
          listBuilder.add(details);
        }
        groupedRows.computeIfAbsent(listBuilder.build(), l -> new HashSet<>()).add(row);
      }
    }

    for (Entry<List<Object>, Set<Row>> entry : groupedRows.entrySet()) {
      ImmutableSortedSet.Builder<String> nodesBuilder = ImmutableSortedSet.naturalOrder();
      entry
          .getValue()
          .forEach(
              r -> {
                @SuppressWarnings("unchecked")
                Set<String> vni = (Set<String>) r.get(COL_NODES, Schema.set(Schema.STRING));
                nodesBuilder.addAll(vni);
              });
      Row r = entry.getValue().iterator().next();
      aggrRows.add(
          Row.of(
              COL_NODES,
              nodesBuilder.build(),
              COL_FILELINES,
              r.getObject(COL_FILELINES),
              COL_ISSUE_TYPE,
              r.getObject(COL_ISSUE_TYPE),
              COL_ISSUE,
              r.getObject(COL_ISSUE),
              COL_DETAILS,
              r.getObject(COL_DETAILS)));
    }
    return aggrRows;
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
      IssueType issueType, WarningTriplet triplet, Map<String, SortedSet<Integer>> filelines) {
    return Row.builder(TABLE_METADATA.toColumnMap())
        .put(COL_NODES, filelines.keySet().stream().collect(ImmutableSet.toImmutableSet()))
        .put(
            COL_FILELINES,
            filelines.entrySet().stream()
                .map(e -> new FileLines(e.getKey(), e.getValue()))
                .collect(ImmutableList.toImmutableList()))
        .put(COL_ISSUE_TYPE, issueType.toString())
        .put(
            COL_ISSUE,
            String.format(
                "%s (%s)", firstNonNull(triplet._comment, "(details not provided)"), triplet._text))
        .build();
  }

  private static Row getRow(
      IssueType issueType,
      Set<String> nodeName,
      Set<String> fileNames,
      BatfishStackTrace stackTrace) {
    String trimmedStackTrace = trimStackTrace(stackTrace);
    Row.RowBuilder rb =
        Row.builder(TABLE_METADATA.toColumnMap())
            .put(COL_ISSUE_TYPE, issueType.toString())
            .put(COL_NODES, nodeName)
            .put(COL_ISSUE, "Exception")
            .put(COL_DETAILS, trimmedStackTrace);
    if (fileNames != null) {
      rb.put(
          COL_FILELINES,
          fileNames.stream()
              .map(n -> new FileLines(n, ImmutableSortedSet.of(-1)))
              .collect(ImmutableList.toImmutableList()));
    }
    return rb.build();
  }

  private static Row getRow(IssueType issueType, Set<String> nodes, Warning warning) {
    return Row.builder(TABLE_METADATA.toColumnMap())
        .put(COL_NODES, nodes)
        .put(COL_ISSUE_TYPE, issueType.toString())
        .put(COL_ISSUE, warning.getText())
        .build();
  }

  private static Row getRow(IssueType issueType, String nodeName, ParseWarning warning) {
    return Row.builder(TABLE_METADATA.toColumnMap())
        .put(COL_NODES, ImmutableSet.of(nodeName))
        .put(COL_ISSUE_TYPE, issueType.toString())
        .put(
            COL_ISSUE,
            String.format(
                "%s (line %d: %s)",
                firstNonNull(warning.getComment(), "(details not provided)"),
                warning.getLine(),
                warning.getText()))
        .put(COL_DETAILS, "Parser context: " + warning.getParserContext())
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
  static final String COL_DETAILS = "Issue_Details";

  private static final List<ColumnMetadata> METADATA =
      ImmutableList.of(
          new ColumnMetadata(
              COL_NODES, Schema.set(Schema.STRING), "The node that was converted", true, false),
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
              COL_DETAILS, Schema.STRING, "Additional details about the issue", false, true));

  private static final String TEXT_DESC = String.format("Placeholder ${%s}", COL_NODES);

  private static final TableMetadata TABLE_METADATA = new TableMetadata(METADATA, TEXT_DESC);
}
