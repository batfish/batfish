package org.batfish.vendor.cisco_ftd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;

import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.DiffieHellmanGroup;
import org.batfish.datamodel.EncryptionAlgorithm;
import org.batfish.datamodel.IkeHashingAlgorithm;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpsecAuthenticationAlgorithm;
import org.batfish.datamodel.IpsecEncapsulationMode;
import org.batfish.datamodel.LineAction;
import org.batfish.vendor.cisco_ftd.representation.FtdAccessGroup;
import org.batfish.vendor.cisco_ftd.representation.FtdAccessListAddressSpecifier;
import org.batfish.vendor.cisco_ftd.representation.FtdAccessListLine;
import org.batfish.vendor.cisco_ftd.representation.FtdBgpNeighbor;
import org.batfish.vendor.cisco_ftd.representation.FtdBgpProcess;
import org.batfish.vendor.cisco_ftd.representation.FtdClassMap;
import org.batfish.vendor.cisco_ftd.representation.FtdConfiguration;
import org.batfish.vendor.cisco_ftd.representation.FtdCryptoMapEntry;
import org.batfish.vendor.cisco_ftd.representation.FtdCryptoMapSet;
import org.batfish.vendor.cisco_ftd.representation.FtdIkev2Policy;
import org.batfish.vendor.cisco_ftd.representation.FtdIpsecProfile;
import org.batfish.vendor.cisco_ftd.representation.FtdIpsecTransformSet;
import org.batfish.vendor.cisco_ftd.representation.FtdNatRule;
import org.batfish.vendor.cisco_ftd.representation.FtdNetworkObject;
import org.batfish.vendor.cisco_ftd.representation.FtdNetworkObjectGroup;
import org.batfish.vendor.cisco_ftd.representation.FtdNetworkObjectGroupMember;
import org.batfish.vendor.cisco_ftd.representation.FtdOspfNetwork;
import org.batfish.vendor.cisco_ftd.representation.FtdOspfProcess;
import org.batfish.vendor.cisco_ftd.representation.FtdPolicyMap;
import org.batfish.vendor.cisco_ftd.representation.FtdRoute;
import org.batfish.vendor.cisco_ftd.representation.FtdServiceObjectGroupMember;
import org.batfish.vendor.cisco_ftd.representation.FtdServicePolicy;
import org.batfish.vendor.cisco_ftd.representation.FtdTunnelGroup;
import org.batfish.vendor.cisco_ftd.representation.Interface;
import org.junit.Test;

/** Extra targeted coverage tests for FTD representation and conversion branches. */
public class FtdCoverageTest extends FtdGrammarTest {

