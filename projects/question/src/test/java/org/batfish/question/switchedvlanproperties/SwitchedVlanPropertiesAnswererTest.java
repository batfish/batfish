package org.batfish.question.switchedvlanproperties;

import static org.batfish.question.switchedvlanproperties.SwitchedVlanPropertiesAnswerer.COL_INTERFACES;
import static org.batfish.question.switchedvlanproperties.SwitchedVlanPropertiesAnswerer.COL_NODE;
import static org.batfish.question.switchedvlanproperties.SwitchedVlanPropertiesAnswerer.COL_VLAN_ID;
import static org.batfish.question.switchedvlanproperties.SwitchedVlanPropertiesAnswerer.COL_VXLAN_VNI;
import static org.batfish.question.switchedvlanproperties.SwitchedVlanPropertiesAnswerer.computeNodeVlanProperties;
import static org.batfish.question.switchedvlanproperties.SwitchedVlanPropertiesAnswerer.createRow;
import static org.batfish.question.switchedvlanproperties.SwitchedVlanPropertiesAnswerer.createTableMetadata;
import static org.batfish.question.switchedvlanproperties.SwitchedVlanPropertiesAnswerer.getProperties;
import static org.batfish.question.switchedvlanproperties.SwitchedVlanPropertiesAnswerer.populateNodeRows;
import static org.batfish.question.switchedvlanproperties.SwitchedVlanPropertiesAnswerer.tryAddInterfaceToVlan;
import static org.batfish.question.switchedvlanproperties.SwitchedVlanPropertiesAnswerer.tryAddInterfaceToVlans;
import static org.batfish.question.switchedvlanproperties.SwitchedVlanPropertiesAnswerer.tryAddVlanVni;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertThat;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.batfish.datamodel.BumTransportMethod;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.SwitchportMode;
import org.batfish.datamodel.VniSettings;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.InterfacesSpecifier;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.specifier.InterfaceSpecifier;
import org.batfish.specifier.ShorthandInterfaceSpecifier;
import org.batfish.specifier.SpecifierContext;
import org.batfish.specifier.SpecifierFactories;
import org.batfish.specifier.TestSpecifierContext;
import org.junit.Before;
import org.junit.Test;

public final class SwitchedVlanPropertiesAnswererTest {

  private static final String INTERFACE = "i1";
  private static final String NODE = "n1";

  private Map<String, Configuration> _configurations;
  private Interface.Builder _ib;
  private NetworkFactory _nf;
  private VniSettings.Builder _vnb;
  private SpecifierContext _specifierContext;

  @Before
  public void setup() {
    _nf = new NetworkFactory();
    Configuration c =
        _nf.configurationBuilder()
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setHostname(NODE)
            .build();
    Vrf v = _nf.vrfBuilder().setName(Configuration.DEFAULT_VRF_NAME).setOwner(c).build();
    _ib = _nf.interfaceBuilder().setOwner(c).setVrf(v).setName(INTERFACE).setActive(true);
    _configurations = ImmutableMap.of(c.getHostname(), c);
    _vnb = VniSettings.builder().setBumTransportMethod(BumTransportMethod.MULTICAST_GROUP);
    _specifierContext =
        new TestSpecifierContext() {
          @Override
          public Map<String, Configuration> getConfigs() {
            return _configurations;
          }
        };
  }

