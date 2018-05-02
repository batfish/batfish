package org.batfish.question.definedstructures;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.DisplayHints;
import org.batfish.datamodel.questions.Question;
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
    Map<String, Schema> columnSchemas = new HashMap<>();
    columnSchemas.put(COL_DEFINITION_LINES, new Schema("List<Integer>"));
    columnSchemas.put(COL_NODE_NAME, new Schema("String"));
    columnSchemas.put(COL_NUM_REFERENCES, new Schema("Integer"));
    columnSchemas.put(COL_STRUCT_NAME, new Schema("String"));
    columnSchemas.put(COL_STRUCT_TYPE, new Schema("String"));

    List<String> primaryKey = new LinkedList<>();
    primaryKey.add(COL_NODE_NAME);
    primaryKey.add(COL_STRUCT_TYPE);
    primaryKey.add(COL_STRUCT_NAME);

    List<String> primaryValue = new LinkedList<>();
    primaryValue.add(COL_NUM_REFERENCES);
    primaryValue.add(COL_DEFINITION_LINES);

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
    return new TableMetadata(columnSchemas, primaryKey, primaryValue, dhints);
  }

  @Override
  public Object fromRow(ObjectNode row) throws IOException {
    return fromRowStatic(row);
  }

  public static DefinedStructureRow fromRowStatic(ObjectNode row) throws IOException {
    SortedSet<Integer> definitionLines = new TreeSet<>();
    definitionLines =
        BatfishObjectMapper.mapper()
            .readValue(
                BatfishObjectMapper.mapper().treeAsTokens(row.get(COL_DEFINITION_LINES)),
                new TypeReference<SortedSet<Integer>>() {});
    Integer numReferences =
        BatfishObjectMapper.mapper().treeToValue(row.get(COL_NUM_REFERENCES), Integer.class);
    String nodeName =
        BatfishObjectMapper.mapper().treeToValue(row.get(COL_NODE_NAME), String.class);
    String structName =
        BatfishObjectMapper.mapper().treeToValue(row.get(COL_STRUCT_NAME), String.class);
    String structType =
        BatfishObjectMapper.mapper().treeToValue(row.get(COL_STRUCT_TYPE), String.class);

    return new DefinedStructureRow(
        nodeName, structType, structName, numReferences, definitionLines);
  }

  @Override
  public ObjectNode toRow(Object o) {
    return toRowStatic((DefinedStructureRow) o);
  }

  public static ObjectNode toRowStatic(DefinedStructureRow info) {
    ObjectNode row = BatfishObjectMapper.mapper().createObjectNode();
    row.set(
        COL_DEFINITION_LINES, BatfishObjectMapper.mapper().valueToTree(info.getDefinitionLines()));
    row.set(COL_NODE_NAME, BatfishObjectMapper.mapper().valueToTree(info.getNodeName()));
    row.set(COL_NUM_REFERENCES, BatfishObjectMapper.mapper().valueToTree(info.getNumReferences()));
    row.set(
        COL_STRUCT_NAME, BatfishObjectMapper.mapper().valueToTree(new Node(info.getStructName())));
    row.set(COL_STRUCT_TYPE, BatfishObjectMapper.mapper().valueToTree(info.getStructType()));
    return row;
  }
}