  @Test
  public void testFtdConfigurationAccessorsAndMutators() {
    FtdConfiguration vc = new FtdConfiguration();
    vc.setVendor(ConfigurationFormat.CISCO_FTD);
    vc.setHostname("edge-a");

    assertThat(vc.getHostname(), equalTo("edge-a"));
    assertThat(vc.getInterfaces(), notNullValue());
    assertThat(vc.getAccessLists(), notNullValue());
    assertThat(vc.getNetworkObjects(), notNullValue());
    assertThat(vc.getNetworkObjectGroups(), notNullValue());
    assertThat(vc.getServiceObjectGroups(), notNullValue());
    assertThat(vc.getOspfProcesses(), notNullValue());
    assertThat(vc.getVrfs(), notNullValue());
    assertThat(vc.getNatRules(), hasSize(0));
    assertThat(vc.getRoutes(), hasSize(0));
    assertThat(vc.getFailoverLines(), hasSize(0));
    assertThat(vc.getAccessGroups(), hasSize(0));
    assertThat(vc.getClassMaps(), notNullValue());
    assertThat(vc.getPolicyMaps(), notNullValue());
    assertThat(vc.getServicePolicies(), hasSize(0));
    assertThat(vc.getCryptoMaps(), notNullValue());
    assertThat(vc.getCryptoMapInterfaceBindings(), notNullValue());
    assertThat(vc.getIpsecTransformSets(), notNullValue());
    assertThat(vc.getIpsecProfiles(), notNullValue());
    assertThat(vc.getIkev2Policies(), notNullValue());
    assertThat(vc.getTunnelGroups(), notNullValue());

    vc.addNatRule(new FtdNatRule("inside", "outside", FtdNatRule.NatPosition.AUTO));
    vc.getRoutes()
        .add(
            new FtdRoute(
                "inside",
                Ip.parse("10.10.0.0"),
                Ip.parse("255.255.0.0"),
                Ip.parse("192.0.2.1"),
                1));
    vc.getFailoverLines().add("failover lan unit primary");
    vc.getAccessGroups().add(new FtdAccessGroup("ACL_A", "inside", "in"));

    FtdClassMap cm = new FtdClassMap("CM_A");
    vc.addClassMap(cm);
    FtdPolicyMap pm = new FtdPolicyMap("PM_A");
    vc.addPolicyMap(pm);
    vc.addServicePolicy(new FtdServicePolicy("PM_A", FtdServicePolicy.Scope.GLOBAL, null));
    vc.addCryptoMapInterfaceBinding("CMAP_A", "outside");

    FtdBgpProcess bgp = new FtdBgpProcess(65001L);
    vc.setBgpProcess(bgp);
    assertThat(vc.getBgpProcess(), equalTo(bgp));

    vc.setNamesEnabled(true);
    vc.getNames().put("host-a", "198.51.100.10");
    vc.setArpTimeout(900);
    assertThat(vc.getNamesEnabled(), equalTo(true));
    assertThat(vc.getArpTimeout(), equalTo(900));

    assertThat(vc.getNatRules(), hasSize(1));
    assertThat(vc.getRoutes(), hasSize(1));
    assertThat(vc.getFailoverLines(), hasSize(1));
    assertThat(vc.getAccessGroups(), hasSize(1));
    assertThat(vc.getClassMaps(), hasKey("CM_A"));
    assertThat(vc.getPolicyMaps(), hasKey("PM_A"));
    assertThat(vc.getServicePolicies(), hasSize(1));
    assertThat(vc.getCryptoMapInterfaceBindings(), hasKey("CMAP_A"));
  }

  @Test
  public void testRepresentationBranches() {
    FtdAccessListAddressSpecifier any4 = FtdAccessListAddressSpecifier.any4();
    FtdAccessListAddressSpecifier any6 = FtdAccessListAddressSpecifier.any6();
    FtdAccessListAddressSpecifier obj = FtdAccessListAddressSpecifier.object("OBJ1");
    FtdAccessListAddressSpecifier objGroup = FtdAccessListAddressSpecifier.objectGroup("GRP1");

    assertThat(any4.toString(), equalTo("any4"));
    assertThat(any6.toString(), equalTo("any6"));
    assertThat(obj.toString(), equalTo("object OBJ1"));
    assertThat(objGroup.toString(), equalTo("object-group GRP1"));

    FtdServiceObjectGroupMember svcDefault = FtdServiceObjectGroupMember.serviceObject(null, null);
    FtdServiceObjectGroupMember port = FtdServiceObjectGroupMember.portObject("tcp", "eq 443");
    FtdServiceObjectGroupMember group = FtdServiceObjectGroupMember.groupObject("SVC_GROUP");

    assertThat(svcDefault.toString(), equalTo("service-object ip"));
    assertThat(port.toString(), equalTo("port-object eq 443"));
    assertThat(group.toString(), equalTo("group-object SVC_GROUP"));

    FtdNetworkObjectGroupMember hostA = FtdNetworkObjectGroupMember.host(Ip.parse("10.0.0.1"));
    FtdNetworkObjectGroupMember hostB = FtdNetworkObjectGroupMember.host(Ip.parse("10.0.0.1"));
    assertThat(hostA, equalTo(hostB));
    assertThat(hostA.hashCode(), equalTo(hostB.hashCode()));

    FtdNetworkObjectGroup groupRep = new FtdNetworkObjectGroup("NET_GROUP");
    groupRep.addMember(hostA);
    assertThat(groupRep.toString(), containsString("NET_GROUP"));

    FtdIpsecTransformSet transform = new FtdIpsecTransformSet("TS1");
    transform.setEspEncryption(EncryptionAlgorithm.AES_128_CBC);
    transform.setEspAuthentication(IpsecAuthenticationAlgorithm.HMAC_SHA1_96);
    transform.setMode(IpsecEncapsulationMode.TRANSPORT);
    assertThat(transform.toString(), containsString("TS1"));
    assertThat(transform.getMode(), equalTo(IpsecEncapsulationMode.TRANSPORT));

    FtdIpsecProfile profile = new FtdIpsecProfile("PROF1");
    profile.setIsakmpProfile("IKEV2-P1");
    profile.setPfsGroup(DiffieHellmanGroup.GROUP14);
    profile.getTransformSets().add("TS1");
    assertThat(profile.toString(), containsString("PROF1"));

    FtdClassMap classMap = new FtdClassMap("CM1");
    classMap.setType("inspect");
    classMap.addMatchLine("match default-inspection-traffic");
    classMap.addAccessListReference("ACL1");
    assertThat(classMap.getAccessListReferences(), hasSize(1));

    FtdTunnelGroup tunnelGroup = new FtdTunnelGroup("203.0.113.10");
    tunnelGroup.setType(FtdTunnelGroup.Type.IPSEC_L2L);
    tunnelGroup.setIkev2Policy("IKEV2-P1");
    tunnelGroup.setPresharedKey("key");
    assertThat(tunnelGroup.toString(), containsString("IPSEC_L2L"));
  }

