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
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.table.Row;
import org.junit.Test;

public class NodePropertiesAnswererTest {

  @Test
  public void answer() {
    // TODO: write an "end-to-end" test here. Requires the Batfish object, and thus moving the
    // BatfishTestUtils class to common
  }

  @Test
  public void fillPropertyAndSchemaEmptyList() {
    Configuration configuration = new Configuration("hostname", ConfigurationFormat.CISCO_IOS);
    Map<String, Schema> schemas = new HashMap<>();
    NodePropertySpecifier nodePropertySpec = new NodePropertySpecifier("ntpServers");
    Row row = new Row();

    NodePropertiesAnswerer.fillPropertyAndSchema(configuration, nodePropertySpec, row, schemas);

    // the row should be filled out with an empty list and the schemas map shouldn't have anything
    assertThat(
        row,
        equalTo(
            new Row()
                .put(
                    NodePropertiesAnswerElement.getColumnNameFromPropertySpec(nodePropertySpec),
                    new LinkedList<String>())));
    assertThat(schemas.size(), equalTo(0));
  }

  @Test
  public void fillPropertyAndSchemaList() {
    Configuration configuration = new Configuration("hostname", ConfigurationFormat.CISCO_IOS);
    configuration.setNtpServers(ImmutableSortedSet.of("sa", "sb"));
    Map<String, Schema> schemas = new HashMap<>();
    NodePropertySpecifier nodePropertySpec = new NodePropertySpecifier("ntpServers");
    Row row = new Row();

    NodePropertiesAnswerer.fillPropertyAndSchema(configuration, nodePropertySpec, row, schemas);

    // the row should be filled out with the right list and the schemas map should be List<String>
    String columnName = NodePropertiesAnswerElement.getColumnNameFromPropertySpec(nodePropertySpec);
    assertThat(row, equalTo(new Row().put(columnName, ImmutableList.of("sa", "sb"))));
    assertThat(schemas, hasEntry(columnName, Schema.list(Schema.STRING)));
  }

  @Test
  public void fillPropertyAndSchemaNull() {
    Configuration configuration = new Configuration("hostname", ConfigurationFormat.CISCO_IOS);
    Map<String, Schema> schemas = new HashMap<>();
    NodePropertySpecifier nodePropertySpec = new NodePropertySpecifier("ntpSourceInterface");
    Row row = new Row();

    NodePropertiesAnswerer.fillPropertyAndSchema(configuration, nodePropertySpec, row, schemas);

    // the row should be filled out with null and the schemas shouldn't be
    String columnName = NodePropertiesAnswerElement.getColumnNameFromPropertySpec(nodePropertySpec);
    assertThat(row, equalTo(new Row().put(columnName, null)));
    assertThat(schemas.size(), equalTo(0));
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
}
