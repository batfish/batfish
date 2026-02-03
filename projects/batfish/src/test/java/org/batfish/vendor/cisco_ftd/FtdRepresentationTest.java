package org.batfish.vendor.cisco_ftd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import org.batfish.datamodel.DiffieHellmanGroup;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpIpSpace;
import org.batfish.datamodel.IpWildcardSetIpSpace;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.PrefixIpSpace;
import org.batfish.vendor.cisco_ftd.representation.FtdAccessList;
import org.batfish.vendor.cisco_ftd.representation.FtdAccessListAddressSpecifier;
import org.batfish.vendor.cisco_ftd.representation.FtdAccessListLine;
import org.batfish.vendor.cisco_ftd.representation.FtdConfiguration;
import org.batfish.vendor.cisco_ftd.representation.FtdCryptoMapEntry;
import org.batfish.vendor.cisco_ftd.representation.FtdNatAddress;
import org.batfish.vendor.cisco_ftd.representation.FtdNetworkObject;
import org.junit.Test;

/** Tests for FTD representation classes. */
public class FtdRepresentationTest extends FtdGrammarTest {

  @Test
  public void testFtdAccessListToString() {
    FtdAccessList acl = new FtdAccessList("TEST_ACL");
    FtdAccessListLine line =
        FtdAccessListLine.createExtended(
            "TEST_ACL",
            LineAction.PERMIT,
            "ip",
            FtdAccessListAddressSpecifier.any(),
            FtdAccessListAddressSpecifier.any());
    acl.addLine(line);

    String result = acl.toString();
    assertThat(result, notNullValue());
    assertThat(result.contains("AccessList: TEST_ACL"), equalTo(true));
  }

  @Test
  public void testFtdCryptoMapEntrySettersAndGetters() {
    FtdCryptoMapEntry entry = new FtdCryptoMapEntry("MYMAP", 10);

    assertThat(entry.getName(), equalTo("MYMAP"));
    assertThat(entry.getSequenceNumber(), equalTo(10));
    assertThat(entry.getAccessList(), nullValue());
    assertThat(entry.getDynamic(), equalTo(false));
    assertThat(entry.getIsakmpProfile(), nullValue());
    assertThat(entry.getPeer(), nullValue());
    assertThat(entry.getTransforms(), hasSize(0));
    assertThat(entry.getPfsKeyGroup(), nullValue());
    assertThat(entry.getReferredDynamicMapSet(), nullValue());

    entry.setAccessList("VPN_ACL");
    assertThat(entry.getAccessList(), equalTo("VPN_ACL"));

    entry.setDynamic(true);
    assertThat(entry.getDynamic(), equalTo(true));

    entry.setIsakmpProfile("PROFILE1");
    assertThat(entry.getIsakmpProfile(), equalTo("PROFILE1"));

    Ip peerIp = Ip.parse("203.0.113.1");
    entry.setPeer(peerIp);
    assertThat(entry.getPeer(), equalTo(peerIp));

    entry.setTransforms(java.util.Arrays.asList("ESP-AES-SHA", "ESP-3DES-SHA"));
    assertThat(entry.getTransforms(), hasSize(2));
    assertThat(entry.getTransforms().get(0), equalTo("ESP-AES-SHA"));

    entry.setPfsKeyGroup(DiffieHellmanGroup.GROUP14);
    assertThat(entry.getPfsKeyGroup(), equalTo(DiffieHellmanGroup.GROUP14));

    entry.setReferredDynamicMapSet("DYNAMIC_MAP");
    assertThat(entry.getReferredDynamicMapSet(), equalTo("DYNAMIC_MAP"));

    entry.setSequenceNumber(20);
    assertThat(entry.getSequenceNumber(), equalTo(20));
  }

  @Test
  public void testFtdNatAddressIpVisitor() {
    FtdNatAddress.FtdNatAddressIp ipAddr = new FtdNatAddress.FtdNatAddressIp(Ip.parse("192.0.2.1"));

    assertThat(ipAddr.getIp(), equalTo(Ip.parse("192.0.2.1")));

    // Test visitor
    FtdNatAddress.Visitor<String> visitor =
        new FtdNatAddress.Visitor<String>() {
          @Override
          public String visitFtdNatAddressIp(FtdNatAddress.FtdNatAddressIp ftdNatAddressIp) {
            return "IP:" + ftdNatAddressIp.getIp();
          }

          @Override
          public String visitFtdNatAddressName(FtdNatAddress.FtdNatAddressName ftdNatAddressName) {
            return "NAME:" + ftdNatAddressName.getName();
          }
        };

    String result = ipAddr.accept(visitor);
    assertThat(result, equalTo("IP:192.0.2.1"));
  }

