package org.batfish.datamodel.answers;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;
import java.util.List;
import java.util.SortedSet;
import java.util.stream.Collectors;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.acl.CanonicalAcl;
import org.batfish.datamodel.table.Row;

/**
 * Represents answers to aclReachability2. Implements {@link AclLinesAnswerElementInterface} so that
 * aclReachability and aclReachability2 can use some of the same methods to answer both.
 */
@ParametersAreNonnullByDefault
public class AclLines2Rows implements AclLinesAnswerElementInterface {
  public static final String COL_SOURCES = "aclSources";
  public static final String COL_LINES = "lines";
  public static final String COL_BLOCKED_LINE_NUM = "blockedLineNum";
  public static final String COL_BLOCKING_LINE_NUMS = "blockingLineNums";
  public static final String COL_DIFF_ACTION = "differentAction";
  public static final String COL_MESSAGE = "message";

  private final Multiset<Row> _rows = HashMultiset.create();

  @Override
  public void addUnreachableLine(
      AclSpecs aclSpecs, int lineNumber, boolean unmatchable, SortedSet<Integer> blockingLines) {

    if (aclSpecs.acl.inCycle(lineNumber)) {
      return;
    }

    IpAccessList acl = aclSpecs.acl.getOriginalAcl();
    LineAction blockedLineAction = acl.getLines().get(lineNumber).getAction();
    boolean diffAction = false;
    for (int blockingLineIndex : blockingLines) {
      if (!blockedLineAction.equals(acl.getLines().get(blockingLineIndex).getAction())) {
        diffAction = true;
        break;
      }
    }

    // All the host-acl pairs that contain this canonical acl
    List<String> flatSources =
        aclSpecs
            .sources
            .entrySet()
            .stream()
            .map(e -> e.getKey() + ": " + String.join(", ", e.getValue()))
            .collect(Collectors.toList());

    _rows.add(
        Row.builder()
            .put(COL_SOURCES, flatSources)
            .put(
                COL_LINES,
                acl.getLines()
                    .stream()
                    .map(l -> firstNonNull(l.getName(), l.toString()))
                    .collect(Collectors.toList()))
            .put(COL_BLOCKED_LINE_NUM, lineNumber)
            .put(COL_BLOCKING_LINE_NUMS, blockingLines)
            .put(COL_DIFF_ACTION, diffAction)
            .put(
                COL_MESSAGE,
                buildMessage(aclSpecs.acl, lineNumber, flatSources, blockingLines, unmatchable))
            .build());
  }

  @Override
  public void addCycle(String hostname, List<String> aclsInCycle) {
    _rows.add(
        Row.builder()
            .put(COL_SOURCES, ImmutableList.of(hostname + ": " + String.join(", ", aclsInCycle)))
            .put(COL_LINES, null)
            .put(COL_BLOCKED_LINE_NUM, null)
            .put(COL_BLOCKING_LINE_NUMS, null)
            .put(COL_DIFF_ACTION, null)
            .put(
                COL_MESSAGE,
                String.format(
                    "Cyclic ACL references in node '%s': %s -> %s",
                    hostname, String.join(" -> ", aclsInCycle), aclsInCycle.get(0)))
            .build());
  }

  private static String buildMessage(
      CanonicalAcl canonicalAcl,
      int blockedLineNum,
      List<String> flatSources,
      SortedSet<Integer> blockingLines,
      boolean unmatchable) {
    List<IpAccessListLine> lines = canonicalAcl.getOriginalAcl().getLines();
    String blockedLineName =
        firstNonNull(lines.get(blockedLineNum).getName(), lines.get(blockedLineNum).toString());
    StringBuilder sb =
        new StringBuilder(
            String.format(
                "ACLs { %s } contain an unreachable line:\n  [index %d] %s\n",
                String.join("; ", flatSources), blockedLineNum, blockedLineName));
    if (canonicalAcl.hasUndefinedRef(blockedLineNum)) {
      sb.append("This line references a structure that is not defined.");
    } else if (unmatchable) {
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
                      .stream()
                      .map(
                          i -> {
                            String blockingLineName =
                                firstNonNull(lines.get(i).getName(), lines.get(i).toString());
                            return String.format("  [index %d] %s", i, blockingLineName);
                          })
                      .collect(Collectors.toList()))));
    }
    return sb.toString();
  }

  public Multiset<Row> getRows() {
    return _rows;
  }
}
