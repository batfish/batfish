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
import org.batfish.datamodel.questions.NodePropertySpecifier;
import org.batfish.datamodel.questions.NodePropertySpecifier.PropertyDescriptor;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.Row.RowBuilder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class NodePropertiesAnswererTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void convertTypeIfNeeded() {
    // map should be converted to set of strings when the schema is string list
    Object propertyValueMap =
        NodePropertiesAnswerer.convertTypeIfNeeded(
            ImmutableSortedMap.of("k1", "v1", "k2", "v2"),
            new PropertyDescriptor(null, Schema.list(Schema.STRING)));
    assertThat(propertyValueMap, equalTo(ImmutableSet.of("k1", "k2")));

    // other objects should be mapped to their names
    Object propertyValueObject =
        NodePropertiesAnswerer.convertTypeIfNeeded(
            new Configuration("cname", ConfigurationFormat.CISCO_IOS),
            new PropertyDescriptor(null, Schema.STRING));
    assertThat(propertyValueObject, equalTo("cname"));

    // null should remain null and not crash
    Object propertyValueNull =
        NodePropertiesAnswerer.convertTypeIfNeeded(
            null, new PropertyDescriptor(null, Schema.STRING));
    assertThat(propertyValueNull, equalTo(null));

    // protect other objects
    Object propertyValueOther =
        NodePropertiesAnswerer.convertTypeIfNeeded(
            new Node("node"), new PropertyDescriptor(null, Schema.NODE));
    assertThat(propertyValueOther, equalTo(new Node("node")));
  }

  @Test
  public void fillPropertyFail() {
    PropertyDescriptor propDescriptor = new PropertyDescriptor(null, Schema.list(Schema.STRING));

    _thrown.expect(BatfishException.class);
    _thrown.expectMessage("Could not recover object");

    NodePropertiesAnswerer.fillProperty("col", "stringNotList", Row.builder(), propDescriptor);
  }

  @Test
  public void fillPropertyForcedString() {
    Configuration configuration = new Configuration("hostname", ConfigurationFormat.CISCO_IOS);
    configuration.setDefaultInboundAction(LineAction.ACCEPT);
    String property = "default-inbound-action";
    RowBuilder row = Row.builder();

    NodePropertiesAnswerer.fillProperty(configuration, property, row);

    // the row should be filled out with the String value
    String columnName = NodePropertiesAnswerElement.getColumnNameFromProperty(property);
    assertThat(
        row.build(), equalTo(Row.builder().put(columnName, LineAction.ACCEPT.toString()).build()));
  }

  @Test
  public void fillPropertyListEmpty() {
    Configuration configuration = new Configuration("hostname", ConfigurationFormat.CISCO_IOS);
    String property = "ntp-servers";
    RowBuilder row = Row.builder();

    NodePropertiesAnswerer.fillProperty(configuration, property, row);

    // the row should be filled out with an empty list
    String columnName = NodePropertiesAnswerElement.getColumnNameFromProperty(property);
    assertThat(
        row.build(), equalTo(Row.builder().put(columnName, new LinkedList<String>()).build()));
  }

  @Test
  public void fillPropertyListNonEmpty() {
    Configuration configuration = new Configuration("hostname", ConfigurationFormat.CISCO_IOS);
    configuration.setNtpServers(ImmutableSortedSet.of("sa", "sb"));
    String property = "ntp-servers";
    RowBuilder row = Row.builder();

    NodePropertiesAnswerer.fillProperty(configuration, property, row);

    // the row should be filled out with the right list and the schemas map should be List<String>
    String columnName = NodePropertiesAnswerElement.getColumnNameFromProperty(property);
    assertThat(
        row.build(), equalTo(Row.builder().put(columnName, ImmutableList.of("sa", "sb")).build()));
  }

  @Test
  public void fillPropertyMap() {
    Configuration configuration = new Configuration("hostname", ConfigurationFormat.CISCO_IOS);
    configuration.setInterfaces(ImmutableSortedMap.of("i1", new Interface("i1")));
    String property = "interfaces";
    RowBuilder row = Row.builder();

    NodePropertiesAnswerer.fillProperty(configuration, property, row);

    // the row should be filled out with the right list and the schemas map should be List<String>
    String columnName = NodePropertiesAnswerElement.getColumnNameFromProperty(property);
    assertThat(row.build(), equalTo(Row.builder().put(columnName, ImmutableList.of("i1")).build()));
  }

  @Test
  public void fillPropertyNull() {
    Configuration configuration = new Configuration("hostname", ConfigurationFormat.CISCO_IOS);
    String property = "ntp-source-interface";
    RowBuilder row = Row.builder();

    NodePropertiesAnswerer.fillProperty(configuration, property, row);

    // the row should be filled out with null and the schemas shouldn't be
    String columnName = NodePropertiesAnswerElement.getColumnNameFromProperty(property);
    assertThat(row.build(), equalTo(Row.builder().put(columnName, null).build()));
  }

  @Test
  public void rawAnswer() {
    String property1 = "configuration-format";
    String property2 = "ntp-servers";
    NodePropertiesQuestion question =
        new NodePropertiesQuestion(null, new NodePropertySpecifier(property1 + "|" + property2));

    Configuration conf1 = new Configuration("node1", ConfigurationFormat.CISCO_IOS);
    Configuration conf2 = new Configuration("node2", ConfigurationFormat.HOST);
    conf2.setNtpServers(ImmutableSortedSet.of("sa"));
    Map<String, Configuration> configurations = ImmutableMap.of("node1", conf1, "node2", conf2);

    Set<String> nodes = ImmutableSet.of("node1", "node2");

    Multiset<Row> propertyRows = NodePropertiesAnswerer.rawAnswer(question, configurations, nodes);

    // we should have exactly these two rows
    String colName1 = NodePropertiesAnswerElement.getColumnNameFromProperty(property1);
    String colName2 = NodePropertiesAnswerElement.getColumnNameFromProperty(property2);
    Row row1 =
        Row.builder()
            .put(NodePropertiesAnswerElement.COL_NODE, new Node("node1"))
            .put(colName1, ConfigurationFormat.CISCO_IOS)
            .put(colName2, ImmutableList.of())
            .build();
    Row row2 =
        Row.builder()
            .put(NodePropertiesAnswerElement.COL_NODE, new Node("node2"))
            .put(colName1, ConfigurationFormat.HOST)
            .put(colName2, ImmutableList.of("sa"))
            .build();

    assertThat(propertyRows.size(), equalTo(2));
    assertThat(propertyRows, hasItems(row1, row2));
  }
}
