package org.batfish.datamodel.questions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import java.util.LinkedList;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.TestInterface;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.PropertySpecifier.PropertyDescriptor;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.Row.RowBuilder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class PropertySpecifierTest {
  @Rule public ExpectedException _thrown = ExpectedException.none();

  /** Map should be converted to set of strings when the schema is string list */
  @Test
  public void convertTypeIfNeededMapToStringSet() {
    Object propertyValueMap =
        PropertySpecifier.convertTypeIfNeeded(
            ImmutableSortedMap.of("k1", "v1", "k2", "v2"), Schema.list(Schema.STRING));
    assertThat(propertyValueMap, equalTo(ImmutableSet.of("k1", "k2")));
  }

  /** Non-string objects should be mapped to their string forms when the schema is String */
  @Test
  public void convertTypeIfNeededNonStringToString() {
    Object propertyValueObject =
        PropertySpecifier.convertTypeIfNeeded(
            new Configuration("cname", ConfigurationFormat.CISCO_IOS), Schema.STRING);
    assertThat(propertyValueObject, equalTo("cname"));
  }

  /** Collection of non-strings should be converted to collection of strings when desired */
  @Test
  public void convertTypeIfNeededNonStringCollectionToStringCollection() {
    Object propertyValueCollection =
        PropertySpecifier.convertTypeIfNeeded(
            ImmutableList.of(new Configuration("cname", ConfigurationFormat.CISCO_IOS)),
            Schema.list(Schema.STRING));
    assertThat(propertyValueCollection, equalTo(ImmutableList.of("cname")));
  }

  /** Null should remain null and not crash */
  @Test
  public void convertTypeIfNeededNullInput() {
    Object propertyValueNull = PropertySpecifier.convertTypeIfNeeded(null, Schema.STRING);
    assertThat(propertyValueNull, equalTo(null));
  }

  /** Protect other objects for which conversion is not needed */
  @Test
  public void convertTypeIfNeededNoConversion() {
    Object propertyValueOther =
        PropertySpecifier.convertTypeIfNeeded(new Node("node"), Schema.NODE);
    assertThat(propertyValueOther, equalTo(new Node("node")));
  }

  @Test
  public void fillPropertyFail() {
    PropertyDescriptor<Configuration> propDescriptor =
        new PropertyDescriptor<>(null, Schema.list(Schema.STRING), "dummy");

    _thrown.expect(ClassCastException.class);
    _thrown.expectMessage("Cannot recover object");

    PropertySpecifier.fillProperty("col", "stringNotList", Row.builder(), propDescriptor);
  }

  @Test
  public void fillPropertyForcedString() {
    Configuration configuration = new Configuration("hostname", ConfigurationFormat.CISCO_IOS);
    configuration.setDefaultInboundAction(LineAction.PERMIT);
    String property = NodePropertySpecifier.DEFAULT_INBOUND_ACTION;
    PropertyDescriptor<Configuration> propertyDescriptor =
        NodePropertySpecifier.getPropertyDescriptor(property);
    RowBuilder row = Row.builder();

    PropertySpecifier.fillProperty(propertyDescriptor, configuration, property, row);

    // the row should be filled out with the String value
    assertThat(
        row.build(), equalTo(Row.builder().put(property, LineAction.PERMIT.toString()).build()));
  }

  @Test
  public void fillPropertyListEmpty() {
    Configuration configuration = new Configuration("hostname", ConfigurationFormat.CISCO_IOS);
    String property = NodePropertySpecifier.NTP_SERVERS;
    PropertyDescriptor<Configuration> propertyDescriptor =
        NodePropertySpecifier.getPropertyDescriptor(property);
    RowBuilder row = Row.builder();

    PropertySpecifier.fillProperty(propertyDescriptor, configuration, property, row);

    // the row should be filled out with an empty list
    assertThat(row.build(), equalTo(Row.builder().put(property, new LinkedList<String>()).build()));
  }

  @Test
  public void fillPropertyListNonEmpty() {
    Configuration configuration = new Configuration("hostname", ConfigurationFormat.CISCO_IOS);
    configuration.setNtpServers(ImmutableSortedSet.of("sa", "sb"));
    String property = NodePropertySpecifier.NTP_SERVERS;
    PropertyDescriptor<Configuration> propertyDescriptor =
        NodePropertySpecifier.getPropertyDescriptor(property);
    RowBuilder row = Row.builder();

    PropertySpecifier.fillProperty(propertyDescriptor, configuration, property, row);

    // the row should be filled out with the right list and the schemas map should be List<String>
    assertThat(
        row.build(), equalTo(Row.builder().put(property, ImmutableList.of("sa", "sb")).build()));
  }

  @Test
  public void fillPropertyMap() {
    Configuration configuration = new Configuration("hostname", ConfigurationFormat.CISCO_IOS);
    configuration.setInterfaces(
        ImmutableSortedMap.of("i1", TestInterface.builder().setName("i1").build()));
    String property = NodePropertySpecifier.INTERFACES;
    PropertyDescriptor<Configuration> propertyDescriptor =
        NodePropertySpecifier.getPropertyDescriptor(property);
    RowBuilder row = Row.builder();

    PropertySpecifier.fillProperty(propertyDescriptor, configuration, property, row);

    // the row should be filled out with the right list and the schemas map should be List<String>
    assertThat(row.build(), equalTo(Row.builder().put(property, ImmutableList.of("i1")).build()));
  }

  @Test
  public void fillPropertyNull() {
    Configuration configuration = new Configuration("hostname", ConfigurationFormat.CISCO_IOS);
    String property = NodePropertySpecifier.NTP_SOURCE_INTERFACE;
    PropertyDescriptor<Configuration> propertyDescriptor =
        NodePropertySpecifier.getPropertyDescriptor(property);
    RowBuilder row = Row.builder();

    PropertySpecifier.fillProperty(propertyDescriptor, configuration, property, row);

    // the row should be filled out with null and the schemas shouldn't be
    assertThat(row.build(), equalTo(Row.builder().put(property, null).build()));
  }

  @Test
  public void testFillPropertyMapForAllInterfaceProperties() {
    // all interface properties should be process correctly without throwing exceptions
    Configuration configuration = new Configuration("hostname", ConfigurationFormat.CISCO_IOS);
    Interface i1 = TestInterface.builder().setName("i1").build();
    configuration.setInterfaces(ImmutableSortedMap.of("i1", i1));
    InterfacePropertySpecifier.ALL
        .getMatchingProperties()
        .forEach(
            property -> {
              PropertyDescriptor<Interface> propertyDescriptor =
                  InterfacePropertySpecifier.getPropertyDescriptor(property);
              RowBuilder row = Row.builder();
              PropertySpecifier.fillProperty(propertyDescriptor, i1, property, row);
            });
  }
}
