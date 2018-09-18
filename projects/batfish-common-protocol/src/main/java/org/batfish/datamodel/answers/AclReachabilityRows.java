package org.batfish.datamodel.answers;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multiset;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.stream.Collectors;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.acl.CanonicalAcl;
import org.batfish.datamodel.questions.DisplayHints;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableMetadata;

/** Represents answers to aclReachability. */
@ParametersAreNonnullByDefault
public class AclReachabilityRows {
  public static final String COL_SOURCES = "ACL_Sources";
  public static final String COL_LINES = "Lines";
  public static final String COL_BLOCKED_LINE_ACTION = "Blocked_Line_Action";
  public static final String COL_BLOCKED_LINE_NUM = "Blocked_Line_Num";
  public static final String COL_BLOCKING_LINE_NUMS = "Blocking_Line_Nums";
  public static final String COL_REASON = "Reason";
  public static final String COL_DIFF_ACTION = "Different_Action";
  public static final String COL_MESSAGE = "Message";

  public static enum Reason {
    CYCLICAL_REFERENCE,
    BLOCKING_LINES,
    UNDEFINED_REFERENCE,
    UNMATCHABLE
  }

  public static final Map<String, ColumnMetadata> COLUMN_METADATA =
      ImmutableMap.<String, ColumnMetadata>builder()
          .put(
              COL_SOURCES,
              new ColumnMetadata(
                  COL_SOURCES, Schema.list(Schema.STRING), "ACL sources", true, false))
          .put(
              COL_LINES,
              new ColumnMetadata(COL_LINES, Schema.list(Schema.STRING), "ACL lines", false, false))
          .put(
              COL_BLOCKED_LINE_NUM,
              new ColumnMetadata(
                  COL_BLOCKED_LINE_NUM, Schema.INTEGER, "Blocked line number", true, false))
          .put(
              COL_BLOCKED_LINE_ACTION,
              new ColumnMetadata(
                  COL_BLOCKED_LINE_ACTION, Schema.STRING, "Blocked line action", true, false))
          .put(
              COL_BLOCKING_LINE_NUMS,
              new ColumnMetadata(
                  COL_BLOCKING_LINE_NUMS,
                  Schema.list(Schema.INTEGER),
                  "Blocking line numbers",
                  false,
                  true))
          .put(
              COL_DIFF_ACTION,
              new ColumnMetadata(COL_DIFF_ACTION, Schema.BOOLEAN, "Different action", false, true))
          .put(
              COL_REASON,
              new ColumnMetadata(COL_REASON, Schema.STRING, "Reason unreachable", false, true))
          .put(COL_MESSAGE, new ColumnMetadata(COL_MESSAGE, Schema.STRING, "Message", false, false))
          .build();

  private final Multiset<Row> _rows = HashMultiset.create();

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

    CanonicalAcl canonicalAcl = aclSpecs.acl;
    Reason reason =
        canonicalAcl.hasUndefinedRef(lineNumber)
            ? Reason.UNDEFINED_REFERENCE
            : unmatchable
                ? Reason.UNMATCHABLE
                : Reason.BLOCKING_LINES;
    _rows.add(
        Row.builder(COLUMN_METADATA)
            .put(COL_SOURCES, flatSources)
            .put(
                COL_LINES,
                acl.getLines()
                    .stream()
                    .map(l -> firstNonNull(l.getName(), l.toString()))
                    .collect(Collectors.toList()))
            .put(COL_BLOCKED_LINE_ACTION, blockedLineAction)
            .put(COL_BLOCKED_LINE_NUM, lineNumber)
            .put(COL_BLOCKING_LINE_NUMS, blockingLines)
            .put(COL_DIFF_ACTION, diffAction)
            .put(COL_REASON, reason)
            .put(
                COL_MESSAGE,
                buildMessage(
                    canonicalAcl.getOriginalAcl().getLines(),
                    lineNumber,
                    flatSources,
                    blockingLines,
                    reason))
            .build());
  }

  /**
   * Creates a {@link TableMetadata} object from the question.
   *
   * @param question The question
   * @return The resulting {@link TableMetadata} object
   */
  public static TableMetadata createMetadata(Question question) {
    List<ColumnMetadata> columnMetadata =
        COLUMN_METADATA.values().stream().collect(ImmutableList.toImmutableList());

    String textDesc = String.format("${%s}", COL_MESSAGE);
    DisplayHints dhints = question.getDisplayHints();
    if (dhints != null && dhints.getTextDesc() != null) {
      textDesc = dhints.getTextDesc();
    }
    return new TableMetadata(columnMetadata, textDesc);
  }

  public void addCycle(String hostname, List<String> aclsInCycle) {
    _rows.add(
        Row.builder(COLUMN_METADATA)
            .put(COL_SOURCES, ImmutableList.of(hostname + ": " + String.join(", ", aclsInCycle)))
            .put(COL_LINES, null)
            .put(COL_BLOCKED_LINE_NUM, null)
            .put(COL_BLOCKED_LINE_ACTION, null)
            .put(COL_BLOCKING_LINE_NUMS, null)
            .put(COL_DIFF_ACTION, null)
            .put(COL_REASON, Reason.CYCLICAL_REFERENCE)
            .put(
                COL_MESSAGE,
                String.format(
                    "Cyclic ACL references in node '%s': %s -> %s",
                    hostname, String.join(" -> ", aclsInCycle), aclsInCycle.get(0)))
            .build());
  }

  private static String buildMessage(
      List<IpAccessListLine> lines,
      int blockedLineNum,
      List<String> flatSources,
      SortedSet<Integer> blockingLines,
      Reason reason) {
    String blockedLineName =
        firstNonNull(lines.get(blockedLineNum).getName(), lines.get(blockedLineNum).toString());
    StringBuilder sb =
        new StringBuilder(
            String.format(
                "ACLs { %s } contain an unreachable line:\n  [index %d] %s\n",
                String.join("; ", flatSources), blockedLineNum, blockedLineName));
    switch (reason) {
      case UNDEFINED_REFERENCE:
        sb.append("This line references a structure that is not defined.");
        break;
      case UNMATCHABLE:
        sb.append("This line will never match any packet, independent of preceding lines.");
        break;
      case BLOCKING_LINES:
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
        break;
      default:
        throw new IllegalArgumentException(String.format("Unsupported reason: %s", reason));
    }
    return sb.toString();
  }

  public Multiset<Row> getRows() {
    return _rows;
  }
}