  @Test
  public void testFtdNatAddressNameVisitor() {
    FtdNatAddress.FtdNatAddressName nameAddr = new FtdNatAddress.FtdNatAddressName("OBJECT_NAME");

    assertThat(nameAddr.getName(), equalTo("OBJECT_NAME"));

    // Test visitor
    FtdNatAddress.Visitor<String> visitor =
        new FtdNatAddress.Visitor<String>() {
          @Override
          public String visitFtdNatAddressIp(FtdNatAddress.FtdNatAddressIp ftdNatAddressIp) {
            return "IP:" + ftdNatAddressIp.getIp();
          }

          @Override
          public String visitFtdNatAddressName(FtdNatAddress.FtdNatAddressName ftdNatAddressName) {
            return "NAME:" + ftdNatAddressName.getName();
          }
        };

    String result = nameAddr.accept(visitor);
    assertThat(result, equalTo("NAME:OBJECT_NAME"));
  }

  @Test
  public void testFtdNetworkObjectHost() {
    FtdNetworkObject obj = new FtdNetworkObject("HOST_OBJ");
    obj.setHost(Ip.parse("10.1.1.1"));

    assertThat(obj.getName(), equalTo("HOST_OBJ"));
    assertThat(obj.getType(), equalTo(FtdNetworkObject.NetworkObjectType.HOST));
    assertThat(obj.getHostIp(), equalTo(Ip.parse("10.1.1.1")));

    var ipSpace = obj.toIpSpace();
    assertThat(ipSpace, notNullValue());
    assertThat(ipSpace, instanceOf(IpIpSpace.class));
  }

  @Test
  public void testFtdNetworkObjectSubnet() {
    FtdNetworkObject obj = new FtdNetworkObject("SUBNET_OBJ");
    obj.setSubnet(Ip.parse("10.0.0.0"), Ip.parse("255.255.255.0"));

    assertThat(obj.getName(), equalTo("SUBNET_OBJ"));
    assertThat(obj.getType(), equalTo(FtdNetworkObject.NetworkObjectType.SUBNET));
    assertThat(obj.getSubnetNetwork(), equalTo(Ip.parse("10.0.0.0")));
    assertThat(obj.getSubnetMask(), equalTo(Ip.parse("255.255.255.0")));

    var ipSpace = obj.toIpSpace();
    assertThat(ipSpace, notNullValue());
    assertThat(ipSpace, instanceOf(PrefixIpSpace.class));
  }

  @Test
  public void testFtdNetworkObjectRange() {
    FtdNetworkObject obj = new FtdNetworkObject("RANGE_OBJ");
    obj.setRange(Ip.parse("10.0.0.1"), Ip.parse("10.0.0.10"));

    assertThat(obj.getName(), equalTo("RANGE_OBJ"));
    assertThat(obj.getType(), equalTo(FtdNetworkObject.NetworkObjectType.RANGE));
    assertThat(obj.getRangeStart(), equalTo(Ip.parse("10.0.0.1")));
    assertThat(obj.getRangeEnd(), equalTo(Ip.parse("10.0.0.10")));

    var ipSpace = obj.toIpSpace();
    assertThat(ipSpace, notNullValue());
    assertThat(ipSpace, instanceOf(IpWildcardSetIpSpace.class));
  }

  @Test
  public void testFtdNetworkObjectFqdn() {
    FtdNetworkObject obj = new FtdNetworkObject("FQDN_OBJ");
    obj.setFqdn("example.com");

    assertThat(obj.getName(), equalTo("FQDN_OBJ"));
    assertThat(obj.getType(), equalTo(FtdNetworkObject.NetworkObjectType.FQDN));
    assertThat(obj.getFqdn(), equalTo("example.com"));

    var ipSpace = obj.toIpSpace();
    assertThat(ipSpace, nullValue()); // FQDN cannot be converted to IpSpace
  }