  @Test
  public void testComputeNodeVlanProperties() {
    int vlan = 1;
    InterfaceSpecifier interfacesSpecifier =
        SpecifierFactories.getInterfaceSpecifierOrDefault(
            null, new ShorthandInterfaceSpecifier(InterfacesSpecifier.ALL));
    boolean excludeShutInterfaces = false;
    IntegerSpace vlans = IntegerSpace.of(vlan);
    Map<Integer, ImmutableSet.Builder<NodeInterfacePair>> switchedVlanInterfaces = new HashMap<>();
    ImmutableMap.Builder<Integer, Integer> vlanVnisBuilder = ImmutableMap.builder();
    Interface iface = _ib.build();
    iface.setSwitchport(true);
    iface.setSwitchportMode(SwitchportMode.ACCESS);
    iface.setAccessVlan(vlan);
    Configuration c = _configurations.values().iterator().next();
    int vni = 10000;
    c.getDefaultVrf().getVniSettings().put(vni, _vnb.setVlan(vlan).setVni(vni).build());

    computeNodeVlanProperties(
        _specifierContext,
        c,
        interfacesSpecifier,
        excludeShutInterfaces,
        vlans,
        switchedVlanInterfaces,
        vlanVnisBuilder);

    assertThat(switchedVlanInterfaces, hasKey(vlan));
    assertThat(
        switchedVlanInterfaces.get(vlan).build(), contains(new NodeInterfacePair(NODE, INTERFACE)));
    assertThat(vlanVnisBuilder.build(), equalTo(ImmutableMap.of(vlan, vni)));
  }

  @Test
  public void testCreateRowVniAbsent() {
    Map<String, ColumnMetadata> columns =
        createTableMetadata(new SwitchedVlanPropertiesQuestion()).toColumnMap();
    int vlan = 1;
    NodeInterfacePair i = new NodeInterfacePair(NODE, INTERFACE);
    Map<Integer, ImmutableSet.Builder<NodeInterfacePair>> switchedVlanInterfaces =
        ImmutableMap.of(vlan, ImmutableSet.<NodeInterfacePair>builder().add(i));
    Map<Integer, Integer> vlanVnis = ImmutableMap.of();

    assertThat(
        createRow(columns, NODE, vlan, switchedVlanInterfaces, vlanVnis),
        equalTo(
            Row.of(
                COL_NODE,
                new Node(NODE),
                COL_VLAN_ID,
                vlan,
                COL_INTERFACES,
                ImmutableSet.of(i),
                COL_VXLAN_VNI,
                null)));
  }

  @Test
  public void testCreateRowVniPresent() {
    Map<String, ColumnMetadata> columns =
        createTableMetadata(new SwitchedVlanPropertiesQuestion()).toColumnMap();
    int vlan = 1;
    int vni = 10000;
    NodeInterfacePair i = new NodeInterfacePair(NODE, INTERFACE);
    Map<Integer, ImmutableSet.Builder<NodeInterfacePair>> switchedVlanInterfaces =
        ImmutableMap.of(vlan, ImmutableSet.<NodeInterfacePair>builder().add(i));
    Map<Integer, Integer> vlanVnis = ImmutableMap.of(vlan, vni);

    assertThat(
        createRow(columns, NODE, vlan, switchedVlanInterfaces, vlanVnis),
        equalTo(
            Row.of(
                COL_NODE,
                new Node(NODE),
                COL_VLAN_ID,
                vlan,
                COL_INTERFACES,
                ImmutableSet.of(i),
                COL_VXLAN_VNI,
                vni)));
  }

  @Test
  public void testGetProperties() {
    int vlan = 1;
    int vni = 10000;
    InterfaceSpecifier interfacesSpecifier =
        new ShorthandInterfaceSpecifier(InterfacesSpecifier.ALL);
    boolean excludeShutInterfaces = false;
    IntegerSpace vlans = IntegerSpace.of(vlan);
    Map<String, ColumnMetadata> columns =
        createTableMetadata(new SwitchedVlanPropertiesQuestion()).toColumnMap();
    Interface iface = _ib.build();
    iface.setSwitchport(true);
    iface.setSwitchportMode(SwitchportMode.ACCESS);
    iface.setAccessVlan(vlan);
    _configurations
        .get(NODE)
        .getDefaultVrf()
        .getVniSettings()
        .put(vni, _vnb.setVni(vni).setVlan(vlan).build());

    assertThat(
        getProperties(
            _specifierContext,
            _configurations,
            ImmutableSet.of(NODE),
            interfacesSpecifier,
            excludeShutInterfaces,
            vlans,
            columns),
        contains(
            Row.of(
                COL_NODE,
                new Node(NODE),
                COL_VLAN_ID,
                vlan,
                COL_INTERFACES,
                ImmutableSet.of(new NodeInterfacePair(NODE, INTERFACE)),
                COL_VXLAN_VNI,
                vni)));
  }

