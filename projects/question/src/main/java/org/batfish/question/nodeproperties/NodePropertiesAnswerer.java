package org.batfish.question.nodeproperties;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.NodePropertySpecifier;
import org.batfish.datamodel.questions.NodePropertySpecifier.PropertyDescriptor;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableMetadata;

public class NodePropertiesAnswerer extends Answerer {

  public NodePropertiesAnswerer(Question question, IBatfish batfish) {
    super(question, batfish);
  }

  @Override
  public AnswerElement answer() {
    NodePropertiesQuestion question = (NodePropertiesQuestion) _question;
    Map<String, Configuration> configurations = _batfish.loadConfigurations();
    Set<String> nodes = question.getNodeRegex().getMatchingNodes(_batfish);

    TableMetadata tableMetadata = NodePropertiesAnswerElement.createMetadata(question);
    NodePropertiesAnswerElement answer = new NodePropertiesAnswerElement(tableMetadata);

    Multiset<Row> propertyRows = rawAnswer(question, configurations, nodes);

    answer.postProcessAnswer(question, propertyRows);
    return answer;
  }

  @VisibleForTesting
  static void fillProperty(
      Configuration configuration, NodePropertySpecifier nodePropertySpec, Row row) {
    PropertyDescriptor propertyDescriptor =
        NodePropertySpecifier.JAVA_MAP.get(nodePropertySpec.toString());
    Object propertyValue = propertyDescriptor.getGetter().apply(configuration);

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
    fillProperty(columnName, propertyValue, row, propertyDescriptor); // separate for testing
  }

  @VisibleForTesting
  static void fillProperty(
      String columnName, Object propertyValue, Row row, PropertyDescriptor propertyDescriptor) {
    row.put(columnName, propertyValue);
    // if this barfs, the value cannot be converted to expected Schema
    row.get(columnName, propertyDescriptor.getSchema());
  }

  @VisibleForTesting
  static Multiset<Row> rawAnswer(
      NodePropertiesQuestion question,
      Map<String, Configuration> configurations,
      Set<String> nodes) {
    Multiset<Row> rows = HashMultiset.create();

    for (String nodeName : nodes) {
      Row row = new Row().put(NodePropertiesAnswerElement.COL_NODE, new Node(nodeName));
      rows.add(row);

      for (NodePropertySpecifier nodePropertySpec : question.getProperties()) {
        fillProperty(configurations.get(nodeName), nodePropertySpec, row);
      }
    }

    return rows;
  }
}
