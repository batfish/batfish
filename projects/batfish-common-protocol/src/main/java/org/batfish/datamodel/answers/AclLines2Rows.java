package org.batfish.datamodel.answers;

import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.acl.CanonicalAcl;
import org.batfish.datamodel.table.Row;

@ParametersAreNonnullByDefault
public class AclLines2Rows implements AclLinesAnswerElementInterface {
  public static final String COL_SOURCES = "aclSources";
  public static final String COL_LINES = "lines";
  public static final String COL_BLOCKED_LINE_NUM = "blockedLineNum";
  public static final String COL_BLOCKING_LINE_NUMS = "blockingLineNums";
  public static final String COL_DIFF_ACTION = "differentAction";
  public static final String COL_MESSAGE = "message";

  private Multiset<Row> _rows = ConcurrentHashMultiset.create();
  private Map<String, Map<String, CanonicalAcl>> _representativeSourcesToAcls = new TreeMap<>();

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
      boolean diffAction,
      boolean undefinedReference,
      boolean cycle) {
    if (cycle) {
      return;
    }
    String aclName = acl.getName();

    // All the host-acl pairs that contain this canonical acl
    Map<String, Set<String>> sources =
        _representativeSourcesToAcls.get(hostname).get(aclName).getSources();
    List<String> flatSources =
        sources
            .entrySet()
            .stream()
            .map(e -> e.getKey() + ": " + String.join(", ", e.getValue()))
            .collect(Collectors.toList());

    _rows.add(
        Row.builder()
            .put(COL_SOURCES, flatSources)
            .put(
                COL_LINES,
                acl.getLines().stream().map(IpAccessListLine::getName).collect(Collectors.toList()))
            .put(COL_BLOCKED_LINE_NUM, lineNumber)
            .put(COL_BLOCKING_LINE_NUMS, blockingLines.keySet())
            .put(COL_DIFF_ACTION, diffAction)
            .put(
                COL_MESSAGE,
                buildMessage(
                    flatSources, lineNumber, line, unmatchable, blockingLines, undefinedReference))
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
      List<String> flatSources,
      int lineNumber,
      String line,
      boolean unmatchable,
      Map<Integer, String> blockingLines,
      boolean undefinedReference) {
    StringBuilder sb =
        new StringBuilder(
            String.format(
                "ACL(s) { %s } contain(s) an unreachable line: '%d: %s'. ",
                String.join("; ", flatSources), lineNumber, line));
    if (undefinedReference) {
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

  @Override
  public void setCanonicalAcls(List<CanonicalAcl> acls) {
    for (CanonicalAcl acl : acls) {
      _representativeSourcesToAcls
          .computeIfAbsent(acl.getRepresentativeHostname(), h -> new TreeMap<>())
          .put(acl.getRepresentativeAclName(), acl);
    }
  }
}