  @Test
  public void testPopulateNodeRows() {
    Map<String, ColumnMetadata> columns =
        createTableMetadata(new SwitchedVlanPropertiesQuestion()).toColumnMap();
    int vlan = 1;
    int vni = 10000;
    NodeInterfacePair i = new NodeInterfacePair(NODE, INTERFACE);
    Map<Integer, ImmutableSet.Builder<NodeInterfacePair>> switchedVlanInterfaces =
        ImmutableMap.of(vlan, ImmutableSet.<NodeInterfacePair>builder().add(i));
    ImmutableMap.Builder<Integer, Integer> vlanVnisBuilder =
        ImmutableMap.<Integer, Integer>builder().put(vlan, vni);
    Collection<Row> rows = HashMultiset.create();

    populateNodeRows(NODE, switchedVlanInterfaces, vlanVnisBuilder, columns, rows);

    assertThat(
        rows,
        contains(
            Row.of(
                COL_NODE,
                new Node(NODE),
                COL_VLAN_ID,
                vlan,
                COL_INTERFACES,
                ImmutableSet.of(i),
                COL_VXLAN_VNI,
                vni)));
  }

  @Test
  public void testTryAddInterfaceToVlanExcludedVlan() {
    Map<Integer, ImmutableSet.Builder<NodeInterfacePair>> switchedVlanInterfaces = new HashMap<>();
    Interface iface = _ib.build();
    int vlan = 1;
    IntegerSpace vlans = IntegerSpace.of(2);

    tryAddInterfaceToVlan(switchedVlanInterfaces, NODE, iface, vlan, vlans);

    assertThat(switchedVlanInterfaces, anEmptyMap());
  }

  @Test
  public void testTryAddInterfaceToVlanIncludedVlan() {
    Map<Integer, ImmutableSet.Builder<NodeInterfacePair>> switchedVlanInterfaces = new HashMap<>();
    Interface iface = _ib.build();
    int vlan = 1;
    IntegerSpace vlans = IntegerSpace.of(vlan);

    tryAddInterfaceToVlan(switchedVlanInterfaces, NODE, iface, vlan, vlans);

    assertThat(switchedVlanInterfaces, hasKey(vlan));
    assertThat(
        switchedVlanInterfaces.get(vlan).build(), contains(new NodeInterfacePair(NODE, INTERFACE)));
  }

  @Test
  public void testTryAddInterfaceToVlanNullVlan() {
    Map<Integer, ImmutableSet.Builder<NodeInterfacePair>> switchedVlanInterfaces = new HashMap<>();
    Interface iface = _ib.build();
    Integer vlan = null;
    IntegerSpace vlans = IntegerSpace.of(2);

    tryAddInterfaceToVlan(switchedVlanInterfaces, NODE, iface, vlan, vlans);

    assertThat(switchedVlanInterfaces, anEmptyMap());
  }

  @Test
  public void testTryAddInterfaceToVlansAccess() {
    int vlan = 1;
    InterfaceSpecifier interfacesSpecifier =
        new ShorthandInterfaceSpecifier(InterfacesSpecifier.ALL);
    boolean excludeShutInterfaces = false;
    IntegerSpace vlans = IntegerSpace.of(vlan);
    Map<Integer, ImmutableSet.Builder<NodeInterfacePair>> switchedVlanInterfaces = new HashMap<>();
    Interface iface = _ib.build();
    iface.setSwitchport(true);
    iface.setSwitchportMode(SwitchportMode.ACCESS);
    iface.setAccessVlan(vlan);

    tryAddInterfaceToVlans(
        _specifierContext,
        interfacesSpecifier,
        excludeShutInterfaces,
        vlans,
        switchedVlanInterfaces,
        NODE,
        iface);

    assertThat(switchedVlanInterfaces, hasKey(vlan));
    assertThat(
        switchedVlanInterfaces.get(vlan).build(), contains(new NodeInterfacePair(NODE, INTERFACE)));
  }

