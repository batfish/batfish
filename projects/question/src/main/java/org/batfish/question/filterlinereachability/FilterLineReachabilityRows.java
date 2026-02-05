package org.batfish.question.filterlinereachability;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
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

  /** Adds row for line at index {@code lineNumber} with reason {@code reason}. */
  void addRowForLine(UnreachableFilterLine line) {
    AclSpecs aclSpecs = line.getAclSpecs();
    int lineNumber = line.getLineNumber();

    if (aclSpecs.acl.inCycle(lineNumber)) {
      return;
    }

    IpAccessList acl = aclSpecs.acl.getOriginalAcl();
    AclLine blockedLine = acl.getLines().get(lineNumber);

    // FTD two-stage filter: Skip reporting regular ACL rules blocked by Prefilter "trust" rules
    // Trust rules fast-path traffic and are intentionally evaluated before regular permit rules
    if (isFtdTwoStageFilterShadowing(line, acl)) {
      return;
    }

    // All the host-acl pairs that contain this canonical acl
    List<String> flatSources =
        aclSpecs.sources.entrySet().stream()
            .map(e -> e.getKey() + ": " + String.join(", ", e.getValue()))
            .collect(Collectors.toList());

    Row.TypedRowBuilder row =
        Row.builder(COLUMN_METADATA)
            .put(COL_SOURCES, flatSources)
            .put(COL_UNREACHABLE_LINE_ACTION, ActionGetter.getLineBehavior(blockedLine))
            .put(COL_UNREACHABLE_LINE, firstNonNull(blockedLine.getName(), blockedLine.toString()))
            .put(COL_BLOCKING_LINES, ImmutableList.of())
            .put(COL_DIFF_ACTION, false);

    line.accept(
        new UnreachableFilterLineVisitor<Void>() {
          @Override
          public Void visitBlockedFilterLine(BlockedFilterLine line) {
            row.put(COL_REASON, Reason.BLOCKING_LINES)
                .put(
                    COL_BLOCKING_LINES,
                    line.getBlockingLines().stream()
                        .map(
                            i -> {
                              AclLine l = acl.getLines().get(i);
                              return firstNonNull(l.getName(), l.toString());
                            })
                        .collect(ImmutableList.toImmutableList()))
                .put(COL_DIFF_ACTION, line.hasDiffAction());
            return null;
          }

          @Override
          public Void visitIndependentlyUnmatchableFilterLine(
              IndependentlyUnmatchableFilterLine line) {
            row.put(COL_REASON, Reason.INDEPENDENTLY_UNMATCHABLE);
            return null;
          }

          @Override
          public Void visitFilterLineWithUndefinedReference(FilterLineWithUndefinedReference line) {
            row.put(COL_REASON, Reason.UNDEFINED_REFERENCE);
            return null;
          }
        });

    _rows.add(row.build());
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

  /**
   * Check if this unreachable line is part of FTD two-stage filtering (Prefilter trust + regular
   * ACL). FTD Prefilter policies with "trust" actions fast-path traffic before regular ACL
   * evaluation. Regular permit rules shadowed by trust rules are expected behavior, not real
   * issues.
   *
   * @return true if blocked line should be skipped (FTD two-stage filtering), false otherwise
   */
  private boolean isFtdTwoStageFilterShadowing(UnreachableFilterLine line, IpAccessList acl) {
    // Only applies to BLOCKING_LINES case
    if (!(line instanceof BlockedFilterLine)) {
      return false;
    }

    BlockedFilterLine blockedLine = (BlockedFilterLine) line;

    // Check if any blocking line is a Prefilter trust rule
    for (Integer blockingLineNum : blockedLine.getBlockingLines()) {
      AclLine blockingLine = acl.getLines().get(blockingLineNum);
      String blockingName = blockingLine.getName();

      // FTD Prefilter trust rules have "Prefilter-FTD" in their name and contain "trust"
      if (blockingName != null
          && blockingName.contains("Prefilter-FTD")
          && blockingName.contains(" trust ")) {
        return true;
      }
    }

    return false;
  }
}