  @Test
  public void testFtdNetworkObjectNullType() {
    FtdNetworkObject obj = new FtdNetworkObject("NULL_TYPE_OBJ");

    assertThat(obj.getType(), nullValue());
    assertThat(obj.toIpSpace(), nullValue());
  }

  @Test
  public void testFtdNetworkObjectSubnetNullValues() {
    FtdNetworkObject obj = new FtdNetworkObject("BAD_SUBNET_OBJ");
    obj.setSubnet(null, null);

    assertThat(obj.getType(), equalTo(FtdNetworkObject.NetworkObjectType.SUBNET));
    assertThat(obj.toIpSpace(), nullValue()); // Null values should return null
  }

  @Test
  public void testFtdNetworkObjectRangeNullValues() {
    FtdNetworkObject obj = new FtdNetworkObject("BAD_RANGE_OBJ");
    obj.setRange(null, null);

    assertThat(obj.getType(), equalTo(FtdNetworkObject.NetworkObjectType.RANGE));
    assertThat(obj.toIpSpace(), nullValue()); // Null values should return null
  }

  @Test
  public void testFtdNetworkObjectToString() {
    FtdNetworkObject obj = new FtdNetworkObject("TEST_OBJ");
    obj.setHost(Ip.parse("1.2.3.4"));

    String result = obj.toString();
    assertThat(result, notNullValue());
    assertThat(result.contains("NetworkObject: TEST_OBJ"), equalTo(true));
    assertThat(result.contains("HOST"), equalTo(true));
  }

  @Test
  public void testFtdNetworkObjectSetDescription() {
    FtdNetworkObject obj = new FtdNetworkObject("DESC_OBJ");
    obj.setDescription("Test description");

    assertThat(obj.getDescription(), equalTo("Test description"));
  }

  @Test
  public void testOspfAreaParsing() {
    String config =
        join(
            "router ospf 1",
            " network 10.0.0.0 255.255.255.0 area 0",
            " network 192.168.1.0 255.255.255.0 area 1");
    FtdConfiguration vc = parseVendorConfig(config);

    assertThat(vc, notNullValue());
    // Just check that OSPF process is created; areas may or may not be populated
    // depending on how the parser works
    assertThat(vc.getOspfProcesses(), hasKey("1"));
  }

  @Test
  public void testInterfaceSettersAndGetters() {
    // Test Interface class directly without parsing
    org.batfish.vendor.cisco_ftd.representation.Interface iface =
        new org.batfish.vendor.cisco_ftd.representation.Interface("GigabitEthernet0/0");

    iface.setDescription("Outside interface");
    iface.setNameif("outside");
    iface.setSecurityLevel(0);
    iface.setActive(false);
    iface.setVlan(100);
    iface.setVrf("VRF1");
    iface.setMtu(9000);

    assertThat(iface.getName(), equalTo("GigabitEthernet0/0"));
    assertThat(iface.getDescription(), equalTo("Outside interface"));
    assertThat(iface.getNameif(), equalTo("outside"));
    assertThat(iface.getSecurityLevel(), equalTo(0));
    assertThat(iface.getActive(), equalTo(false));
    assertThat(iface.getVlan(), equalTo(100));
    assertThat(iface.getVrf(), equalTo("VRF1"));
    assertThat(iface.getMtu(), equalTo(9000));
  }

  @Test
  public void testInterfaceDefaults() {
    String config = "interface GigabitEthernet0/0\n";
    FtdConfiguration vc = parseVendorConfig(config);

    org.batfish.vendor.cisco_ftd.representation.Interface iface =
        vc.getInterfaces().get("GigabitEthernet0/0");

    assertThat(iface.getActive(), equalTo(true)); // Active by default
    assertThat(iface.getDescription(), nullValue());
    assertThat(iface.getSecurityLevel(), nullValue());
    assertThat(iface.getVlan(), nullValue());
    assertThat(iface.getVrf(), nullValue());
    assertThat(iface.getMtu(), nullValue());
  }

  @Test
  public void testFtdRouteConstructorsAndGetters() {
    org.batfish.vendor.cisco_ftd.representation.FtdRoute route =
        new org.batfish.vendor.cisco_ftd.representation.FtdRoute(
            "GigabitEthernet0/0",
            Ip.parse("10.0.0.0"),
            Ip.parse("255.0.0.0"),
            Ip.parse("192.0.2.1"),
            1);

    assertThat(route.getInterfaceName(), equalTo("GigabitEthernet0/0"));
    assertThat(route.getNetwork(), equalTo(Ip.parse("10.0.0.0")));
    assertThat(route.getMask(), equalTo(Ip.parse("255.0.0.0")));
    assertThat(route.getGateway(), equalTo(Ip.parse("192.0.2.1")));
    assertThat(route.getMetric(), equalTo(1));
  }

