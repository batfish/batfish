package org.batfish.question.nodeproperties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.hamcrest.core.IsEqual.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Multiset;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.table.Row;
import org.batfish.question.nodeproperties.NodePropertySpecifier.PropertyDescriptor;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class NodePropertiesAnswererTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void rawAnswer() {
    NodePropertySpecifier propSpec1 = new NodePropertySpecifier("configuration-format");
    NodePropertySpecifier propSpec2 = new NodePropertySpecifier("ntp-servers");
    NodePropertiesQuestion question =
        new NodePropertiesQuestion(null, ImmutableList.of(propSpec1, propSpec2));

    Configuration conf1 = new Configuration("node1", ConfigurationFormat.CISCO_IOS);
    Configuration conf2 = new Configuration("node2", ConfigurationFormat.HOST);
    conf2.setNtpServers(ImmutableSortedSet.of("sa"));
    Map<String, Configuration> configurations = ImmutableMap.of("node1", conf1, "node2", conf2);

    Set<String> nodes = ImmutableSet.of("node1", "node2");

    Multiset<Row> propertyRows = NodePropertiesAnswerer.rawAnswer(question, configurations, nodes);

    // we should have exactly these two rows
    String colName1 = NodePropertiesAnswerElement.getColumnNameFromPropertySpec(propSpec1);
    String colName2 = NodePropertiesAnswerElement.getColumnNameFromPropertySpec(propSpec2);
    Row row1 =
        new Row()
            .put(NodePropertiesAnswerElement.COL_NODE, new Node("node1"))
            .put(colName1, ConfigurationFormat.CISCO_IOS)
            .put(colName2, ImmutableList.of());
    Row row2 =
        new Row()
            .put(NodePropertiesAnswerElement.COL_NODE, new Node("node2"))
            .put(colName1, ConfigurationFormat.HOST)
            .put(colName2, ImmutableList.of("sa"));

    assertThat(propertyRows.size(), equalTo(2));
    assertThat(propertyRows, hasItems(row1, row2));
  }

  @Test
  public void fillPropertyFail() {
    PropertyDescriptor propDescriptor = new PropertyDescriptor(null, Schema.list(Schema.STRING));

    _thrown.expect(BatfishException.class);
    _thrown.expectMessage("Could not recover object");

    NodePropertiesAnswerer.fillProperty("col", "stringNotList", new Row(), propDescriptor);
  }

  @Test
  public void fillPropertyForcedString() {
    Configuration configuration = new Configuration("hostname", ConfigurationFormat.CISCO_IOS);
    configuration.setDefaultInboundAction(LineAction.ACCEPT);
    NodePropertySpecifier nodePropertySpec = new NodePropertySpecifier("default-inbound-action");
    Row row = new Row();

    NodePropertiesAnswerer.fillProperty(configuration, nodePropertySpec, row);

    // the row should be filled out with the String value
    String columnName = NodePropertiesAnswerElement.getColumnNameFromPropertySpec(nodePropertySpec);
    assertThat(row, equalTo(new Row().put(columnName, LineAction.ACCEPT.toString())));
  }

  @Test
  public void fillPropertyListEmpty() {
    Configuration configuration = new Configuration("hostname", ConfigurationFormat.CISCO_IOS);
    NodePropertySpecifier nodePropertySpec = new NodePropertySpecifier("ntp-servers");
    Row row = new Row();

    NodePropertiesAnswerer.fillProperty(configuration, nodePropertySpec, row);

    // the row should be filled out with an empty list
    String columnName = NodePropertiesAnswerElement.getColumnNameFromPropertySpec(nodePropertySpec);
    assertThat(row, equalTo(new Row().put(columnName, new LinkedList<String>())));
  }

  @Test
  public void fillPropertyListNonEmpty() {
    Configuration configuration = new Configuration("hostname", ConfigurationFormat.CISCO_IOS);
    configuration.setNtpServers(ImmutableSortedSet.of("sa", "sb"));
    NodePropertySpecifier nodePropertySpec = new NodePropertySpecifier("ntp-servers");
    Row row = new Row();

    NodePropertiesAnswerer.fillProperty(configuration, nodePropertySpec, row);

    // the row should be filled out with the right list and the schemas map should be List<String>
    String columnName = NodePropertiesAnswerElement.getColumnNameFromPropertySpec(nodePropertySpec);
    assertThat(row, equalTo(new Row().put(columnName, ImmutableList.of("sa", "sb"))));
  }

  @Test
  public void fillPropertyMap() {
    Configuration configuration = new Configuration("hostname", ConfigurationFormat.CISCO_IOS);
    configuration.setInterfaces(ImmutableSortedMap.of("i1", new Interface("i1")));
    NodePropertySpecifier nodePropertySpec = new NodePropertySpecifier("interfaces");
    Row row = new Row();

    NodePropertiesAnswerer.fillProperty(configuration, nodePropertySpec, row);

    // the row should be filled out with the right list and the schemas map should be List<String>
    String columnName = NodePropertiesAnswerElement.getColumnNameFromPropertySpec(nodePropertySpec);
    assertThat(row, equalTo(new Row().put(columnName, ImmutableList.of("i1"))));
  }

  @Test
  public void fillPropertyNull() {
    Configuration configuration = new Configuration("hostname", ConfigurationFormat.CISCO_IOS);
    NodePropertySpecifier nodePropertySpec = new NodePropertySpecifier("ntp-source-interface");
    Row row = new Row();

    NodePropertiesAnswerer.fillProperty(configuration, nodePropertySpec, row);

    // the row should be filled out with null and the schemas shouldn't be
    String columnName = NodePropertiesAnswerElement.getColumnNameFromPropertySpec(nodePropertySpec);
    assertThat(row, equalTo(new Row().put(columnName, null)));
  }
}
