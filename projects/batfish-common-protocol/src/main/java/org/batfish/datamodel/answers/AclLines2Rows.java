package org.batfish.datamodel.answers;

import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.Multiset;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.table.Row;

@ParametersAreNonnullByDefault
public class AclLines2Rows implements AclLinesAnswerElementInterface {
  public static final String COL_NODES = "nodes";
  public static final String COL_ACL = "acl";
  public static final String COL_LINES = "lines";
  public static final String COL_BLOCKED_LINE_NUM = "blockedLineNum";
  public static final String COL_BLOCKING_LINE_NUMS = "blockingLineNums";
  public static final String COL_DIFF_ACTION = "differentAction";
  public static final String COL_MESSAGE = "message";

  private SortedMap<String, SortedMap<String, AclSpecs>> _equivalenceClasses = new TreeMap<>();
  private Multiset<Row> _rows = ConcurrentHashMultiset.create();

  @Override
  public void addEquivalenceClass(
      String aclName, String hostname, SortedSet<String> eqClassNodes, List<String> aclLines) {
    _equivalenceClasses
        .computeIfAbsent(aclName, k -> new TreeMap<>())
        .put(hostname, new AclSpecs(aclLines, eqClassNodes));
  }

  @Override
  public void addReachableLine(
      String hostname, IpAccessList ipAccessList, int lineNumber, String line) {}

  @Override
  public void addUnreachableLine(
      String hostname,
      IpAccessList acl,
      int lineNumber,
      String line,
      boolean unmatchable,
      SortedMap<Integer, String> blockingLines,
      boolean diffAction) {

    String aclName = acl.getName();
    AclSpecs specs = _equivalenceClasses.get(aclName).get(hostname);

    _rows.add(
        Row.builder()
            .put(COL_NODES, specs.nodes)
            .put(COL_ACL, aclName)
            .put(COL_LINES, specs.lines)
            .put(COL_BLOCKED_LINE_NUM, lineNumber)
            .put(COL_BLOCKING_LINE_NUMS, blockingLines.keySet())
            .put(COL_DIFF_ACTION, diffAction)
            .put(
                COL_MESSAGE,
                buildMessage(aclName, specs.nodes, lineNumber, line, unmatchable, blockingLines))
            .build());
  }

  private static String buildMessage(
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

  public Multiset<Row> getRows() {
    return _rows;
  }
}
