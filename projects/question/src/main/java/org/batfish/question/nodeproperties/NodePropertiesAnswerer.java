package org.batfish.question.nodeproperties;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Multiset;
import com.google.common.collect.UnmodifiableListIterator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.DisplayHints;
import org.batfish.datamodel.questions.NodePropertySpecifier;
import org.batfish.datamodel.questions.PropertySpecifier;
import org.batfish.datamodel.questions.PropertySpecifier.PropertyDescriptor;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.Row.RowBuilder;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;

public class NodePropertiesAnswerer extends Answerer {

  public static final String COL_NODE = "node";
  /* TODO: Need to be add the role-specific column when handling role-based outliers.
   */
  // private static final String ROLE_TAG_DEFAULT = "defaultroletag";

  public NodePropertiesAnswerer(Question question, IBatfish batfish) {
    super(question, batfish);
  }

  /**
   * Creates a {@link TableMetadata} object from the question.
   *
   * @param question The question
   * @return The resulting {@link TableMetadata} object
   */
  @SuppressWarnings("unchecked")
  static TableMetadata createPropertiesMetadata(
      NodePropertiesQuestion question,
      Map<String, Configuration> configurations,
      Set<String> nodes) {
    ImmutableList.Builder<ColumnMetadata> columnMetadataList = ImmutableList.builder();
    Schema schema = Schema.STRING;

    ImmutableList.builder();
    columnMetadataList.add(new ColumnMetadata(COL_NODE, Schema.NODE, "Node", true, false));

    /* TODO: Need to be add the role-specific column when handling role-based outliers.
     */
    //    columnMetadataList.add(
    //        new ColumnMetadata(
    //            ROLE_TAG_DEFAULT, Schema.STRING, "role-tag", Boolean.TRUE, Boolean.FALSE));

    for (String nodeName : nodes) {
      for (String property : question.getPropertySpec().getMatchingProperties()) {
        Configuration nodeConfiguration = configurations.get(nodeName);

        Object propertyValueSet =
            NodePropertySpecifier.JAVA_MAP.get(property).getGetter().apply(nodeConfiguration);
        // Get the schema and see if we can use this instead of TreeMap in the conditions.

        if (propertyValueSet != null) {
          String propertyValueType = getPropertyValueType(propertyValueSet);
          int varSize = getPropertyValueSize(property, propertyValueSet);

          if (varSize > 0) {
            if (propertyValueType.equalsIgnoreCase("TreeMap")) {
              schema = Schema.OBJECT;

              for (Map.Entry<Object, Object> entry :
                  ((TreeMap<Object, Object>) propertyValueSet).entrySet()) {

                Object key = entry.getKey();
                String propertyName = key.toString();

                if (!propertyName.startsWith("~")
                    && !propertyName.startsWith("..")
                    && !isEntryPresent(columnMetadataList, propertyName)) {

                  columnMetadataList.add(
                      new ColumnMetadata(
                          propertyName, schema, property, Boolean.FALSE, Boolean.TRUE));
                }
              }
            } else if (propertyValueType.equalsIgnoreCase("TreeSet")) {
              schema = Schema.list(Schema.STRING);

              if (!property.startsWith("~")
                  && !property.startsWith("..")
                  && !isEntryPresent(columnMetadataList, property)) {

                columnMetadataList.add(
                    new ColumnMetadata(property, schema, property, Boolean.FALSE, Boolean.TRUE));
              }
            } else {
              schema = Schema.STRING;
              if (!property.startsWith("~")
                  && !property.startsWith("..")
                  && !isEntryPresent(columnMetadataList, property)) {

                columnMetadataList.add(
                    new ColumnMetadata(property, schema, property, Boolean.FALSE, Boolean.TRUE));
              }
            }
          } else {
            /* See if it suits the list of outlier detection for same Name.
             */
          }
        }
      }
    }

    DisplayHints dhints = question.getDisplayHints();
    if (dhints == null) {
      dhints = new DisplayHints();
      dhints.setTextDesc(String.format("Properties of node ${%s}.", COL_NODE));
    }
    return new TableMetadata(columnMetadataList.build(), dhints);
  }