  @Test
  public void testTryAddInterfaceToVlansExcludedInterfaceByShutdown() {
    int vlan = 1;
    InterfaceSpecifier interfacesSpecifier =
        new ShorthandInterfaceSpecifier(InterfacesSpecifier.ALL);
    boolean excludeShutInterfaces = true;
    IntegerSpace vlans = IntegerSpace.of(vlan);
    Map<Integer, ImmutableSet.Builder<NodeInterfacePair>> switchedVlanInterfaces = new HashMap<>();
    Interface iface = _ib.setActive(false).build();
    iface.setSwitchport(true);
    iface.setSwitchportMode(SwitchportMode.ACCESS);
    iface.setAccessVlan(vlan);

    tryAddInterfaceToVlans(
        _specifierContext,
        interfacesSpecifier,
        excludeShutInterfaces,
        vlans,
        switchedVlanInterfaces,
        NODE,
        iface);

    assertThat(switchedVlanInterfaces, anEmptyMap());
  }

  @Test
  public void testTryAddInterfaceToVlansExcludedInterfaceBySpecifier() {
    int vlan = 1;
    InterfaceSpecifier interfacesSpecifier =
        new ShorthandInterfaceSpecifier(new InterfacesSpecifier(""));
    boolean excludeShutInterfaces = false;
    IntegerSpace vlans = IntegerSpace.of(vlan);
    Map<Integer, ImmutableSet.Builder<NodeInterfacePair>> switchedVlanInterfaces = new HashMap<>();
    Interface iface = _ib.build();
    iface.setSwitchport(true);
    iface.setSwitchportMode(SwitchportMode.ACCESS);
    iface.setAccessVlan(vlan);

    tryAddInterfaceToVlans(
        _specifierContext,
        interfacesSpecifier,
        excludeShutInterfaces,
        vlans,
        switchedVlanInterfaces,
        NODE,
        iface);

    assertThat(switchedVlanInterfaces, anEmptyMap());
  }

  @Test
  public void testTryAddInterfaceToVlansExcludedInterfaceByType() {
    int vlan = 1;
    InterfaceSpecifier interfacesSpecifier =
        new ShorthandInterfaceSpecifier(InterfacesSpecifier.ALL);
    boolean excludeShutInterfaces = true;
    IntegerSpace vlans = IntegerSpace.of(vlan);
    Map<Integer, ImmutableSet.Builder<NodeInterfacePair>> switchedVlanInterfaces = new HashMap<>();
    Interface iface = _ib.setActive(false).build();
    iface.setSwitchport(false);

    tryAddInterfaceToVlans(
        _specifierContext,
        interfacesSpecifier,
        excludeShutInterfaces,
        vlans,
        switchedVlanInterfaces,
        NODE,
        iface);

    assertThat(switchedVlanInterfaces, anEmptyMap());
  }

  @Test
  public void testTryAddInterfaceToVlansIncludeShutWithFlag() {
    int vlan = 1;
    InterfaceSpecifier interfacesSpecifier =
        new ShorthandInterfaceSpecifier(InterfacesSpecifier.ALL);
    boolean excludeShutInterfaces = false;
    IntegerSpace vlans = IntegerSpace.of(vlan);
    Map<Integer, ImmutableSet.Builder<NodeInterfacePair>> switchedVlanInterfaces = new HashMap<>();
    Interface iface = _ib.setActive(false).build();
    iface.setInterfaceType(InterfaceType.VLAN);
    iface.setSwitchport(false);
    iface.setVlan(vlan);

    tryAddInterfaceToVlans(
        _specifierContext,
        interfacesSpecifier,
        excludeShutInterfaces,
        vlans,
        switchedVlanInterfaces,
        NODE,
        iface);

    assertThat(switchedVlanInterfaces, hasKey(vlan));
    assertThat(
        switchedVlanInterfaces.get(vlan).build(), contains(new NodeInterfacePair(NODE, INTERFACE)));
  }

