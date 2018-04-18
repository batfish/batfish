package org.batfish.question.tracefilters;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;

public class TraceFiltersAnswerElement extends TableAnswerElement {

  private static final String COLUMN_NODE = "node";

  private static final String COLUMN_FILTER_NAME = "filterName";

  private static final String COLUMN_FLOW = "flow";

  private static final String COLUMN_ACTION = "action";

  private static final String COLUMN_LINE_NUMBER = "lineNumber";

  private static final String COLUMN_LINE_CONTENT = "lineContent";

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
    TableMetadata metadata = new TableMetadata(columnSchemas, null, null, null);
    return new TraceFiltersAnswerElement(metadata);
  }

  @JsonCreator
  public TraceFiltersAnswerElement(
      @Nonnull @JsonProperty(PROP_METADATA) TableMetadata tableMetadata) {
    super(tableMetadata);
  }

  public void addResult(
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
    addRow(row);
  }
}