  private static boolean isEntryPresent(
      Builder<ColumnMetadata> columnMetadataList, String propertyName) {

    UnmodifiableListIterator<ColumnMetadata> iter = columnMetadataList.build().listIterator();
    boolean flag = false;
    while (iter.hasNext()) {
      ColumnMetadata columnVal = iter.next();
      String propertyIterValName = columnVal.getName();
      if (propertyIterValName.equals(propertyName)) {
        flag = true;
        break;
      }
    }
    return flag;
  }

  private static String getPropertyValueType(Object propertiesFullValueSet) {
    String propertyValueType = propertiesFullValueSet.getClass().getSimpleName();
    return propertyValueType;
  }

  private static int getPropertyValueSize(String property, Object propertyValueSetFull) {

    int varSize = 0;
    String propertyValueType = propertyValueSetFull.getClass().getSimpleName();

    Object propertyValueSetFullT;
    if (propertyValueType.equalsIgnoreCase("Ip")) {
      propertyValueSetFullT =
          (Ip)
              PropertySpecifier.convertTypeIfNeeded(
                  propertyValueSetFull, NodePropertySpecifier.JAVA_MAP.get(property));
      varSize = propertyValueSetFullT.getClass().getSimpleName().length();
    } else if (propertyValueType.equalsIgnoreCase("String")) {
      propertyValueSetFullT =
          (String)
              PropertySpecifier.convertTypeIfNeeded(
                  propertyValueSetFull, NodePropertySpecifier.JAVA_MAP.get(property));
      varSize = propertyValueSetFullT.getClass().getFields().length;
    } else if (propertyValueType.equalsIgnoreCase("TreeMap")) {
      propertyValueSetFullT =
          PropertySpecifier.convertTypeIfNeeded(
              propertyValueSetFull, NodePropertySpecifier.JAVA_MAP.get(property));
      varSize = ((HashSet) propertyValueSetFullT).size();
    } else if (propertyValueType.equalsIgnoreCase("TreeSet")) {
      propertyValueSetFullT =
          (TreeSet)
              PropertySpecifier.convertTypeIfNeeded(
                  propertyValueSetFull, NodePropertySpecifier.JAVA_MAP.get(property));
      varSize = ((TreeSet) propertyValueSetFullT).size();
    } else if (propertyValueType.equalsIgnoreCase("HashSet")) {
      propertyValueSetFullT =
          (HashSet)
              PropertySpecifier.convertTypeIfNeeded(
                  propertyValueSetFull, NodePropertySpecifier.JAVA_MAP.get(property));
      varSize = ((HashSet) propertyValueSetFullT).size();
    } else {
      propertyValueSetFullT =
          PropertySpecifier.convertTypeIfNeeded(
              propertyValueSetFull, NodePropertySpecifier.JAVA_MAP.get(property));
      varSize = propertyValueSetFullT.getClass().getFields().length;
    }
    return varSize;
  }

  /**
   * Creates a {@link TableMetadata} object from the question.
   *
   * @param question The question
   * @return The resulting {@link TableMetadata} object
   */
  static TableMetadata createMetadata(NodePropertiesQuestion question) {
    List<ColumnMetadata> columnMetadata =
        new ImmutableList.Builder<ColumnMetadata>()
            .add(new ColumnMetadata(COL_NODE, Schema.NODE, "Node", true, false))
            .addAll(
                question
                    .getPropertySpec()
                    .getMatchingProperties()
                    .stream()
                    .map(
                        prop ->
                            new ColumnMetadata(
                                prop,
                                NodePropertySpecifier.JAVA_MAP.get(prop).getSchema(),
                                "Property " + prop,
                                false,
                                true))
                    .collect(Collectors.toList()))
            .build();

    DisplayHints dhints = question.getDisplayHints();
    if (dhints == null) {
      dhints = new DisplayHints();
      dhints.setTextDesc(String.format("Properties of node ${%s}.", COL_NODE));
    }
    return new TableMetadata(columnMetadata, dhints);
  }

