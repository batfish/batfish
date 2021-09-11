package org.batfish.question.vrrpproperties;

import static org.batfish.question.vrrpproperties.VrrpPropertiesAnswerer.COL_GROUP_ID;
import static org.batfish.question.vrrpproperties.VrrpPropertiesAnswerer.COL_INTERFACE;
import static org.batfish.question.vrrpproperties.VrrpPropertiesAnswerer.COL_PREEMPT;
import static org.batfish.question.vrrpproperties.VrrpPropertiesAnswerer.COL_PRIORITY;
import static org.batfish.question.vrrpproperties.VrrpPropertiesAnswerer.COL_VIRTUAL_ADDRESS;
import static org.batfish.question.vrrpproperties.VrrpPropertiesAnswerer.getProperties;
import static org.batfish.question.vrrpproperties.VrrpPropertiesAnswerer.populateRow;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSortedMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.VrrpGroup;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.Row.RowBuilder;
import org.batfish.specifier.MockSpecifierContext;
import org.batfish.specifier.NameNodeSpecifier;
import org.batfish.specifier.NameRegexInterfaceSpecifier;
import org.junit.Test;

public final class VrrpPropertiesAnswererTest {

  private static final Map<String, ColumnMetadata> _columnMap =
      VrrpPropertiesAnswerer.createTableMetadata(new VrrpPropertiesQuestion(null, null, false))
          .toColumnMap();

  @Test
  public void testGetProperties() {
    Configuration conf1 = new Configuration("node1", ConfigurationFormat.CISCO_IOS);

    VrrpGroup group =
        VrrpGroup.builder()
            .setVirtualAddress(ConcreteInterfaceAddress.create(Ip.parse("1.1.1.1"), 32))
            .setPriority(23)
            .setPreempt(true)
            .build();

    // active interface with VRRP group
    Interface iface1 =
        Interface.builder()
            .setName("iface1")
            .setOwner(conf1)
            .setVrrpGroups(ImmutableSortedMap.of(0, group))
            .setActive(true)
            .build();

    // inactive interface with VRRP group
    Interface iface2 =
        Interface.builder()
            .setName("iface2")
            .setOwner(conf1)
            .setVrrpGroups(ImmutableSortedMap.of(0, group))
            .setActive(false)
            .build();

    // active interface without VRRP group
    Interface iface3 =
        Interface.builder().setName("iface3").setOwner(conf1).setActive(true).build();

    conf1
        .getAllInterfaces()
        .putAll(ImmutableMap.of("iface1", iface1, "iface2", iface2, "iface3", iface3));
    MockSpecifierContext ctxt =
        MockSpecifierContext.builder().setConfigs(ImmutableMap.of("node1", conf1)).build();

    RowBuilder expectedRow1Builder =
        Row.builder().put(COL_INTERFACE, NodeInterfacePair.of("node1", "iface1"));
    populateRow(expectedRow1Builder, 0, group);
    Row expectedRow1 = expectedRow1Builder.build();

    RowBuilder expectedRow2Builder =
        Row.builder().put(COL_INTERFACE, NodeInterfacePair.of("node1", "iface2"));
    populateRow(expectedRow2Builder, 0, group);
    Row expectedRow2 = expectedRow2Builder.build();

    assertThat(
        getProperties(
            ctxt,
            new NameNodeSpecifier("node1"),
            new NameRegexInterfaceSpecifier(Pattern.compile(".*")),
            false,
            _columnMap),
        equalTo(ImmutableMultiset.of(expectedRow1, expectedRow2)));
    assertThat(
        getProperties(
            ctxt,
            new NameNodeSpecifier("node1"),
            new NameRegexInterfaceSpecifier(Pattern.compile(".*")),
            true,
            _columnMap),
        equalTo(ImmutableMultiset.of(expectedRow1)));
  }

  @Test
  public void testPopulateRow() {
    RowBuilder row = Row.builder(_columnMap);
    Ip address = Ip.parse("2.2.2.2");
    VrrpGroup group =
        VrrpGroup.builder()
            .setVirtualAddress(ConcreteInterfaceAddress.create(address, 32))
            .setPriority(23)
            .setPreempt(true)
            .build();
    VrrpPropertiesAnswerer.populateRow(row, 42, group);
    assertThat(
        row.build(),
        equalTo(
            Row.builder(_columnMap)
                .put(COL_GROUP_ID, 42)
                .put(COL_VIRTUAL_ADDRESS, address)
                .put(COL_PRIORITY, 23)
                .put(COL_PREEMPT, true)
                .build()));
  }
}