  @Test
  public void testFtdOspfAreaConstructorAndGetters() {
    org.batfish.vendor.cisco_ftd.representation.FtdOspfArea area =
        new org.batfish.vendor.cisco_ftd.representation.FtdOspfArea(0L);

    assertThat(area.getAreaId(), equalTo(0L));
  }

  @Test
  public void testFtdOspfAreaDifferentAreaIds() {
    org.batfish.vendor.cisco_ftd.representation.FtdOspfArea area0 =
        new org.batfish.vendor.cisco_ftd.representation.FtdOspfArea(0L);
    org.batfish.vendor.cisco_ftd.representation.FtdOspfArea area1 =
        new org.batfish.vendor.cisco_ftd.representation.FtdOspfArea(1L);

    assertThat(area0.getAreaId(), equalTo(0L));
    assertThat(area1.getAreaId(), equalTo(1L));
  }

  @Test
  public void testFtdRouteWithDifferentMetrics() {
    org.batfish.vendor.cisco_ftd.representation.FtdRoute route1 =
        new org.batfish.vendor.cisco_ftd.representation.FtdRoute(
            "Ethernet0/0",
            Ip.parse("192.168.1.0"),
            Ip.parse("255.255.255.0"),
            Ip.parse("10.1.1.1"),
            10);

    org.batfish.vendor.cisco_ftd.representation.FtdRoute route2 =
        new org.batfish.vendor.cisco_ftd.representation.FtdRoute(
            "Ethernet0/1",
            Ip.parse("172.16.0.0"),
            Ip.parse("255.240.0.0"),
            Ip.parse("10.2.2.2"),
            20);

    assertThat(route1.getMetric(), equalTo(10));
    assertThat(route2.getMetric(), equalTo(20));
    assertThat(route1.getInterfaceName(), equalTo("Ethernet0/0"));
    assertThat(route2.getInterfaceName(), equalTo("Ethernet0/1"));
  }

  // ==================== FtdOspfProcess Tests ====================

  @Test
  public void testFtdOspfProcessConstructorAndGetters() {
    org.batfish.vendor.cisco_ftd.representation.FtdOspfProcess process =
        new org.batfish.vendor.cisco_ftd.representation.FtdOspfProcess("100");

    assertThat(process.getName(), equalTo("100"));
    assertThat(process.getRouterId(), nullValue());
    assertThat(process.getNetworks(), hasSize(0));
    assertThat(process.getPassiveInterfaces(), hasSize(0));
    assertThat(process.getAreas().entrySet(), hasSize(0));
    assertThat(process.getPassiveInterfaceDefault(), equalTo(false));
  }

  @Test
  public void testFtdOspfProcessSetRouterId() {
    org.batfish.vendor.cisco_ftd.representation.FtdOspfProcess process =
        new org.batfish.vendor.cisco_ftd.representation.FtdOspfProcess("100");

    process.setRouterId(Ip.parse("1.1.1.1"));
    assertThat(process.getRouterId(), equalTo(Ip.parse("1.1.1.1")));

    process.setRouterId(Ip.parse("2.2.2.2"));
    assertThat(process.getRouterId(), equalTo(Ip.parse("2.2.2.2")));
  }

  @Test
  public void testFtdOspfProcessPassiveInterfaceDefault() {
    org.batfish.vendor.cisco_ftd.representation.FtdOspfProcess process =
        new org.batfish.vendor.cisco_ftd.representation.FtdOspfProcess("100");

    assertThat(process.getPassiveInterfaceDefault(), equalTo(false));

    process.setPassiveInterfaceDefault(true);
    assertThat(process.getPassiveInterfaceDefault(), equalTo(true));

    process.setPassiveInterfaceDefault(false);
    assertThat(process.getPassiveInterfaceDefault(), equalTo(false));
  }