  @Test
  public void testRepresentationAccessorsAndFormattingBranches() {
    FtdAccessListAddressSpecifier any = FtdAccessListAddressSpecifier.any();
    FtdAccessListAddressSpecifier host = FtdAccessListAddressSpecifier.host(Ip.parse("192.0.2.11"));
    FtdAccessListAddressSpecifier network =
        FtdAccessListAddressSpecifier.networkMask(Ip.parse("192.0.2.0"), Ip.parse("255.255.255.0"));
    FtdAccessListAddressSpecifier object = FtdAccessListAddressSpecifier.object("OBJ_A");
    FtdAccessListAddressSpecifier objectGroup = FtdAccessListAddressSpecifier.objectGroup("GRP_A");
    assertThat(any.toString(), equalTo("any"));
    assertThat(host.toString(), equalTo("host 192.0.2.11"));
    assertThat(network.toString(), equalTo("192.0.2.0 255.255.255.0"));
    assertThat(object.toString(), equalTo("object OBJ_A"));
    assertThat(objectGroup.toString(), equalTo("object-group GRP_A"));
    assertThat(host.getIp(), equalTo(Ip.parse("192.0.2.11")));
    assertThat(network.getMask(), equalTo(Ip.parse("255.255.255.0")));
    assertThat(object.getObjectName(), equalTo("OBJ_A"));
    assertThat(objectGroup.getObjectName(), equalTo("GRP_A"));
    assertThat(host.equals("not-a-specifier"), equalTo(false));

    FtdServiceObjectGroupMember svcWithPort =
        FtdServiceObjectGroupMember.serviceObject("tcp", "eq 443");
    FtdServiceObjectGroupMember svcNoPort = FtdServiceObjectGroupMember.serviceObject("udp", null);
    FtdServiceObjectGroupMember portNoSpec = FtdServiceObjectGroupMember.portObject("tcp", null);
    FtdServiceObjectGroupMember group = FtdServiceObjectGroupMember.groupObject("SVC_A");
    assertThat(
        svcWithPort.getType(), equalTo(FtdServiceObjectGroupMember.MemberType.SERVICE_OBJECT));
    assertThat(svcWithPort.getProtocol(), equalTo("tcp"));
    assertThat(svcWithPort.getPortSpec(), equalTo("eq 443"));
    assertThat(group.getObjectName(), equalTo("SVC_A"));
    assertThat(svcWithPort.toString(), equalTo("service-object tcp eq 443"));
    assertThat(svcNoPort.toString(), equalTo("service-object udp"));
    assertThat(portNoSpec.toString(), equalTo("port-object "));
    assertThat(group.equals("not-a-member"), equalTo(false));

    FtdNetworkObjectGroupMember networkMember =
        FtdNetworkObjectGroupMember.networkMask(
            Ip.parse("198.51.100.0"), Ip.parse("255.255.255.0"));
    FtdNetworkObjectGroupMember objectMember = FtdNetworkObjectGroupMember.object("OBJ_B");
    FtdNetworkObjectGroupMember groupMember = FtdNetworkObjectGroupMember.groupObject("GROUP_B");
    assertThat(
        networkMember.getType(), equalTo(FtdNetworkObjectGroupMember.MemberType.NETWORK_MASK));
    assertThat(networkMember.getIp(), equalTo(Ip.parse("198.51.100.0")));
    assertThat(networkMember.getMask(), equalTo(Ip.parse("255.255.255.0")));
    assertThat(objectMember.getObjectName(), equalTo("OBJ_B"));
    assertThat(groupMember.toString(), equalTo("group-object GROUP_B"));
    assertThat(objectMember.equals("not-a-net-member"), equalTo(false));

    FtdAccessListLine remark = FtdAccessListLine.createRemark("ACL_TEST", "allow web");
    assertThat(remark.getName(), equalTo("ACL_TEST"));
    assertThat(remark.getAclType(), equalTo(FtdAccessListLine.AclType.REMARK));
    assertThat(remark.getRemark(), equalTo("allow web"));
    assertThat(remark.toString(), equalTo("remark allow web"));

    FtdAccessListLine advanced =
        FtdAccessListLine.createAdvanced("ACL_TEST", LineAction.PERMIT, "tcp", host, network);
    advanced.setSourcePortSpecifier("eq 12345");
    advanced.setDestinationPortSpecifier("eq https");
    advanced.setInterfaceName("outside");
    advanced.setRuleId(200L);
    advanced.setTrust(true);
    advanced.setInactive(true);
    advanced.setLog(true);
    advanced.setTimeRange("WORK_HOURS");
    assertThat(advanced.getAclType(), equalTo(FtdAccessListLine.AclType.ADVANCED));
    assertThat(advanced.getSourcePortSpecifier(), equalTo("eq 12345"));
    assertThat(advanced.getDestinationPortSpecifier(), equalTo("eq https"));
    assertThat(advanced.getInterfaceName(), equalTo("outside"));
    assertThat(advanced.getRuleId(), equalTo(200L));
    assertThat(advanced.isTrust(), equalTo(true));
    assertThat(advanced.isInactive(), equalTo(true));
    assertThat(advanced.isLog(), equalTo(true));
    assertThat(advanced.getTimeRange(), equalTo("WORK_HOURS"));
    assertThat(
        advanced.toString(),
        containsString("PERMIT tcp host 192.0.2.11 -> 192.0.2.0 255.255.255.0"));

    FtdNetworkObject fqdnObject = new FtdNetworkObject("OBJ_FQDN");
    fqdnObject.setDescription("fqdn test");
    fqdnObject.setFqdn("example.com");
    assertThat(fqdnObject.getDescription(), equalTo("fqdn test"));
    assertThat(fqdnObject.getFqdn(), equalTo("example.com"));
    assertThat(fqdnObject.toIpSpace(), nullValue());
  }

