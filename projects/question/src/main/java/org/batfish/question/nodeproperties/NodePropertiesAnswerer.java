package org.batfish.question.nodeproperties;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.Row;

public class NodePropertiesAnswerer extends Answerer {

  public NodePropertiesAnswerer(Question question, IBatfish batfish) {
    super(question, batfish);
  }

  @Override
  public AnswerElement answer() {
    NodePropertiesQuestion question = (NodePropertiesQuestion) _question;
    Map<String, Schema> schemas = new HashMap<>();
    Multiset<Row> sessions = rawAnswer(question, schemas);

    NodePropertiesAnswerElement answer =
        new NodePropertiesAnswerElement(
            NodePropertiesAnswerElement.createMetadata(question, schemas));
    answer.postProcessAnswer(question, sessions);
    return answer;
  }

  private Multiset<Row> rawAnswer(NodePropertiesQuestion question, Map<String, Schema> schemas) {
    Multiset<Row> rows = HashMultiset.create();
    Map<String, Configuration> configurations = _batfish.loadConfigurations();
    Set<String> nodeNames = question.getNodeRegex().getMatchingNodes(_batfish);

    for (String nodeName : nodeNames) {
      Row row = new Row().put(NodePropertiesAnswerElement.COL_NODE, new Node(nodeName));
      rows.add(row);

      for (NodePropertySpecifier nodePropertySpec : question.getProperties()) {
        fillPropertyAndSchema(configurations.get(nodeName), nodePropertySpec, row, schemas);
      }
    }

    return rows;
  }

  @VisibleForTesting
  static void fillPropertyAndSchema(
      Configuration configuration,
      NodePropertySpecifier nodePropertySpec,
      Row row,
      Map<String, Schema> schemas) {
    Function<Configuration, Object> function =
        NodePropertySpecifier.JAVA_MAP.get(nodePropertySpec.toString());
    Object propertyValue = function.apply(configuration);

    // for Maps (e.g., routing policies) we use the list of keys
    if (propertyValue instanceof Map<?, ?>) {
      propertyValue =
          ((Map<?, ?>) propertyValue)
              .keySet()
              .stream()
              .map(k -> k.toString())
              .collect(Collectors.toList());
    }

    String columnName = NodePropertiesAnswerElement.getColumnNameFromPropertySpec(nodePropertySpec);

    // figure out the schema and populate the row accordingly
    if (propertyValue == null
        || (propertyValue instanceof Collection<?> && ((Collection<?>) propertyValue).isEmpty())) {
      row.put(columnName, propertyValue);
    } else if (!schemas.containsKey(columnName)) {
      // we haven't yet figured out the schema of this column
      Schema schema = Schema.fromValue(propertyValue);
      if (schema == null) {
        // force String schema when inference fails
        if (propertyValue instanceof Collection<?>) {
          schemas.put(columnName, Schema.list(Schema.STRING));
          row.put(
              columnName,
              ((Collection<?>) propertyValue)
                  .stream()
                  .map(val -> val.toString())
                  .collect(Collectors.toList()));
        } else {
          schemas.put(columnName, Schema.STRING);
          row.put(columnName, propertyValue.toString());
        }
      } else {
        schemas.put(columnName, schema);
        row.put(columnName, propertyValue);
      }
    } else {
      // the first two branches are for when we forced a String schema
      if (schemas.get(columnName).equals(Schema.STRING)) {
        row.put(columnName, propertyValue.toString());
      } else if (schemas.get(columnName).equals(Schema.list(Schema.STRING))) {
        row.put(
            columnName,
            ((Collection<?>) propertyValue)
                .stream()
                .map(val -> val.toString())
                .collect(Collectors.toList()));
      } else {
        row.put(columnName, propertyValue);
      }
    }
  }
}
