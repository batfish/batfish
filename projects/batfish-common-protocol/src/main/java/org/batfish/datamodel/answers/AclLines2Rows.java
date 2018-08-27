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
  public static final String COL_CIRCULAR_REFERENCE = "circularReference";
  public static final String COL_MULTIPLE_BLOCKING_LINES = "multipleBlockingLines";
  public static final String COL_UNDEFINED_REFERENCE = "undefinedReference";
  public static final String COL_UNMATCHABLE = "unmatchable";
  public static final String COL_DIFF_ACTION = "differentAction";
  public static final String COL_MESSAGE = "message";

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
              COL_CIRCULAR_REFERENCE,
              new ColumnMetadata(
                  COL_CIRCULAR_REFERENCE, Schema.BOOLEAN, "Circular reference", false, true))
          .put(
              COL_UNDEFINED_REFERENCE,
              new ColumnMetadata(
                  COL_UNDEFINED_REFERENCE, Schema.BOOLEAN, "Undefined reference", false, true))
          .put(
              COL_UNMATCHABLE,
              new ColumnMetadata(COL_UNMATCHABLE, Schema.BOOLEAN, "Unmatchable line", false, true))
          .put(
              COL_MULTIPLE_BLOCKING_LINES,
              new ColumnMetadata(
                  COL_MULTIPLE_BLOCKING_LINES,
                  Schema.BOOLEAN,
                  "Multiple blocking lines",
                  false,
                  true))
          .put(COL_MESSAGE, new ColumnMetadata(COL_MESSAGE, Schema.STRING, "Message", false, false))
          .build();

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

    CanonicalAcl canonicalAcl = aclSpecs.acl;
    boolean undefinedRef = canonicalAcl.hasUndefinedRef(lineNumber);
    boolean independentlyUnmatchable = !undefinedRef && unmatchable;
    boolean multipleBlocking = !undefinedRef && !unmatchable && blockingLines.isEmpty();

    _rows.add(
        Row.builder(COLUMN_METADATA)
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
            .put(COL_CIRCULAR_REFERENCE, false)
            .put(COL_MULTIPLE_BLOCKING_LINES, multipleBlocking)
            .put(COL_UNDEFINED_REFERENCE, undefinedRef)
            .put(COL_UNMATCHABLE, independentlyUnmatchable)
            .put(
                COL_MESSAGE,
                buildMessage(
                    canonicalAcl.getOriginalAcl().getLines(),
                    lineNumber,
                    flatSources,
                    blockingLines,
                    undefinedRef,
                    independentlyUnmatchable,
                    multipleBlocking))
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

  @Override
  public void addCycle(String hostname, List<String> aclsInCycle) {
    _rows.add(
        Row.builder(COLUMN_METADATA)
            .put(COL_SOURCES, ImmutableList.of(hostname + ": " + String.join(", ", aclsInCycle)))
            .put(COL_LINES, null)
            .put(COL_BLOCKED_LINE_NUM, null)
            .put(COL_BLOCKING_LINE_NUMS, null)
            .put(COL_DIFF_ACTION, null)
            .put(COL_CIRCULAR_REFERENCE, true)
            .put(COL_UNDEFINED_REFERENCE, false)
            .put(COL_UNMATCHABLE, false)
            .put(COL_MULTIPLE_BLOCKING_LINES, false)
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
      boolean undefinedRef,
      boolean unmatchable,
      boolean multipleBlocking) {
    String blockedLineName =
        firstNonNull(lines.get(blockedLineNum).getName(), lines.get(blockedLineNum).toString());
    StringBuilder sb =
        new StringBuilder(
            String.format(
                "ACLs { %s } contain an unreachable line:\n  [index %d] %s\n",
                String.join("; ", flatSources), blockedLineNum, blockedLineName));
    if (undefinedRef) {
      sb.append("This line references a structure that is not defined.");
    } else if (unmatchable) {
      sb.append("This line will never match any packet, independent of preceding lines.");
    } else if (multipleBlocking) {
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