  @Test
  public void testFtdConfigurationConversionVpnBgpAndMetadata() {
    FtdConfiguration vc = new FtdConfiguration();
    vc.setVendor(ConfigurationFormat.CISCO_FTD);
    vc.setHostname("ftd-edge");

    vc.setNamesEnabled(true);
    vc.getNames().put("srv1", "203.0.113.99");
    vc.setArpTimeout(1800);

    Interface inside = new Interface("GigabitEthernet0/0");
    inside.setNameif("inside");
    inside.setAddress(ConcreteInterfaceAddress.parse("10.0.0.1/24"));
    vc.getInterfaces().put(inside.getName(), inside);

    Interface outside = new Interface("GigabitEthernet0/1");
    outside.setNameif("outside");
    outside.setAddress(ConcreteInterfaceAddress.parse("198.51.100.2/24"));
    outside.setMtu(1400);
    vc.getInterfaces().put(outside.getName(), outside);

    vc.getRoutes()
        .add(
            new FtdRoute(
                outside.getName(),
                Ip.parse("172.16.0.0"),
                Ip.parse("255.240.0.0"),
                Ip.parse("198.51.100.1"),
                10));

    FtdIkev2Policy ikePolicy = new FtdIkev2Policy(10);
    ikePolicy.getEncryptionAlgorithms().add(EncryptionAlgorithm.AES_256_CBC);
    ikePolicy.getIntegrityAlgorithms().add(IkeHashingAlgorithm.SHA_256);
    ikePolicy.getDhGroups().add(DiffieHellmanGroup.GROUP14);
    ikePolicy.setLifetimeSeconds(86400);
    vc.getIkev2Policies().put(10, ikePolicy);

    FtdIpsecTransformSet transformSet = new FtdIpsecTransformSet("ESP-AES-SHA");
    transformSet.setEspEncryption(EncryptionAlgorithm.AES_128_CBC);
    transformSet.setEspAuthentication(IpsecAuthenticationAlgorithm.HMAC_SHA1_96);
    vc.getIpsecTransformSets().put(transformSet.getName(), transformSet);

    FtdIpsecProfile ipsecProfile = new FtdIpsecProfile("IPSEC-PROFILE");
    ipsecProfile.setPfsGroup(DiffieHellmanGroup.GROUP14);
    ipsecProfile.getTransformSets().add(transformSet.getName());
    vc.getIpsecProfiles().put(ipsecProfile.getName(), ipsecProfile);

    FtdCryptoMapSet mapSet = new FtdCryptoMapSet("CMAP");
    FtdCryptoMapEntry entry = new FtdCryptoMapEntry("CMAP", 10);
    entry.setPeer(Ip.parse("203.0.113.1"));
    entry.setTransforms(java.util.List.of(transformSet.getName()));
    entry.setPfsKeyGroup(DiffieHellmanGroup.GROUP14);
    mapSet.addEntry(entry);
    vc.getCryptoMaps().put(mapSet.getName(), mapSet);

    vc.addCryptoMapInterfaceBinding("CMAP", "outside");

    FtdTunnelGroup tg = new FtdTunnelGroup("203.0.113.1");
    tg.setType(FtdTunnelGroup.Type.IPSEC_L2L);
    tg.setPresharedKey("secret123");
    vc.getTunnelGroups().put(tg.getName(), tg);

    FtdBgpProcess bgp = new FtdBgpProcess(65001L);
    bgp.setRouterId(Ip.parse("10.0.0.1"));
    bgp.setHasIpv4AddressFamily(true);

    FtdBgpNeighbor active = new FtdBgpNeighbor(Ip.parse("203.0.113.2"));
    active.setRemoteAs(65002L);
    active.setIpv4UnicastActive(true);
    active.setDescription("active peer");
    bgp.getNeighbors().put(active.getIp(), active);

    FtdBgpNeighbor missingRemoteAs = new FtdBgpNeighbor(Ip.parse("203.0.113.3"));
    bgp.getNeighbors().put(missingRemoteAs.getIp(), missingRemoteAs);

    FtdBgpNeighbor inactiveIpv4 = new FtdBgpNeighbor(Ip.parse("203.0.113.4"));
    inactiveIpv4.setRemoteAs(65004L);
    inactiveIpv4.setIpv4UnicastActive(false);
    bgp.getNeighbors().put(inactiveIpv4.getIp(), inactiveIpv4);

    vc.setBgpProcess(bgp);

    FtdOspfProcess ospf = new FtdOspfProcess("1");
    ospf.setRouterId(Ip.parse("10.0.0.1"));
    ospf.getNetworks().add(new FtdOspfNetwork(Ip.parse("10.0.0.0"), Ip.parse("255.255.255.0"), 0L));
    vc.getOspfProcesses().put("1", ospf);

    FtdClassMap cm = new FtdClassMap("CM_INSPECT");
    vc.addClassMap(cm);
    FtdPolicyMap pm = new FtdPolicyMap("PM_INSPECT");
    vc.addPolicyMap(pm);
    vc.addServicePolicy(new FtdServicePolicy(pm.getName(), FtdServicePolicy.Scope.GLOBAL, null));

    Configuration c = vc.toVendorIndependentConfigurations().get(0);

    assertThat(c.getDeviceModel(), equalTo(DeviceModel.CISCO_FTD));
    assertThat(c.getHostname(), equalTo("ftd-edge"));
    assertThat(c.getAllInterfaces(), hasKey("GigabitEthernet0/0"));
    assertThat(c.getAllInterfaces(), hasKey("GigabitEthernet0/1"));
    assertThat(c.getZones(), hasKey("inside"));
    assertThat(c.getZones(), hasKey("outside"));

    assertThat(c.getAllInterfaces().get("GigabitEthernet0/1").getCryptoMap(), equalTo("CMAP"));

    assertThat(c.getIpsecPhase2Proposals(), hasKey("ESP-AES-SHA"));
    assertThat(c.getIpsecPhase2Policies(), hasKey("IPSEC-PROFILE"));
    assertThat(c.getIpsecPeerConfigs().size(), equalTo(1));
    assertThat(c.getIkePhase1Proposals().size(), greaterThan(0));
    assertThat(c.getIkePhase1Policies(), hasKey("10"));
    assertThat(c.getIkePhase1Keys(), hasKey("203.0.113.1"));

    assertThat(c.getVrfs().get(Configuration.DEFAULT_VRF_NAME).getStaticRoutes(), hasSize(0));
    assertThat(c.getVrfs().get(Configuration.DEFAULT_VRF_NAME).getBgpProcess(), notNullValue());
    assertThat(
        c.getVrfs().get(Configuration.DEFAULT_VRF_NAME).getBgpProcess().getActiveNeighbors().size(),
        equalTo(1));
    assertThat(
        c.getVrfs().get(Configuration.DEFAULT_VRF_NAME).getOspfProcesses().size(), equalTo(1));

    assertThat(c.getVendorFamily().getCisco(), notNullValue());
    assertThat(c.getVendorFamily().getCisco().getServices(), hasKey("names"));
    assertThat(c.getVendorFamily().getCisco().getServices(), hasKey("arp"));
    assertThat(c.getVendorFamily().getCisco().getServices(), hasKey("mpf"));
  }