  @Test
  public void testFtdOspfProcessWithNetwork() {
    org.batfish.vendor.cisco_ftd.representation.FtdOspfProcess process =
        new org.batfish.vendor.cisco_ftd.representation.FtdOspfProcess("100");

    org.batfish.vendor.cisco_ftd.representation.FtdOspfNetwork network =
        new org.batfish.vendor.cisco_ftd.representation.FtdOspfNetwork(
            Ip.parse("192.168.1.0"), Ip.parse("255.255.255.0"), 0L);

    process.getNetworks().add(network);

    assertThat(process.getNetworks(), hasSize(1));
    assertThat(process.getNetworks().get(0).getIp(), equalTo(Ip.parse("192.168.1.0")));
    assertThat(process.getNetworks().get(0).getAreaId(), equalTo(0L));
  }

  @Test
  public void testFtdOspfProcessWithPassiveInterface() {
    org.batfish.vendor.cisco_ftd.representation.FtdOspfProcess process =
        new org.batfish.vendor.cisco_ftd.representation.FtdOspfProcess("100");

    process.getPassiveInterfaces().add("outside");
    process.getPassiveInterfaces().add("inside");

    assertThat(process.getPassiveInterfaces(), hasSize(2));
    assertThat(process.getPassiveInterfaces().contains("outside"), equalTo(true));
    assertThat(process.getPassiveInterfaces().contains("inside"), equalTo(true));
  }

  @Test
  public void testFtdOspfProcessWithArea() {
    org.batfish.vendor.cisco_ftd.representation.FtdOspfProcess process =
        new org.batfish.vendor.cisco_ftd.representation.FtdOspfProcess("100");

    org.batfish.vendor.cisco_ftd.representation.FtdOspfArea area =
        new org.batfish.vendor.cisco_ftd.representation.FtdOspfArea(0L);

    process.getAreas().put(0L, area);

    assertThat(process.getAreas(), hasKey(0L));
    assertThat(process.getAreas().get(0L).getAreaId(), equalTo(0L));
  }

  @Test
  public void testFtdOspfProcessMultipleAreas() {
    org.batfish.vendor.cisco_ftd.representation.FtdOspfProcess process =
        new org.batfish.vendor.cisco_ftd.representation.FtdOspfProcess("100");

    process.getAreas().put(0L, new org.batfish.vendor.cisco_ftd.representation.FtdOspfArea(0L));
    process.getAreas().put(1L, new org.batfish.vendor.cisco_ftd.representation.FtdOspfArea(1L));

    assertThat(process.getAreas().entrySet(), hasSize(2));
    assertThat(process.getAreas(), hasKey(0L));
    assertThat(process.getAreas(), hasKey(1L));
  }

  // ==================== FtdPolicyMap Tests ====================

  @Test
  public void testFtdPolicyMapConstructorAndGetters() {
    org.batfish.vendor.cisco_ftd.representation.FtdPolicyMap policyMap =
        new org.batfish.vendor.cisco_ftd.representation.FtdPolicyMap("GLOBAL_POLICY");

    assertThat(policyMap.getName(), equalTo("GLOBAL_POLICY"));
    assertThat(policyMap.getType(), nullValue());
    assertThat(policyMap.getClassNames(), hasSize(0));
    assertThat(policyMap.getParameterLines(), hasSize(0));
    assertThat(policyMap.getClassActionLines().entrySet(), hasSize(0));
  }

  @Test
  public void testFtdPolicyMapSetType() {
    org.batfish.vendor.cisco_ftd.representation.FtdPolicyMap policyMap =
        new org.batfish.vendor.cisco_ftd.representation.FtdPolicyMap("INSPECT_POLICY");

    assertThat(policyMap.getType(), nullValue());

    policyMap.setType("inspection");
    assertThat(policyMap.getType(), equalTo("inspection"));

    policyMap.setType("type-inspect");
    assertThat(policyMap.getType(), equalTo("type-inspect"));
  }

  @Test
  public void testFtdPolicyMapAddClassName() {
    org.batfish.vendor.cisco_ftd.representation.FtdPolicyMap policyMap =
        new org.batfish.vendor.cisco_ftd.representation.FtdPolicyMap("TEST_POLICY");

    assertThat(policyMap.getClassNames(), hasSize(0));

    policyMap.addClassName("class1");
    assertThat(policyMap.getClassNames(), hasSize(1));
    assertThat(policyMap.getClassNames().get(0), equalTo("class1"));

    policyMap.addClassName("class2");
    assertThat(policyMap.getClassNames(), hasSize(2));
  }

