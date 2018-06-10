package org.batfish.question.interfaceproperties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multiset;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.questions.InterfacePropertySpecifier;
import org.batfish.datamodel.questions.InterfacesSpecifier;
import org.batfish.datamodel.table.Row;
import org.junit.Test;

public class InterfacePropertiesAnswererTest {

  @Test
  public void rawAnswer() {
    Configuration conf1 = new Configuration("node1", ConfigurationFormat.CISCO_IOS);

    Interface iface1 = new Interface("iface1", conf1);
    iface1.setDescription("desc desc desc");
    iface1.setActive(false);

    Interface iface2 = new Interface("iface2", conf1);
    iface2.setDescription("blah blah blah");

    conf1.getInterfaces().putAll(ImmutableMap.of("iface1", iface1, "iface2", iface2));

    String property1 = "description";
    String property2 = "active";

    InterfacePropertiesQuestion question =
        new InterfacePropertiesQuestion(
            new InterfacesSpecifier("iface1"),
            null,
            new InterfacePropertySpecifier(property1 + "|" + property2));

    Multiset<Row> propertyRows =
        InterfacePropertiesAnswerer.rawAnswer(
            question, ImmutableMap.of("node1", conf1), ImmutableSet.of("node1"));

    // we should have exactly one row1 with two properties; iface2 should have been filtered out
    Row expectedRow =
        Row.builder()
            .put(
                InterfacePropertiesAnswerer.COL_INTERFACE, new NodeInterfacePair("node1", "iface1"))
            .put(property2, false)
            .put(property1, "desc desc desc")
            .build();

    assertThat(propertyRows, equalTo(ImmutableMultiset.of(expectedRow)));
  }
}