  @Test
  public void testNamesDisabledAndNoArpMetadata() {
    FtdConfiguration vc = new FtdConfiguration();
    vc.setVendor(ConfigurationFormat.CISCO_FTD);
    vc.setHostname("ftd-min");
    vc.getNames().put("srv1", "1.1.1.1");

    Interface iface = new Interface("GigabitEthernet0/2");
    iface.setAddress(ConcreteInterfaceAddress.parse("192.0.2.2/24"));
    vc.getInterfaces().put(iface.getName(), iface);

    Configuration c = vc.toVendorIndependentConfigurations().get(0);

    if (c.getVendorFamily().getCisco() != null) {
      assertThat(c.getVendorFamily().getCisco().getServices(), not(hasKey("names")));
      assertThat(c.getVendorFamily().getCisco().getServices(), not(hasKey("arp")));
    }
    assertThat(c.getAllInterfaces(), hasKey("GigabitEthernet0/2"));
    assertThat(c.getAllInterfaces().get("GigabitEthernet0/2").getConcreteAddress(), notNullValue());
    assertThat(c.getAllInterfaces().get("GigabitEthernet0/2").getCryptoMap(), nullValue());
  }

  @Test
  public void testAccessGroupsSecurityDefaultsAndNatApplication() {
    String config =
        join(
            "interface GigabitEthernet0/0",
            " nameif inside",
            " security-level 100",
            " ip address 10.0.0.1 255.255.255.0",
            "interface GigabitEthernet0/1",
            " nameif outside",
            " security-level 0",
            " ip address 198.51.100.2 255.255.255.0",
            "interface GigabitEthernet0/2",
            " nameif dmz",
            " security-level 50",
            " ip address 172.16.0.1 255.255.255.0",
            "access-list ACL_IN extended permit ip any any",
            "access-list ACL_OUT extended permit ip any any",
            "access-list ACL_GLOBAL extended permit ip any any",
            "access-group ACL_IN in interface inside",
            "access-group ACL_OUT out interface outside",
            "access-group ACL_GLOBAL global",
            "access-group ACL_MISSING in interface inside",
            "object network REAL_1",
            " host 10.0.0.10",
            "object network MAPPED_1",
            " host 192.0.2.10",
            "object network REAL_SRC",
            " host 10.0.0.20",
            "object network MAPPED_SRC",
            " host 192.0.2.20",
            "object network REAL_DST",
            " host 203.0.113.5",
            "object network MAPPED_DST",
            " host 198.51.100.5",
            "nat (inside,outside) source static REAL_1 MAPPED_1",
            "nat (inside,outside) after-auto source static REAL_SRC MAPPED_SRC destination static"
                + " REAL_DST MAPPED_DST");

    FtdConfiguration vc = parseVendorConfig(config);
    Configuration c = vc.toVendorIndependentConfigurations().get(0);

    assertThat(
        c.getAllInterfaces().get("GigabitEthernet0/0").getIncomingFilter().getName(),
        equalTo("ACL_IN"));
    assertThat(
        c.getAllInterfaces().get("GigabitEthernet0/1").getOutgoingFilter().getName(),
        equalTo("ACL_OUT"));
    assertThat(
        c.getAllInterfaces().get("GigabitEthernet0/2").getIncomingFilter().getName(),
        startsWith("~SECURITY_LEVEL_DEFAULT~GigabitEthernet0/2~"));

    assertThat(
        c.getAllInterfaces().get("GigabitEthernet0/0").getOutgoingTransformation(), notNullValue());
    assertThat(
        c.getAllInterfaces().get("GigabitEthernet0/1").getIncomingTransformation(), notNullValue());
  }

