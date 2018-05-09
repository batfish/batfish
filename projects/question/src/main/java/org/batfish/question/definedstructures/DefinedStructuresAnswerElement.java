package org.batfish.question.definedstructures;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.DisplayHints;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;

public class DefinedStructuresAnswerElement extends TableAnswerElement {

  public static final String COL_DEFINITION_LINES = "definitionLines";
  public static final String COL_NODE_NAME = "nodeName";
  public static final String COL_NUM_REFERENCES = "numReferences";
  public static final String COL_STRUCT_NAME = "structName";
  public static final String COL_STRUCT_TYPE = "structType";

  @JsonCreator
  public DefinedStructuresAnswerElement(
      @Nonnull @JsonProperty(PROP_METADATA) TableMetadata tableMetadata) {
    super(tableMetadata);
  }

  public static TableMetadata createMetadata(Question question) {
    Map<String, ColumnMetadata> columnMetadataMap = new HashMap<>();
    columnMetadataMap.put(
        COL_DEFINITION_LINES,
        new ColumnMetadata(
            Schema.list(Schema.INTEGER), "Lines where the structure is defined", false, true));
    columnMetadataMap.put(
        COL_NODE_NAME,
        new ColumnMetadata(Schema.STRING, "Node where the structure is defined", true, false));
    columnMetadataMap.put(
        COL_NUM_REFERENCES,
        new ColumnMetadata(Schema.INTEGER, "Number of references to this structure", false, true));
    columnMetadataMap.put(
        COL_STRUCT_NAME, new ColumnMetadata(Schema.STRING, "Name of the structure", true, false));
    columnMetadataMap.put(
        COL_STRUCT_TYPE, new ColumnMetadata(Schema.STRING, "Type of the structure", true, false));

    DisplayHints dhints = question.getDisplayHints();
    if (dhints == null) {
      dhints = new DisplayHints();
      dhints.setTextDesc(
          String.format(
              "On ${%s} struct ${%s}:${%s} on lines ${%s} has ${%s} references.",
              COL_NODE_NAME,
              COL_STRUCT_TYPE,
              COL_STRUCT_NAME,
              COL_DEFINITION_LINES,
              COL_NUM_REFERENCES));
    }
    return new TableMetadata(columnMetadataMap, dhints);
  }

  @Override
  public Object fromRow(Row row) {
    return fromRowStatic(row);
  }

  public static DefinedStructureRow fromRowStatic(Row row) {
    SortedSet<Integer> definitionLines =
        row.get(COL_DEFINITION_LINES, new TypeReference<SortedSet<Integer>>() {});
    Integer numReferences = row.get(COL_NUM_REFERENCES, Integer.class);
    String nodeName = row.get(COL_NODE_NAME, String.class);
    String structName = row.get(COL_STRUCT_NAME, String.class);
    String structType = row.get(COL_STRUCT_TYPE, String.class);

    return new DefinedStructureRow(
        nodeName, structType, structName, numReferences, definitionLines);
  }

  @Override
  public Row toRow(Object o) {
    return toRowStatic((DefinedStructureRow) o);
  }

  public static Row toRowStatic(DefinedStructureRow info) {
    Row row = new Row();
    row.put(COL_DEFINITION_LINES, info.getDefinitionLines())
        .put(COL_NODE_NAME, info.getNodeName())
        .put(COL_NUM_REFERENCES, info.getNumReferences())
        .put(COL_STRUCT_NAME, new Node(info.getStructName()))
        .put(COL_STRUCT_TYPE, info.getStructType());
    return row;
  }
}
