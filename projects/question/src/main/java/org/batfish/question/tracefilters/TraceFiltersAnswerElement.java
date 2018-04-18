package org.batfish.question.tracefilters;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.DisplayHints;
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
   * @param question
   * @return
   */
  public static TraceFiltersAnswerElement create(TraceFiltersQuestion question) {
    Map<String, Schema> columnSchemas =
        new ImmutableMap.Builder<String, Schema>()
            .put(COLUMN_NODE, new Schema("Node"))
            .put(COLUMN_FILTER_NAME, new Schema("String"))
            .put(COLUMN_FLOW, new Schema("Flow"))
            .put(COLUMN_ACTION, new Schema("String"))
            .put(COLUMN_LINE_NUMBER, new Schema("Integer"))
            .put(COLUMN_LINE_CONTENT, new Schema("String"))
            .build();
    List<String> primaryKey =
        new ImmutableList.Builder<String>()
            .add(COLUMN_NODE)
            .add(COLUMN_FILTER_NAME)
            .add(COLUMN_FLOW)
            .build();
    List<String> primaryValue =
        new ImmutableList.Builder<String>()
            .add(COLUMN_ACTION)
            .add(COLUMN_LINE_NUMBER)
            .add(COLUMN_LINE_CONTENT)
            .build();
    DisplayHints dhints = question.getDisplayHints();
    if (dhints == null) {
      dhints = new DisplayHints();
      dhints.setTextDesc(
          String.format(
              "Filter ${%s} on node {%s} will {%s} flow {%s} at line {%s} {%s}",
              COLUMN_FILTER_NAME,
              COLUMN_NODE,
              COLUMN_ACTION,
              COLUMN_FLOW,
              COLUMN_LINE_NUMBER,
              COLUMN_LINE_CONTENT));
    }
    TableMetadata metadata = new TableMetadata(columnSchemas, primaryKey, primaryValue, dhints);
    return new TraceFiltersAnswerElement(metadata);
  }

  @JsonCreator
  public TraceFiltersAnswerElement(
      @Nonnull @JsonProperty(PROP_METADATA) TableMetadata tableMetadata) {
    super(tableMetadata);
  }

  public ObjectNode getRow(
      String nodeName,
      String filterName,
      Flow flow,
      LineAction action,
      Integer matchLine,
      String lineContent) {
    ObjectNode row = BatfishObjectMapper.mapper().createObjectNode();
    row.set(COLUMN_NODE, BatfishObjectMapper.mapper().valueToTree(new Node(nodeName)));
    row.set(COLUMN_FILTER_NAME, BatfishObjectMapper.mapper().valueToTree(filterName));
    row.set(COLUMN_FLOW, BatfishObjectMapper.mapper().valueToTree(flow));
    row.set(COLUMN_ACTION, BatfishObjectMapper.mapper().valueToTree(action));
    row.set(COLUMN_LINE_NUMBER, BatfishObjectMapper.mapper().valueToTree(matchLine));
    row.set(COLUMN_LINE_CONTENT, BatfishObjectMapper.mapper().valueToTree(lineContent));
    return row;
  }
}
