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
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.acl.CanonicalAcl;
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
                  COL_UNREACHABLE_LINE, Schema.STRING, "Unreachable line", true, false))
          .put(
              COL_UNREACHABLE_LINE_ACTION,
              new ColumnMetadata(
                  COL_UNREACHABLE_LINE_ACTION,
                  Schema.STRING,
                  "Unreachable line action",
                  true,
                  false))
          .put(
              COL_BLOCKING_LINES,
              new ColumnMetadata(
                  COL_BLOCKING_LINES, Schema.list(Schema.STRING), "Blocking lines", false, true))
          .put(
              COL_DIFF_ACTION,
              new ColumnMetadata(COL_DIFF_ACTION, Schema.BOOLEAN, "Different action", false, true))
          .put(
              COL_REASON,
              new ColumnMetadata(COL_REASON, Schema.STRING, "Reason unreachable", false, true))
          .put(
              COL_ADDITIONAL_INFO,
              new ColumnMetadata(
                  COL_ADDITIONAL_INFO, Schema.STRING, "Additional information", false, false))
          .build();

  private final Multiset<Row> _rows = HashMultiset.create();

  public void addUnreachableLine(
      AclSpecs aclSpecs,
      int lineNumber,
      boolean unmatchable,
      @Nonnull SortedSet<Integer> blockingLines) {

    if (aclSpecs.acl.inCycle(lineNumber)) {
      return;
    }

    IpAccessList acl = aclSpecs.acl.getOriginalAcl();
    IpAccessListLine blockedLine = acl.getLines().get(lineNumber);
    boolean diffAction =
        blockingLines.stream()
            .anyMatch(i -> !acl.getLines().get(i).getAction().equals(blockedLine.getAction()));

    // All the host-acl pairs that contain this canonical acl
    List<String> flatSources =
        aclSpecs.sources.entrySet().stream()
            .map(e -> e.getKey() + ": " + String.join(", ", e.getValue()))
            .collect(Collectors.toList());

    CanonicalAcl canonicalAcl = aclSpecs.acl;
    Reason reason =
        canonicalAcl.hasUndefinedRef(lineNumber)
            ? Reason.UNDEFINED_REFERENCE
            : unmatchable ? Reason.INDEPENDENTLY_UNMATCHABLE : Reason.BLOCKING_LINES;
    _rows.add(
        Row.builder(COLUMN_METADATA)
            .put(COL_SOURCES, flatSources)
            .put(COL_UNREACHABLE_LINE_ACTION, blockedLine.getAction())
            .put(COL_UNREACHABLE_LINE, firstNonNull(blockedLine.getName(), blockedLine.toString()))
            .put(
                COL_BLOCKING_LINES,
                blockingLines.stream()
                    .map(
                        i -> {
                          IpAccessListLine l = acl.getLines().get(i);
                          return firstNonNull(l.getName(), l.toString());
                        })
                    .collect(ImmutableList.toImmutableList()))
            .put(COL_DIFF_ACTION, diffAction)
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
            "Filters {%s} contain an unreachable line: ${%s}", COL_SOURCES, COL_UNREACHABLE_LINE);
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
