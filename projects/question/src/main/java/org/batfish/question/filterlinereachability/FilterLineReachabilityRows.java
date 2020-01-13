package org.batfish.question.filterlinereachability;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Multiset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.acl.ActionGetter;
import org.batfish.datamodel.answers.AclSpecs;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.questions.DisplayHints;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableMetadata;
import org.batfish.question.filterlinereachability.FilterLineReachabilityAnswerer.BlockingProperties;

/** Represents answers to aclReachability. */
@ParametersAreNonnullByDefault
public class FilterLineReachabilityRows {
  public static final String COL_SOURCES = "Sources";
  public static final String COL_UNREACHABLE_LINE_ACTION = "Unreachable_Line_Action";
  public static final String COL_UNREACHABLE_LINE = "Unreachable_Line";
  public static final String COL_BLOCKING_LINES = "Blocking_Lines";
  public static final String COL_REASON = "Reason";
  public static final String COL_DIFF_ACTION = "Different_Action";
  public static final String COL_ADDITIONAL_INFO = "Additional_Info";

  public enum Reason {
    CYCLICAL_REFERENCE,
    BLOCKING_LINES,
    UNDEFINED_REFERENCE,
    INDEPENDENTLY_UNMATCHABLE
  }

  public static final Map<String, ColumnMetadata> COLUMN_METADATA =
      ImmutableMap.<String, ColumnMetadata>builder()
          .put(
              COL_SOURCES,
              new ColumnMetadata(
                  COL_SOURCES, Schema.list(Schema.STRING), "Filter sources", true, false))
          .put(
              COL_UNREACHABLE_LINE,
              new ColumnMetadata(
                  COL_UNREACHABLE_LINE,
                  Schema.STRING,
                  "Filter line that cannot be matched (i.e., unreachable)",
                  true,
                  false))
          .put(
              COL_UNREACHABLE_LINE_ACTION,
              new ColumnMetadata(
                  COL_UNREACHABLE_LINE_ACTION,
                  Schema.STRING,
                  "Action performed by the unreachable line (e.g., PERMIT or DENY)",
                  true,
                  false))
          .put(
              COL_BLOCKING_LINES,
              new ColumnMetadata(
                  COL_BLOCKING_LINES,
                  Schema.list(Schema.STRING),
                  "Lines that, when combined, cover the unreachable line",
                  false,
                  true))
          .put(
              COL_DIFF_ACTION,
              new ColumnMetadata(
                  COL_DIFF_ACTION,
                  Schema.BOOLEAN,
                  "Whether unreachable line has an action different from the blocking line(s)",
                  false,
                  true))
          .put(
              COL_REASON,
              new ColumnMetadata(
                  COL_REASON, Schema.STRING, "The reason a line is unreachable", false, true))
          .put(
              COL_ADDITIONAL_INFO,
              new ColumnMetadata(
                  COL_ADDITIONAL_INFO, Schema.STRING, "Additional information", false, false))
          .build();

  private final Multiset<Row> _rows = HashMultiset.create();

  /** Adds row for unmatchable line at index {@code lineNumber}. */
  public void addUnmatchableLine(AclSpecs aclSpecs, int lineNumber) {
    Reason reason =
        aclSpecs.acl.hasUndefinedRef(lineNumber)
            ? Reason.UNDEFINED_REFERENCE
            : Reason.INDEPENDENTLY_UNMATCHABLE;
    addRowForLine(
        aclSpecs, lineNumber, reason, new BlockingProperties(ImmutableSortedSet.of(), false));
  }

  /** Adds row for blocked line at index {@code lineNumber}. */
  public void addBlockedLine(AclSpecs aclSpecs, int lineNumber, BlockingProperties blockingProps) {
    addRowForLine(aclSpecs, lineNumber, Reason.BLOCKING_LINES, blockingProps);
  }

  /** Adds row for line at index {@code lineNumber} with reason {@code reason}. */
  private void addRowForLine(
      AclSpecs aclSpecs, int lineNumber, Reason reason, BlockingProperties blockingProps) {
    if (aclSpecs.acl.inCycle(lineNumber)) {
      return;
    }

    IpAccessList acl = aclSpecs.acl.getOriginalAcl();
    AclLine blockedLine = acl.getLines().get(lineNumber);

    // All the host-acl pairs that contain this canonical acl
    List<String> flatSources =
        aclSpecs.sources.entrySet().stream()
            .map(e -> e.getKey() + ": " + String.join(", ", e.getValue()))
            .collect(Collectors.toList());

    _rows.add(
        Row.builder(COLUMN_METADATA)
            .put(COL_SOURCES, flatSources)
            .put(COL_UNREACHABLE_LINE_ACTION, ActionGetter.getLineBehavior(blockedLine))
            .put(COL_UNREACHABLE_LINE, firstNonNull(blockedLine.getName(), blockedLine.toString()))
            .put(
                COL_BLOCKING_LINES,
                blockingProps.getBlockingLineNums().stream()
                    .map(
                        i -> {
                          AclLine l = acl.getLines().get(i);
                          return firstNonNull(l.getName(), l.toString());
                        })
                    .collect(ImmutableList.toImmutableList()))
            .put(COL_DIFF_ACTION, blockingProps.getDiffAction())
            .put(COL_REASON, reason)
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

    String textDesc =
        String.format(
            "Filters ${%s} contain an unreachable line: ${%s}", COL_SOURCES, COL_UNREACHABLE_LINE);
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
            .put(COL_UNREACHABLE_LINE, null)
            .put(COL_UNREACHABLE_LINE_ACTION, null)
            .put(COL_BLOCKING_LINES, null)
            .put(COL_DIFF_ACTION, null)
            .put(COL_REASON, Reason.CYCLICAL_REFERENCE)
            .put(
                COL_ADDITIONAL_INFO,
                String.format(
                    "Cyclic references in node '%s': %s -> %s",
                    hostname, String.join(" -> ", aclsInCycle), aclsInCycle.get(0)))
            .build());
  }

  public Multiset<Row> getRows() {
    return _rows;
  }
}
