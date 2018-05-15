package org.batfish.question.nodeproperties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.core.IsEqual.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.table.Row;
import org.junit.Test;

public class NodePropertiesAnswererTest {

  @Test
  public void answer() {
    // TODO: write an "end-to-end" test here.
  }

  @Test
  public void fillPropertyAndSchemaEmptyList() {
    Configuration configuration = new Configuration("hostname", ConfigurationFormat.CISCO_IOS);
    Map<String, Schema> schemas = new HashMap<>();
    NodePropertySpecifier nodePropertySpec = new NodePropertySpecifier("ntp-servers");
    Row row = new Row();

    NodePropertiesAnswerer.fillPropertyAndSchema(configuration, nodePropertySpec, row, schemas);

    // the row should be filled out with an empty list and the schemas map shouldn't have anything
    String columnName = NodePropertiesAnswerElement.getColumnNameFromPropertySpec(nodePropertySpec);
    assertThat(row, equalTo(new Row().put(columnName, new LinkedList<String>())));
    assertThat(schemas.size(), equalTo(0));
  }

  @Test
  public void fillPropertyAndSchemaForcedString() {
    Configuration configuration = new Configuration("hostname", ConfigurationFormat.CISCO_IOS);
    configuration.setDefaultInboundAction(LineAction.ACCEPT);
    Map<String, Schema> schemas = new HashMap<>();
    NodePropertySpecifier nodePropertySpec = new NodePropertySpecifier("default-inbound-action");
    Row row = new Row();

    NodePropertiesAnswerer.fillPropertyAndSchema(configuration, nodePropertySpec, row, schemas);

    // the row should be filled out with the String value and the schemas should be appropriate
    String columnName = NodePropertiesAnswerElement.getColumnNameFromPropertySpec(nodePropertySpec);
    assertThat(row, equalTo(new Row().put(columnName, LineAction.ACCEPT.toString())));
    assertThat(schemas, hasEntry(columnName, Schema.STRING));
  }

  @Test
  public void fillPropertyAndSchemaList() {
    Configuration configuration = new Configuration("hostname", ConfigurationFormat.CISCO_IOS);
    configuration.setNtpServers(ImmutableSortedSet.of("sa", "sb"));
    Map<String, Schema> schemas = new HashMap<>();
    NodePropertySpecifier nodePropertySpec = new NodePropertySpecifier("ntp-servers");
    Row row = new Row();

    NodePropertiesAnswerer.fillPropertyAndSchema(configuration, nodePropertySpec, row, schemas);

    // the row should be filled out with the right list and the schemas map should be List<String>
    String columnName = NodePropertiesAnswerElement.getColumnNameFromPropertySpec(nodePropertySpec);
    assertThat(row, equalTo(new Row().put(columnName, ImmutableList.of("sa", "sb"))));
    assertThat(schemas, hasEntry(columnName, Schema.list(Schema.STRING)));
  }

  @Test
  public void fillPropertyAndSchemaMap() {
    Configuration configuration = new Configuration("hostname", ConfigurationFormat.CISCO_IOS);
    configuration.setInterfaces(ImmutableSortedMap.of("i1", new Interface("i1")));
    Map<String, Schema> schemas = new HashMap<>();
    NodePropertySpecifier nodePropertySpec = new NodePropertySpecifier("interfaces");
    Row row = new Row();

    NodePropertiesAnswerer.fillPropertyAndSchema(configuration, nodePropertySpec, row, schemas);

    // the row should be filled out with the right list and the schemas map should be List<String>
    String columnName = NodePropertiesAnswerElement.getColumnNameFromPropertySpec(nodePropertySpec);
    assertThat(row, equalTo(new Row().put(columnName, ImmutableList.of("i1"))));
    assertThat(schemas, hasEntry(columnName, Schema.list(Schema.STRING)));
  }

  @Test
  public void fillPropertyAndSchemaNull() {
    Configuration configuration = new Configuration("hostname", ConfigurationFormat.CISCO_IOS);
    Map<String, Schema> schemas = new HashMap<>();
    NodePropertySpecifier nodePropertySpec = new NodePropertySpecifier("ntp-source-interface");
    Row row = new Row();

    NodePropertiesAnswerer.fillPropertyAndSchema(configuration, nodePropertySpec, row, schemas);

    // the row should be filled out with null and the schemas shouldn't be
    String columnName = NodePropertiesAnswerElement.getColumnNameFromPropertySpec(nodePropertySpec);
    assertThat(row, equalTo(new Row().put(columnName, null)));
    assertThat(schemas.size(), equalTo(0));
  }

  @Test
  public void fillPropertyAndSchemaSequenceForcedString() {
    Map<String, Schema> schemas = new HashMap<>();
    NodePropertySpecifier nodePropertySpec = new NodePropertySpecifier("default-inbound-action");

    Row row1 = new Row();
    Configuration configuration1 = new Configuration("hostname1", ConfigurationFormat.CISCO_IOS);
    configuration1.setDefaultInboundAction(LineAction.ACCEPT);
    NodePropertiesAnswerer.fillPropertyAndSchema(configuration1, nodePropertySpec, row1, schemas);

    Row row2 = new Row();
    Configuration configuration2 = new Configuration("hostname2", ConfigurationFormat.CISCO_IOS);
    configuration2.setDefaultInboundAction(LineAction.REJECT);
    NodePropertiesAnswerer.fillPropertyAndSchema(configuration2, nodePropertySpec, row2, schemas);

    // the schema shouldn't be List<String> and the two rows should be good
    String columnName = NodePropertiesAnswerElement.getColumnNameFromPropertySpec(nodePropertySpec);
    assertThat(row1, equalTo(new Row().put(columnName, LineAction.ACCEPT.toString())));
    assertThat(row2, equalTo(new Row().put(columnName, LineAction.REJECT.toString())));
    assertThat(schemas, hasEntry(columnName, Schema.STRING));
  }

  @Test
  public void fillPropertyAndSchemaSequenceValueAfterEmpty() {
    Map<String, Schema> schemas = new HashMap<>();
    NodePropertySpecifier nodePropertySpec = new NodePropertySpecifier("ntp-servers");

    Row row1 = new Row();
    Configuration configuration1 = new Configuration("hostname1", ConfigurationFormat.CISCO_IOS);
    NodePropertiesAnswerer.fillPropertyAndSchema(configuration1, nodePropertySpec, row1, schemas);

    Row row2 = new Row();
    Configuration configuration2 = new Configuration("hostname2", ConfigurationFormat.CISCO_IOS);
    configuration2.setNtpServers(ImmutableSortedSet.of("sa", "sb"));
    NodePropertiesAnswerer.fillPropertyAndSchema(configuration2, nodePropertySpec, row2, schemas);

    // the schema shouldn't be List<String> and the two rows should be good
    String columnName = NodePropertiesAnswerElement.getColumnNameFromPropertySpec(nodePropertySpec);
    assertThat(row1, equalTo(new Row().put(columnName, new LinkedList<String>())));
    assertThat(row2, equalTo(new Row().put(columnName, ImmutableList.of("sa", "sb"))));
    assertThat(schemas, hasEntry(columnName, Schema.list(Schema.STRING)));
  }
}
