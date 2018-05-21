package org.batfish.question.tracefilters;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.DisplayHints;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.Row.RowBuilder;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;

public class TraceFiltersAnswerElement extends TableAnswerElement {

  private static final String COLUMN_NODE = "node";
  private static final String COLUMN_FILTER_NAME = "filterName";
  private static final String COLUMN_FLOW = "flow";
  private static final String COLUMN_ACTION = "action";
  private static final String COLUMN_LINE_NUMBER = "lineNumber";
  private static final String COLUMN_LINE_CONTENT = "lineContent";

  /**
   * Creates a {@link TraceFiltersAnswerElement} object the right metadata
   *
   * @param question The question object for which the answer is being created, to borrow its {@link
   *     DisplayHints}
   * @return The creates the answer element object.
   */
  public static TraceFiltersAnswerElement create(TraceFiltersQuestion question) {
    List<ColumnMetadata> columnMetadata =
        ImmutableList.of(
            new ColumnMetadata(COLUMN_NODE, Schema.NODE, "Node", true, false),
            new ColumnMetadata(COLUMN_FILTER_NAME, Schema.STRING, "Filter name", true, false),
            new ColumnMetadata(COLUMN_FLOW, Schema.FLOW, "Evaluated flow", true, false),
            new ColumnMetadata(COLUMN_ACTION, Schema.STRING, "Outcome", false, true),
            new ColumnMetadata(COLUMN_LINE_NUMBER, Schema.INTEGER, "Line number", false, true),
            new ColumnMetadata(COLUMN_LINE_CONTENT, Schema.STRING, "Line content", false, true));
    DisplayHints dhints = question.getDisplayHints();
    if (dhints == null) {
      dhints = new DisplayHints();
      dhints.setTextDesc(
          String.format(
              "Filter ${%s} on node ${%s} will ${%s} flow ${%s} at line ${%s} ${%s}",
              COLUMN_FILTER_NAME,
              COLUMN_NODE,
              COLUMN_ACTION,
              COLUMN_FLOW,
              COLUMN_LINE_NUMBER,
              COLUMN_LINE_CONTENT));
    }
    TableMetadata metadata = new TableMetadata(columnMetadata, dhints);
    return new TraceFiltersAnswerElement(metadata);
  }

  @JsonCreator
  public TraceFiltersAnswerElement(
      @Nonnull @JsonProperty(PROP_METADATA) TableMetadata tableMetadata) {
    super(tableMetadata);
  }

  public Row getRow(
      String nodeName,
      String filterName,
      Flow flow,
      LineAction action,
      Integer matchLine,
      String lineContent) {
    RowBuilder row = Row.builder();
    row.put(COLUMN_NODE, new Node(nodeName))
        .put(COLUMN_FILTER_NAME, filterName)
        .put(COLUMN_FLOW, flow)
        .put(COLUMN_ACTION, action)
        .put(COLUMN_LINE_NUMBER, matchLine)
        .put(COLUMN_LINE_CONTENT, lineContent);
    return row.build();
  }
}