  @Test
  public void testTryAddInterfaceToVlansIrb() {
    int vlan = 1;
    InterfaceSpecifier interfacesSpecifier =
        new ShorthandInterfaceSpecifier(InterfacesSpecifier.ALL);
    boolean excludeShutInterfaces = false;
    IntegerSpace vlans = IntegerSpace.of(vlan);
    Map<Integer, ImmutableSet.Builder<NodeInterfacePair>> switchedVlanInterfaces = new HashMap<>();
    Interface iface = _ib.build();
    iface.setInterfaceType(InterfaceType.VLAN);
    iface.setSwitchport(false);
    iface.setVlan(vlan);

    tryAddInterfaceToVlans(
        _specifierContext,
        interfacesSpecifier,
        excludeShutInterfaces,
        vlans,
        switchedVlanInterfaces,
        NODE,
        iface);

    assertThat(switchedVlanInterfaces, hasKey(vlan));
    assertThat(
        switchedVlanInterfaces.get(vlan).build(), contains(new NodeInterfacePair(NODE, INTERFACE)));
  }

  @Test
  public void testTryAddInterfaceToVlansTrunk() {
    int vlan = 1;
    InterfaceSpecifier interfacesSpecifier =
        new ShorthandInterfaceSpecifier(InterfacesSpecifier.ALL);
    boolean excludeShutInterfaces = false;
    IntegerSpace vlans = IntegerSpace.of(Range.closed(1, 3));
    Map<Integer, ImmutableSet.Builder<NodeInterfacePair>> switchedVlanInterfaces = new HashMap<>();
    Interface iface = _ib.build();
    iface.setSwitchport(true);
    iface.setSwitchportMode(SwitchportMode.TRUNK);
    iface.setNativeVlan(vlan);
    iface.setAllowedVlans(vlans);

    tryAddInterfaceToVlans(
        _specifierContext,
        interfacesSpecifier,
        excludeShutInterfaces,
        vlans,
        switchedVlanInterfaces,
        NODE,
        iface);

    assertThat(switchedVlanInterfaces.keySet(), containsInAnyOrder(1, 2, 3));
    switchedVlanInterfaces
        .values()
        .forEach(b -> assertThat(b.build(), contains(new NodeInterfacePair(NODE, INTERFACE))));
  }

  @Test
  public void testTryAddVlanVniVlanAbsent() {
    int vni = 10000;
    int vlan = 1;
    IntegerSpace vlans = IntegerSpace.of(vlan);
    ImmutableMap.Builder<Integer, Integer> vlanVnisBuilder = ImmutableMap.builder();
    Map<Integer, ImmutableSet.Builder<NodeInterfacePair>> switchedVlanInterfaces = new HashMap<>();
    VniSettings vniSettings = _vnb.setVni(vni).build();

    tryAddVlanVni(vniSettings, vlans, vlanVnisBuilder, switchedVlanInterfaces);

    assertThat(vlanVnisBuilder.build(), anEmptyMap());
    assertThat(switchedVlanInterfaces, anEmptyMap());
  }

  @Test
  public void testTryAddVlanVniVlanPresent() {
    int vni = 10000;
    int vlan = 1;
    IntegerSpace vlans = IntegerSpace.of(vlan);
    ImmutableMap.Builder<Integer, Integer> vlanVnisBuilder = ImmutableMap.builder();
    Map<Integer, ImmutableSet.Builder<NodeInterfacePair>> switchedVlanInterfaces = new HashMap<>();
    VniSettings vniSettings = _vnb.setVni(vni).setVlan(vlan).build();

    tryAddVlanVni(vniSettings, vlans, vlanVnisBuilder, switchedVlanInterfaces);

    assertThat(vlanVnisBuilder.build(), equalTo(ImmutableMap.of(vlan, vni)));
    assertThat(switchedVlanInterfaces, hasKey(vlan));
    assertThat(switchedVlanInterfaces.get(vlan).build(), empty());
  }
}