  @Test
  public void testAclRemarkMetadataIncludedInViAclLineName() {
    String config =
        join(
            "interface GigabitEthernet0/0",
            " nameif OUTSIDE",
            " ip address 203.0.113.1 255.255.255.0",
            "access-list CSM_FW_ACL_ remark rule-id 100: PREFILTER POLICY: Prefilter-FTD",
            "access-list CSM_FW_ACL_ remark rule-id 100: RULE: ACL-OUTSIDE-IN_#1",
            "access-list CSM_FW_ACL_ advanced trust tcp ifc OUTSIDE any any eq 80 rule-id 100");

    FtdConfiguration vc = parseVendorConfig(config);
    Configuration c = vc.toVendorIndependentConfigurations().get(0);

    assertThat(c.getIpAccessLists(), hasKey("CSM_FW_ACL_"));
    String lineName = c.getIpAccessLists().get("CSM_FW_ACL_").getLines().get(0).getName();
    assertThat(lineName, containsString("rule-id 100"));
    assertThat(lineName, containsString("ACL-OUTSIDE-IN_#1"));
    assertThat(lineName, containsString("Prefilter-FTD"));
    assertThat(lineName, containsString("ifc OUTSIDE"));
    assertThat(lineName, containsString("trust tcp any -> any eq 80"));
  }
}