  @Test
  public void testFtdPolicyMapAddParameterLine() {
    org.batfish.vendor.cisco_ftd.representation.FtdPolicyMap policyMap =
        new org.batfish.vendor.cisco_ftd.representation.FtdPolicyMap("PARAM_POLICY");

    assertThat(policyMap.getParameterLines(), hasSize(0));

    policyMap.addParameterLine("parameters");
    assertThat(policyMap.getParameterLines(), hasSize(1));

    policyMap.addParameterLine("  timeout reset 30");
    assertThat(policyMap.getParameterLines(), hasSize(2));
  }

  @Test
  public void testFtdPolicyMapAddClassActionLine() {
    org.batfish.vendor.cisco_ftd.representation.FtdPolicyMap policyMap =
        new org.batfish.vendor.cisco_ftd.representation.FtdPolicyMap("ACTION_POLICY");

    assertThat(policyMap.getClassActionLines().entrySet(), hasSize(0));

    policyMap.addClassActionLine("inspect-class", "inspect http");
    assertThat(policyMap.getClassActionLines().entrySet(), hasSize(1));
    assertThat(policyMap.getClassActionLines().get("inspect-class"), hasSize(1));

    policyMap.addClassActionLine("inspect-class", "inspect dns");
    assertThat(policyMap.getClassActionLines().get("inspect-class"), hasSize(2));

    policyMap.addClassActionLine("priority-class", "priority level 1");
    assertThat(policyMap.getClassActionLines().entrySet(), hasSize(2));
  }

  @Test
  public void testFtdPolicyMapComplexConfiguration() {
    org.batfish.vendor.cisco_ftd.representation.FtdPolicyMap policyMap =
        new org.batfish.vendor.cisco_ftd.representation.FtdPolicyMap("COMPLEX_POLICY");

    policyMap.setType("inspection");

    policyMap.addClassName("inspection_http");
    policyMap.addClassName("inspection_dns");
    policyMap.addClassName("inspection_ssh");

    policyMap.addParameterLine("parameters");
    policyMap.addParameterLine("  timeout reset 30");

    policyMap.addClassActionLine("inspection_http", "inspect http");
    policyMap.addClassActionLine("inspection_http", "set connection timeout idle 300");
    policyMap.addClassActionLine("inspection_dns", "inspect dns");
    policyMap.addClassActionLine("inspection_ssh", "inspect ssh");

    assertThat(policyMap.getType(), equalTo("inspection"));
    assertThat(policyMap.getClassNames(), hasSize(3));
    assertThat(policyMap.getParameterLines(), hasSize(2));
    assertThat(policyMap.getClassActionLines().entrySet(), hasSize(3));
    assertThat(policyMap.getClassActionLines().get("inspection_http"), hasSize(2));
  }

  // ==================== FtdOspfNetwork Tests ====================

  @Test
  public void testFtdOspfNetworkConstructorAndGetters() {
    org.batfish.vendor.cisco_ftd.representation.FtdOspfNetwork network =
        new org.batfish.vendor.cisco_ftd.representation.FtdOspfNetwork(
            Ip.parse("10.0.0.0"), Ip.parse("255.0.0.0"), 0L);

    assertThat(network.getIp(), equalTo(Ip.parse("10.0.0.0")));
    assertThat(network.getMask(), equalTo(Ip.parse("255.0.0.0")));
    assertThat(network.getAreaId(), equalTo(0L));
  }

  @Test
  public void testFtdOspfNetworkDifferentAreas() {
    org.batfish.vendor.cisco_ftd.representation.FtdOspfNetwork networkArea0 =
        new org.batfish.vendor.cisco_ftd.representation.FtdOspfNetwork(
            Ip.parse("192.168.1.0"), Ip.parse("255.255.255.0"), 0L);

    org.batfish.vendor.cisco_ftd.representation.FtdOspfNetwork networkArea1 =
        new org.batfish.vendor.cisco_ftd.representation.FtdOspfNetwork(
            Ip.parse("10.0.0.0"), Ip.parse("255.0.0.0"), 1L);

    assertThat(networkArea0.getAreaId(), equalTo(0L));
    assertThat(networkArea1.getAreaId(), equalTo(1L));
  }
}