  @Override
  public AnswerElement answer() {

    NodePropertiesQuestion question = (NodePropertiesQuestion) _question;
    Map<String, Configuration> configurations = _batfish.loadConfigurations();
    Set<String> nodes = question.getNodeRegex().getMatchingNodes(_batfish);

    TableMetadata tablePropertiesMetadata =
        createPropertiesMetadata(question, configurations, nodes);
    TableAnswerElement answer = new TableAnswerElement(tablePropertiesMetadata);

    Multiset<Row> propertyRows =
        rawPropertiesAnswer(question, configurations, nodes, tablePropertiesMetadata);
    answer.postProcessAnswer(question, propertyRows);

    return answer;
  }

  @VisibleForTesting
  static Multiset<Row> rawAnswer(
      NodePropertiesQuestion question,
      Map<String, Configuration> configurations,
      Set<String> nodes,
      Map<String, ColumnMetadata> columns) {
    Multiset<Row> rows = HashMultiset.create();

    for (String nodeName : nodes) {
      RowBuilder row = Row.builder(columns).put(COL_NODE, new Node(nodeName));

      for (String property : question.getPropertySpec().getMatchingProperties()) {

        PropertySpecifier.fillProperty(
            NodePropertySpecifier.JAVA_MAP.get(property),
            configurations.get(nodeName),
            property,
            row);
      }
      rows.add(row.build());
    }

    return rows;
  }

  @VisibleForTesting
  @SuppressWarnings("unchecked")
  static Multiset<Row> rawPropertiesAnswer(
      NodePropertiesQuestion question,
      Map<String, Configuration> configurations,
      Set<String> nodes,
      TableMetadata tableMetadata) {
    Multiset<Row> rows = HashMultiset.create();

    Map<String, ColumnMetadata> columns = tableMetadata.toColumnMap();

    for (String nodeName : nodes) {
      RowBuilder row = Row.builder();
      row.put(COL_NODE, new Node(nodeName));
      /* TODO: Need to be add the role-specific column when handling role-based outliers.
       */
      // row.put(ROLE_TAG_DEFAULT, new Node("default-role"));
      Configuration nodeConfiguration = configurations.get(nodeName);
      for (Map.Entry<String, ColumnMetadata> columnEntry : columns.entrySet()) {
        Object propertyValueSet = null;

        ColumnMetadata columnValue = columnEntry.getValue();
        String columnName = columnValue.getName();
        String columnProperty = columnValue.getDescription();

        PropertyDescriptor<Configuration> propertyDescriptor =
            NodePropertySpecifier.JAVA_MAP.get(columnProperty);
        if (propertyDescriptor != null) {
          propertyValueSet = propertyDescriptor.getGetter().apply(nodeConfiguration);
          if (propertyValueSet != null
              && getPropertyValueSize(columnProperty, propertyValueSet) > 0) {
            String propertyValueType = getPropertyValueType(propertyValueSet);

            if (propertyValueType.equalsIgnoreCase("TreeMap")) {

              boolean checkConfFlag = false;
              for (Map.Entry<Object, Object> entry :
                  ((TreeMap<Object, Object>) propertyValueSet).entrySet()) {
                Object keyProp = entry.getKey();
                String propertyName = keyProp.toString();
                if (propertyName.equals(columnName)) {
                  Object rowValue = entry.getValue();
                  if (!propertyName.startsWith("~") && !propertyName.startsWith("..")) {
                    row.put(propertyName, rowValue);
                    checkConfFlag = true;
                    break;
                  }
                }
              }
              if (!checkConfFlag) {
                row.put(columnName, null);
              }

            } else if (propertyValueType.equalsIgnoreCase("TreeSet")) {

              if (!columnProperty.startsWith("~") && !columnProperty.startsWith("..")) {
                row.put(columnProperty, ((TreeSet) propertyValueSet));
              }
            } else {
              if (!columnProperty.startsWith("~") && !columnProperty.startsWith("..")) {
                row.put(columnProperty, propertyValueSet);
              }
            }
          } else {
            /* To fill the missing cells in the answer Table. */
            row.put(columnName, null);
          }
        }
      }
      rows.add(row.build());
    }
    return rows;
  }
}
