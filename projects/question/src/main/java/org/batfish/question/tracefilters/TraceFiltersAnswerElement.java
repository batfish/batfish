package org.batfish.question.tracefilters;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSortedMap;
import java.util.Comparator;
import java.util.SortedMap;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.DisplayHints;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
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
    SortedMap<String, ColumnMetadata> columnMetadata =
        new ImmutableSortedMap.Builder<String, ColumnMetadata>(Comparator.naturalOrder())
            .put(COLUMN_NODE, new ColumnMetadata(Schema.NODE, "Node", true, false))
            .put(COLUMN_FILTER_NAME, new ColumnMetadata(Schema.STRING, "Filter name", true, false))
            .put(COLUMN_FLOW, new ColumnMetadata(Schema.FLOW, "Evaluated flow", true, false))
            .put(COLUMN_ACTION, new ColumnMetadata(Schema.STRING, "Outcome", false, true))
            .put(COLUMN_LINE_NUMBER, new ColumnMetadata(Schema.INTEGER, "Line number", false, true))
            .put(
                COLUMN_LINE_CONTENT, new ColumnMetadata(Schema.STRING, "Line content", false, true))
            .build();
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

  @Override
  public Object fromRow(Row o) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  public Row getRow(
      String nodeName,
      String filterName,
      Flow flow,
      LineAction action,
      Integer matchLine,
      String lineContent) {
    Row row = new Row();
    row.put(COLUMN_NODE, new Node(nodeName))
        .put(COLUMN_FILTER_NAME, filterName)
        .put(COLUMN_FLOW, flow)
        .put(COLUMN_ACTION, action)
        .put(COLUMN_LINE_NUMBER, matchLine)
        .put(COLUMN_LINE_CONTENT, lineContent);
    return row;
  }

  @Override
  public Row toRow(Object object) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }
}
