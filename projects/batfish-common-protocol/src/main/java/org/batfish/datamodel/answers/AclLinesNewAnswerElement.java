package org.batfish.datamodel.answers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.Rows;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;

@ParametersAreNonnullByDefault
public class AclLinesNewAnswerElement extends TableAnswerElement implements AclLinesAnswerElement {
  public static final String COL_NODES = "nodes";
  public static final String COL_ACL = "acl";
  public static final String COL_LINES = "lines";
  public static final String COL_BLOCKED_LINE_NUM = "blockedLineNum";
  public static final String COL_BLOCKING_LINE_NUMS = "blockingLineNums";
  public static final String COL_DIFF_ACTION = "differentAction";
  public static final String COL_MESSAGE = "message";

  private SortedMap<String, SortedMap<String, AclSpecs>> _equivalenceClasses = new TreeMap<>();
  private Rows _initialRows = new Rows(null);

  public AclLinesNewAnswerElement(TableMetadata tableMetadata) {
    super(tableMetadata);
  }

  @Override
  public void addEquivalenceClass(
      String aclName, String hostname, SortedSet<String> eqClassNodes, List<String> aclLines) {
    _equivalenceClasses
        .computeIfAbsent(aclName, k -> new TreeMap<>())
        .put(hostname, new AclSpecs(aclLines, eqClassNodes));
  }

  @Override
  public void addUnreachableLine(
      String hostname,
      String aclName,
      int lineNumber,
      String line,
      boolean unmatchable,
      @Nullable Integer blockingLineNum,
      String blockingLine,
      boolean diffAction) {

    AclSpecs specs = _equivalenceClasses.get(aclName).get(hostname);
    ImmutableMap<Integer, String> blockingLines =
        blockingLineNum == null
            ? ImmutableMap.of()
            : ImmutableMap.of(blockingLineNum, blockingLine);

    _initialRows.add(
        new Row()
            .put(COL_NODES, specs.nodes)
            .put(COL_ACL, aclName)
            .put(COL_LINES, specs.lines)
            .put(COL_BLOCKED_LINE_NUM, lineNumber)
            .put(COL_BLOCKING_LINE_NUMS, blockingLines.keySet())
            .put(COL_DIFF_ACTION, diffAction)
            .put(
                COL_MESSAGE,
                buildMessage(aclName, specs.nodes, lineNumber, line, unmatchable, blockingLines)));
  }

  private String buildMessage(
      String aclName,
      SortedSet<String> nodes,
      int lineNumber,
      String line,
      boolean unmatchable,
      Map<Integer, String> blockingLines) {
    StringBuilder sb =
        new StringBuilder(
            String.format(
                "In node(s) '%s', ACL '%s' has an unreachable line '%d: %s'. ",
                String.join("', '", nodes), aclName, lineNumber, line));
    if (unmatchable) {
      sb.append("This line will never match any packet, independent of preceding lines.");
    } else if (blockingLines.isEmpty()) {
      sb.append("Multiple earlier lines partially block this line, making it unreachable.");
    } else {
      sb.append(
          String.format(
              "Blocking line(s):\n%s",
              String.join(
                  "\n",
                  blockingLines
                      .entrySet()
                      .stream()
                      .map(e -> String.format("  [index %d] %s", e.getKey(), e.getValue()))
                      .collect(Collectors.toList()))));
    }
    return sb.toString();
  }

  @JsonIgnore
  public Rows getInitialRows() {
    return _initialRows;
  }
}
