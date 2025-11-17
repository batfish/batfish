package org.batfish.question.vrrpproperties;

import static junit.framework.TestCase.assertTrue;
import static org.batfish.question.vrrpproperties.VrrpPropertiesAnswerer.COL_ACTIVE;
import static org.batfish.question.vrrpproperties.VrrpPropertiesAnswerer.COL_GROUP_ID;
import static org.batfish.question.vrrpproperties.VrrpPropertiesAnswerer.COL_INTERFACE;
import static org.batfish.question.vrrpproperties.VrrpPropertiesAnswerer.COL_PREEMPT;
import static org.batfish.question.vrrpproperties.VrrpPropertiesAnswerer.COL_PRIORITY;
import static org.batfish.question.vrrpproperties.VrrpPropertiesAnswerer.COL_SOURCE_ADDRESS;
import static org.batfish.question.vrrpproperties.VrrpPropertiesAnswerer.COL_VIRTUAL_ADDRESSES;
import static org.batfish.question.vrrpproperties.VrrpPropertiesAnswerer.populateRow;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import java.util.Map;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.IBatfishTestAdapter;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.TestInterface;
import org.batfish.datamodel.VrrpGroup;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.Row.RowBuilder;
import org.batfish.specifier.MockSpecifierContext;
import org.batfish.specifier.SpecifierContext;
import org.junit.Test;

/** Test of {@link VrrpPropertiesAnswerer}. */
public final class VrrpPropertiesAnswererTest {

  private static final Map<String, ColumnMetadata> _columnMap =
      VrrpPropertiesAnswerer.createTableMetadata(
              new VrrpPropertiesQuestion(null, null, null, false))
          .toColumnMap();

  private static RowBuilder createRowBuilder(String hostname, Interface iface) {
    return Row.builder()
        .put(COL_INTERFACE, NodeInterfacePair.of(hostname, iface.getName()))
        .put(COL_ACTIVE, iface.getActive());
  }

  private static class MockBatfish extends IBatfishTestAdapter {
    private final SpecifierContext _context;

    MockBatfish(SpecifierContext context) {
      _context = context;
    }

    @Override
    public SpecifierContext specifierContext(NetworkSnapshot snapshot) {
      return _context;
    }
  }

  @Test
  public void testAnswer() {
    Configuration conf1 = new Configuration("node1", ConfigurationFormat.CISCO_IOS);

    VrrpGroup.Builder group =
        VrrpGroup.builder()
            .setSourceAddress(ConcreteInterfaceAddress.create(Ip.parse("1.1.1.2"), 29))
            .setPriority(23)
            .setPreempt(true);

    VrrpGroup group1 = group.setVirtualAddresses("iface1", Ip.parse("1.1.1.1")).build();
    VrrpGroup group2 = group.setVirtualAddresses("iface2", Ip.parse("1.1.1.1")).build();

    // active interface with VRRP group
    Interface iface1 =
        TestInterface.builder()
            .setName("iface1")
            .setOwner(conf1)
            .setVrrpGroups(ImmutableSortedMap.of(0, group1))
            .setAdminUp(true)
            .build();

    // inactive interface with VRRP group
    Interface iface2 =
        TestInterface.builder()
            .setName("iface2")
            .setOwner(conf1)
            .setVrrpGroups(ImmutableSortedMap.of(0, group2))
            .setAdminUp(false)
            .build();

    // active interface without VRRP group
    Interface iface3 =
        TestInterface.builder().setName("iface3").setOwner(conf1).setAdminUp(true).build();

    conf1
        .getAllInterfaces()
        .putAll(ImmutableMap.of("iface1", iface1, "iface2", iface2, "iface3", iface3));
    MockSpecifierContext ctxt =
        MockSpecifierContext.builder().setConfigs(ImmutableMap.of("node1", conf1)).build();

    Row expectedRow1 = populateRow(createRowBuilder("node1", iface1), 0, group1).build();
    Row expectedRow2 = populateRow(createRowBuilder("node1", iface2), 0, group2).build();

    IBatfish mockBatfish = new MockBatfish(ctxt);

    assertThat(
        new VrrpPropertiesAnswerer(
                new VrrpPropertiesQuestion("node1", "/.*/", null, false), mockBatfish)
            .answer(null)
            .getRowsList(),
        contains(expectedRow1, expectedRow2));
    assertThat(
        new VrrpPropertiesAnswerer(
                new VrrpPropertiesQuestion("node1", "/.*/", null, true), mockBatfish)
            .answer(null)
            .getRowsList(),
        contains(expectedRow1));
  }

  @Test
  public void testAnswer_virtualAddressFiltering() {
    Configuration conf = new Configuration("node", ConfigurationFormat.CISCO_IOS);

    VrrpGroup group =
        VrrpGroup.builder()
            .setVirtualAddresses(
                "iface", ImmutableList.of(Ip.parse("1.1.1.1"), Ip.parse("2.2.2.2")))
            .setSourceAddress(ConcreteInterfaceAddress.create(Ip.parse("1.1.1.2"), 29))
            .setPriority(23)
            .setPreempt(true)
            .build();

    Interface iface =
        TestInterface.builder()
            .setName("iface")
            .setOwner(conf)
            .setVrrpGroups(ImmutableSortedMap.of(0, group))
            .setAdminUp(true)
            .build();

    conf.getAllInterfaces().putAll(ImmutableMap.of(iface.getName(), iface));
    MockSpecifierContext ctxt =
        MockSpecifierContext.builder()
            .setConfigs(ImmutableMap.of(conf.getHostname(), conf))
            .build();

    Row expectedRow = populateRow(createRowBuilder(conf.getHostname(), iface), 0, group).build();

    IBatfish mockBatfish = new MockBatfish(ctxt);

    // row is included when there is no address filtering
    assertThat(
        new VrrpPropertiesAnswerer(new VrrpPropertiesQuestion(null, null, null, false), mockBatfish)
            .answer(null)
            .getRowsList(),
        contains(expectedRow));

    // row is included when one address matches
    assertThat(
        new VrrpPropertiesAnswerer(
                new VrrpPropertiesQuestion(null, null, "1.1.1.1", false), mockBatfish)
            .answer(null)
            .getRowsList(),
        contains(expectedRow));

    // row is not included no address matches
    assertTrue(
        new VrrpPropertiesAnswerer(
                new VrrpPropertiesQuestion(null, null, "3.3.3.3", false), mockBatfish)
            .answer(null)
            .getRowsList()
            .isEmpty());
  }

  @Test
  public void testPopulateRow() {
    RowBuilder row = Row.builder(_columnMap);
    Ip address = Ip.parse("2.2.2.2");
    ConcreteInterfaceAddress sourceAddress =
        ConcreteInterfaceAddress.create(Ip.parse("2.2.2.1"), 29);
    VrrpGroup group =
        VrrpGroup.builder()
            .setVirtualAddresses("foo", address)
            .setSourceAddress(sourceAddress)
            .setPriority(23)
            .setPreempt(true)
            .build();
    VrrpPropertiesAnswerer.populateRow(row, 42, group);
    assertThat(
        row.build(),
        equalTo(
            Row.builder(_columnMap)
                .put(COL_GROUP_ID, 42)
                .put(COL_VIRTUAL_ADDRESSES, ImmutableSet.of(address))
                .put(COL_SOURCE_ADDRESS, sourceAddress)
                .put(COL_PRIORITY, 23)
                .put(COL_PREEMPT, true)
                .build()));
  }
}
