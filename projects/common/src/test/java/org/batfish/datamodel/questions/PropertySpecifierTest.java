package org.batfish.datamodel.questions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
import org.junit.jupiter.api.Test;

class PropertySpecifierTest {

  /** Map should be converted to set of strings when the schema is string list */
  @Test
  void convertTypeIfNeededMapToStringSet() {
    Object propertyValueMap =
        PropertySpecifier.convertTypeIfNeeded(
            ImmutableSortedMap.of("k1", "v1", "k2", "v2"), Schema.list(Schema.STRING));
    assertThat(propertyValueMap, equalTo(ImmutableSet.of("k1", "k2")));
  }

  /** Non-string objects should be mapped to their string forms when the schema is String */
  @Test
  void convertTypeIfNeededNonStringToString() {
    Object propertyValueObject =
        PropertySpecifier.convertTypeIfNeeded(
            new Configuration("cname", ConfigurationFormat.CISCO_IOS), Schema.STRING);
    assertThat(propertyValueObject, equalTo("cname"));
  }

  /** Collection of non-strings should be converted to collection of strings when desired */
  @Test
  void convertTypeIfNeededNonStringCollectionToStringCollection() {
    Object propertyValueCollection =
        PropertySpecifier.convertTypeIfNeeded(
            ImmutableList.of(new Configuration("cname", ConfigurationFormat.CISCO_IOS)),
            Schema.list(Schema.STRING));
    assertThat(propertyValueCollection, equalTo(ImmutableList.of("cname")));
  }

  /** Null should remain null and not crash */
  @Test
  void convertTypeIfNeededNullInput() {
    Object propertyValueNull = PropertySpecifier.convertTypeIfNeeded(null, Schema.STRING);
    assertThat(propertyValueNull, equalTo(null));
  }

  /** Protect other objects for which conversion is not needed */
  @Test
  void convertTypeIfNeededNoConversion() {
    Object propertyValueOther =
        PropertySpecifier.convertTypeIfNeeded(new Node("node"), Schema.NODE);
    assertThat(propertyValueOther, equalTo(new Node("node")));
  }

  @Test
  void fillPropertyFail() {
    PropertyDescriptor<Configuration> propDescriptor =
        new PropertyDescriptor<>(null, Schema.list(Schema.STRING), "dummy");

    Throwable exception =
        assertThrows(
            ClassCastException.class,
            () ->
                PropertySpecifier.fillProperty(
                    "col", "stringNotList", Row.builder(), propDescriptor));
    assertThat(exception.getMessage(), containsString("Cannot recover object"));
  }

  @Test
  void fillPropertyForcedString() {
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
  void fillPropertyListEmpty() {
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
  void fillPropertyListNonEmpty() {
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
  void fillPropertyMap() {
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
  void fillPropertyNull() {
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
  void testFillPropertyMapForAllInterfaceProperties() {
    // all interface properties should be process correctly without throwing exceptions
    Configuration configuration = new Configuration("hostname", ConfigurationFormat.CISCO_IOS);
    Interface i1 = TestInterface.builder().setName("i1").build();
    configuration.setInterfaces(ImmutableSortedMap.of("i1", i1));
    InterfacePropertySpecifier.ALL
        .getMatchingProperties()
        .forEach(property -> {
          PropertyDescriptor<Interface> propertyDescriptor =
              InterfacePropertySpecifier.getPropertyDescriptor(property);
          RowBuilder row = Row.builder();
          PropertySpecifier.fillProperty(propertyDescriptor, i1, property, row);
        });
  }
}
