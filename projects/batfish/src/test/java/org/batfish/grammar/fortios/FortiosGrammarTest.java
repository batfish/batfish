package org.batfish.grammar.fortios;

import static com.google.common.collect.Iterables.getOnlyElement;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.matchers.ParseWarningMatchers.hasComment;
import static org.batfish.common.matchers.ParseWarningMatchers.hasText;
import static org.batfish.common.matchers.WarningsMatchers.hasParseWarning;
import static org.batfish.common.matchers.WarningsMatchers.hasParseWarnings;
import static org.batfish.common.util.Resources.readResource;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasHostname;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasDefinedStructure;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasNumReferrers;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasUndefinedReference;
import static org.batfish.datamodel.matchers.MapMatchers.hasKeys;
import static org.batfish.main.BatfishTestUtils.TEST_SNAPSHOT;
import static org.batfish.main.BatfishTestUtils.configureBatfishTestSettings;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import net.sf.javabdd.BDD;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Warnings;
import org.batfish.common.bdd.IpAccessListToBdd;
import org.batfish.common.bdd.IpSpaceToBDD;
import org.batfish.common.plugin.IBatfish;
import org.batfish.config.Settings;
import org.batfish.datamodel.BddTestbed;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.representation.fortios.Address;
import org.batfish.representation.fortios.FortiosConfiguration;
import org.batfish.representation.fortios.FortiosStructureType;
import org.batfish.representation.fortios.FortiosStructureUsage;
import org.batfish.representation.fortios.Interface;
import org.batfish.representation.fortios.Interface.Status;
import org.batfish.representation.fortios.Interface.Type;
import org.batfish.representation.fortios.Policy;
import org.batfish.representation.fortios.Policy.Action;
import org.batfish.representation.fortios.Service;
import org.batfish.representation.fortios.Service.Protocol;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

public final class FortiosGrammarTest {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();
  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testHostnameExtraction() {
    String filename = "fortios_hostname";
    String hostname = "my_fortios-hostname1";
    assertThat(parseVendorConfig(filename).getHostname(), equalTo(hostname));
  }

  @Test
  public void testHostnameConversion() throws IOException {
    String filename = "fortios_hostname";
    String hostname = "my_fortios-hostname1";
    assertThat(parseTextConfigs(filename), hasEntry(equalTo(hostname), hasHostname(hostname)));
  }

  @Test
  public void testInvalidHostnameWithDotExtraction() {
    String filename = "fortios_bad_hostname";
    // invalid hostname from config file is thrown away
    assertThat(parseVendorConfig(filename).getHostname(), nullValue());
  }

  @Test
  public void testInvalidHostnameWithDotConversion() throws IOException {
    String filename = "fortios_bad_hostname";
    Batfish batfish = getBatfishForConfigurationNames(filename);
    Warnings warnings =
        getOnlyElement(
            batfish
                .loadParseVendorConfigurationAnswerElement(batfish.getSnapshot())
                .getWarnings()
                .values());
    assertThat(warnings, hasParseWarning(hasComment("Illegal value for device hostname")));
  }

  @Test
  public void testReplacemsgExtraction() {
    String hostname = "fortios_replacemsg";
    String majorType = "admin";
    String minorTypePre = "pre_admin-disclaimer-text";
    String minorTypePost = "post_admin-disclaimer-text";
    FortiosConfiguration vc = parseVendorConfig(hostname);
    assertThat(
        vc.getReplacemsgs(), hasEntry(equalTo(majorType), hasKeys(minorTypePre, minorTypePost)));
    assertThat(
        vc.getReplacemsgs().get(majorType).get(minorTypePre).getBuffer(),
        equalTo("\"npre\"''\\\\nabc\\\\\\\" \"\nlastline"));
    assertThat(vc.getReplacemsgs().get(majorType).get(minorTypePost).getBuffer(), nullValue());
  }

  @Test
  public void testReplacemsgConversion() throws IOException {
    String filename = "fortios_replacemsg";
    Batfish batfish = getBatfishForConfigurationNames(filename);
    // Should see a single conversion warning for Ethernet1/1's conflicting speeds
    Warnings warnings =
        getOnlyElement(
            batfish
                .loadParseVendorConfigurationAnswerElement(batfish.getSnapshot())
                .getWarnings()
                .values());
    assertThat(warnings, hasParseWarning(hasComment("Illegal value for replacemsg minor type")));
  }

  @Test
  public void testAddressExtraction() {
    String hostname = "address";
    FortiosConfiguration vc = parseVendorConfig(hostname);

    Map<String, Address> addresses = vc.getAddresses();
    assertThat(
        addresses,
        hasKeys(
            "ipmask",
            "iprange",
            "fqdn",
            "dynamic",
            "geography",
            "interface-subnet",
            "mac",
            "wildcard",
            "undefined-refs",
            "abcdefghijklmnopqrstuvwxyz abcdefghijklmnopqrstuvwxyz abcdefghijklmnopqrstuvwxy"));

    Address ipmask = addresses.get("ipmask");
    Address iprange = addresses.get("iprange");
    Address fqdn = addresses.get("fqdn");
    Address dynamic = addresses.get("dynamic");
    Address geography = addresses.get("geography");
    Address interfaceSubnet = addresses.get("interface-subnet");
    Address undefinedRefs = addresses.get("undefined-refs");
    Address mac = addresses.get("mac");
    Address wildcard = addresses.get("wildcard");
    Address longName =
        addresses.get(
            "abcdefghijklmnopqrstuvwxyz abcdefghijklmnopqrstuvwxyz abcdefghijklmnopqrstuvwxy");

    // Test types
    assertThat(ipmask.getType(), equalTo(Address.Type.IPMASK));
    assertThat(iprange.getType(), equalTo(Address.Type.IPRANGE));
    assertThat(interfaceSubnet.getType(), equalTo(Address.Type.INTERFACE_SUBNET));
    assertThat(wildcard.getType(), equalTo(Address.Type.WILDCARD));
    assertNull(longName.getType());
    assertThat(undefinedRefs.getType(), equalTo(Address.Type.INTERFACE_SUBNET));
    assertThat(dynamic.getType(), equalTo(Address.Type.DYNAMIC));
    assertThat(fqdn.getType(), equalTo(Address.Type.FQDN));
    assertThat(geography.getType(), equalTo(Address.Type.GEOGRAPHY));
    assertThat(mac.getType(), equalTo(Address.Type.MAC));

    // Test that type-specific fields are populated correctly
    assertThat(ipmask.getTypeSpecificFields().getSubnet(), equalTo(Prefix.parse("1.1.1.0/24")));
    assertThat(iprange.getTypeSpecificFields().getStartIp(), equalTo(Ip.parse("1.1.1.0")));
    assertThat(iprange.getTypeSpecificFields().getEndIp(), equalTo(Ip.parse("1.1.1.255")));
    assertThat(interfaceSubnet.getTypeSpecificFields().getInterface(), equalTo("port1"));
    assertThat(
        wildcard.getTypeSpecificFields().getWildcard(),
        equalTo(IpWildcard.ipWithWildcardMask(Ip.parse("2.0.0.2"), Ip.parse("255.0.0.255"))));
    assertThat(longName.getTypeSpecificFields().getSubnet(), equalTo(Prefix.parse("1.1.1.0/24")));

    // Test explicitly set values
    assertThat(ipmask.getAllowRouting(), equalTo(true));
    assertThat(ipmask.getAssociatedInterface(), equalTo("port1"));
    assertThat(ipmask.getComment(), equalTo("Hello world"));
    assertThat(ipmask.getFabricObject(), equalTo(true));

    // Test default values
    assertThat(longName.getTypeEffective(), equalTo(Address.Type.IPMASK));
    assertNull(longName.getAllowRouting());
    assertFalse(longName.getAllowRoutingEffective());
    assertNull(longName.getAssociatedInterface());
    assertNull(longName.getComment());
    assertNull(longName.getFabricObject());
    assertFalse(longName.getFabricObjectEffective());

    // Test that undefined structures are not added to Address object
    // TODO Also check that undefined references are filed (once they are filed)
    assertNull(undefinedRefs.getAssociatedInterface());
    assertNull(undefinedRefs.getTypeSpecificFields().getInterface());
  }

  @Test
  public void testAddressWarnings() throws IOException {
    String hostname = "address_warnings";
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    Warnings w =
        getOnlyElement(
            batfish
                .loadParseVendorConfigurationAnswerElement(batfish.getSnapshot())
                .getWarnings()
                .values());

    List<Matcher<Warnings.ParseWarning>> warningMatchers = new ArrayList<>();

    // Expect warnings for each illegal name used in the config
    Map<String, String> illegalNames =
        ImmutableMap.of(
            "address name",
            "abcdefghijklmnopqrstuvwxyz abcdefghijklmnopqrstuvwxyz abcdefghijklmnopqrstuvwxyz",
            "zone or interface name",
            "abcdefghijklmnopqrstuvwxyz abcdefghi",
            "interface name",
            "abcdefghijklmnop");
    illegalNames.forEach(
        (nameType, name) ->
            warningMatchers.add(
                allOf(hasComment("Illegal value for " + nameType), hasText(containsString(name)))));

    // Expect warnings for each undefined reference in the config (in an otherwise legal context)
    warningMatchers.add(hasComment("No interface or zone named undefined_iface"));
    warningMatchers.add(hasComment("No interface named undefined_iface"));

    // Warn on all type-specific fields set for inappropriate types
    for (String f : ImmutableList.of("start-ip", "end-ip", "interface", "wildcard")) {
      warningMatchers.add(
          hasComment(String.format("Cannot set %s for address type %s", f, Address.Type.IPMASK)));
    }
    for (String f : ImmutableList.of("interface", "subnet", "wildcard")) {
      warningMatchers.add(
          hasComment(String.format("Cannot set %s for address type %s", f, Address.Type.IPRANGE)));
    }
    for (String f : ImmutableList.of("start-ip", "end-ip", "wildcard")) {
      warningMatchers.add(
          hasComment(
              String.format(
                  "Cannot set %s for address type %s", f, Address.Type.INTERFACE_SUBNET)));
    }
    for (String f : ImmutableList.of("start-ip", "end-ip", "interface", "subnet")) {
      warningMatchers.add(
          hasComment(String.format("Cannot set %s for address type %s", f, Address.Type.WILDCARD)));
    }
    for (String f : ImmutableList.of("start-ip", "end-ip", "interface", "subnet", "wildcard")) {
      // None of the type-specific fields are settable for any of the unsupported types
      for (Address.Type type :
          ImmutableList.of(
              Address.Type.DYNAMIC, Address.Type.FQDN, Address.Type.GEOGRAPHY, Address.Type.MAC)) {
        warningMatchers.add(
            hasComment(String.format("Cannot set %s for address type %s", f, type)));
      }
    }
    assertThat(w.getParseWarnings(), hasSize(warningMatchers.size()));
    warningMatchers.forEach(matcher -> assertThat(w, hasParseWarning(matcher)));

    // TODO None of the defined addresses are valid, so none should make it into VS.
    // Once this is correctly implemented, test that no addresses are in the VS config.
  }

  @Test
  public void testAddressConversion() throws IOException {
    String hostname = "address";
    Configuration c = parseConfig(hostname);

    Map<String, IpSpace> ipSpaces = c.getIpSpaces();
    String longName =
        "abcdefghijklmnopqrstuvwxyz abcdefghijklmnopqrstuvwxyz abcdefghijklmnopqrstuvwxy";
    assertThat(
        ipSpaces,
        hasKeys(
            "ipmask",
            "iprange",
            "fqdn",
            "dynamic",
            "geography",
            "interface-subnet",
            "mac",
            "wildcard",
            // TODO undefined-refs shouldn't be converted
            "undefined-refs",
            longName));

    BDD prefix1110 = Prefix.parse("1.1.1.0/24").toIpSpace().accept(SRC_IP_BDD);
    assertThat(ipSpaces.get("ipmask").accept(SRC_IP_BDD), equalTo(prefix1110));
    assertThat(ipSpaces.get("iprange").accept(SRC_IP_BDD), equalTo(prefix1110));
    assertThat(ipSpaces.get(longName).accept(SRC_IP_BDD), equalTo(prefix1110));
    assertThat(
        ipSpaces.get("wildcard").accept(SRC_IP_BDD),
        equalTo(
            IpWildcard.ipWithWildcardMask(Ip.parse("2.0.0.2"), Ip.parse("255.0.0.255"))
                .toIpSpace()
                .accept(SRC_IP_BDD)));

    // Unsupported types
    Stream.of("fqdn", "dynamic", "geography", "interface-subnet", "mac", "undefined-refs")
        .forEach(t -> assertThat(ipSpaces.get(t).accept(SRC_IP_BDD), equalTo(ZERO)));
  }

  @Test
  public void testInterfaceExtraction() {
    String hostname = "iface";
    FortiosConfiguration vc = parseVendorConfig(hostname);

    Map<String, Interface> ifaces = vc.getInterfaces();
    assertThat(
        ifaces.keySet(),
        containsInAnyOrder(
            "port1",
            "port2",
            "longest if name",
            "tunnel",
            "loopback123",
            "agg",
            "emac",
            "redundant",
            "vlan",
            "wl"));

    Interface port1 = ifaces.get("port1");
    Interface port2 = ifaces.get("port2");
    Interface longName = ifaces.get("longest if name");
    Interface tunnel = ifaces.get("tunnel");
    Interface loopback = ifaces.get("loopback123");
    Interface agg = ifaces.get("agg");
    Interface emac = ifaces.get("emac");
    Interface redundant = ifaces.get("redundant");
    Interface vlan = ifaces.get("vlan");
    Interface wl = ifaces.get("wl");

    assertThat(port1.getVdom(), equalTo("root"));
    assertThat(port1.getIp(), equalTo(ConcreteInterfaceAddress.parse("192.168.122.2/24")));
    assertThat(port1.getType(), equalTo(Type.PHYSICAL));
    assertThat(port1.getAlias(), equalTo("longest possibl alias str"));
    assertThat(port1.getDescription(), equalTo("quoted description w/ spaces and more"));
    // Check defaults
    assertThat(port1.getStatus(), equalTo(Status.UNKNOWN));
    assertTrue(port1.getStatusEffective());
    assertThat(port1.getMtu(), nullValue());
    assertThat(port1.getMtuEffective(), equalTo(Interface.DEFAULT_INTERFACE_MTU));
    assertThat(port1.getMtuOverride(), nullValue());
    assertThat(port1.getVrf(), nullValue());
    assertThat(port1.getVrfEffective(), equalTo(0));

    assertThat(port2.getVdom(), equalTo("root"));
    assertThat(port2.getAlias(), equalTo("no_spaces"));
    assertThat(port2.getDescription(), equalTo("no_spaces_descr"));
    // Check overriding defaults
    assertThat(port2.getMtuOverride(), equalTo(true));
    assertThat(port2.getMtu(), equalTo(1234));
    assertThat(port2.getMtuEffective(), equalTo(1234));

    assertThat(longName.getIp(), equalTo(ConcreteInterfaceAddress.parse("169.254.1.1/24")));
    assertThat(longName.getAlias(), equalTo(""));
    // Check overriding defaults
    assertTrue(longName.getStatusEffective());
    assertThat(longName.getStatus(), equalTo(Status.UP));
    assertThat(longName.getVrf(), equalTo(31));
    assertThat(longName.getVrfEffective(), equalTo(31));

    assertThat(tunnel.getStatus(), equalTo(Status.DOWN));
    assertFalse(tunnel.getStatusEffective());
    assertThat(tunnel.getType(), equalTo(Type.TUNNEL));
    // MTU is set, but not used since override isn't set
    assertThat(tunnel.getMtuOverride(), nullValue());
    assertThat(tunnel.getMtu(), equalTo(65535));
    assertThat(tunnel.getMtuEffective(), equalTo(Interface.DEFAULT_INTERFACE_MTU));

    assertThat(loopback.getType(), equalTo(Type.LOOPBACK));
    assertThat(agg.getType(), equalTo(Type.AGGREGATE));
    assertThat(emac.getType(), equalTo(Type.EMAC_VLAN));
    assertThat(redundant.getType(), equalTo(Type.REDUNDANT));
    assertThat(vlan.getType(), equalTo(Type.VLAN));
    assertThat(wl.getType(), equalTo(Type.WL_MESH));
  }

  @Test
  public void testServiceCustomExtraction() {
    String hostname = "service_custom";
    FortiosConfiguration vc = parseVendorConfig(hostname);

    Map<String, Service> services = vc.getServices();
    assertThat(
        services.keySet(),
        containsInAnyOrder(
            "longest possible firewall service custom service name that is accepted by devic",
            "custom_default",
            "explicit_tcp",
            "src_port_defaults",
            "custom_icmp",
            "custom_icmp6",
            "custom_ip",
            "change_protocol"));

    Service serviceLongName =
        services.get(
            "longest possible firewall service custom service name that is accepted by devic");
    Service serviceDefault = services.get("custom_default");
    Service serviceTcp = services.get("explicit_tcp");
    Service serviceSrcPortDefaults = services.get("src_port_defaults");
    Service serviceIcmp = services.get("custom_icmp");
    Service serviceIcmp6 = services.get("custom_icmp6");
    Service serviceIp = services.get("custom_ip");
    Service serviceChangeProtocol = services.get("change_protocol");

    assertThat(serviceLongName.getComment(), equalTo("service custom comment"));

    // Check default protocol
    assertThat(serviceDefault.getProtocol(), nullValue());
    assertThat(serviceDefault.getProtocolEffective(), equalTo(Service.DEFAULT_PROTOCOL));
    // Check defaults
    assertThat(serviceDefault.getSctpPortRangeDst(), nullValue());
    assertThat(serviceDefault.getSctpPortRangeSrc(), nullValue());
    assertThat(serviceDefault.getTcpPortRangeDst(), nullValue());
    assertThat(serviceDefault.getTcpPortRangeSrc(), nullValue());
    assertThat(serviceDefault.getUdpPortRangeDst(), nullValue());
    assertThat(serviceDefault.getUdpPortRangeSrc(), nullValue());

    // Even with dest ports configured, source ports should still show up as default
    assertThat(serviceSrcPortDefaults.getSctpPortRangeSrc(), nullValue());
    assertThat(
        serviceSrcPortDefaults.getSctpPortRangeSrcEffective(),
        equalTo(Service.DEFAULT_SOURCE_PORT_RANGE));
    assertThat(serviceSrcPortDefaults.getTcpPortRangeSrc(), nullValue());
    assertThat(
        serviceSrcPortDefaults.getTcpPortRangeSrcEffective(),
        equalTo(Service.DEFAULT_SOURCE_PORT_RANGE));
    assertThat(serviceSrcPortDefaults.getUdpPortRangeSrc(), nullValue());
    assertThat(
        serviceSrcPortDefaults.getUdpPortRangeSrcEffective(),
        equalTo(Service.DEFAULT_SOURCE_PORT_RANGE));

    assertThat(serviceTcp.getProtocol(), equalTo(Protocol.TCP_UDP_SCTP));
    assertThat(serviceTcp.getProtocolEffective(), equalTo(Protocol.TCP_UDP_SCTP));
    // Check variety of port range syntax
    // TCP
    assertThat(
        serviceTcp.getTcpPortRangeDst(),
        equalTo(IntegerSpace.builder().including(1, 2, 10, 11, 13).build()));
    assertThat(
        serviceTcp.getTcpPortRangeSrc(),
        equalTo(IntegerSpace.builder().including(3, 4, 6, 7).build()));
    // UDP
    assertThat(
        serviceTcp.getUdpPortRangeDst(), equalTo(IntegerSpace.builder().including(100).build()));
    assertThat(serviceTcp.getUdpPortRangeSrc(), nullValue());
    // SCTP
    assertThat(
        serviceTcp.getSctpPortRangeDst(),
        equalTo(IntegerSpace.builder().including(200, 201).build()));
    assertThat(
        serviceTcp.getSctpPortRangeSrc(), equalTo(IntegerSpace.builder().including(300).build()));

    assertThat(serviceIcmp.getProtocol(), equalTo(Protocol.ICMP));
    assertThat(serviceIcmp.getProtocolEffective(), equalTo(Protocol.ICMP));
    assertThat(serviceIcmp.getIcmpCode(), equalTo(255));
    assertThat(serviceIcmp.getIcmpType(), equalTo(255));

    assertThat(serviceIcmp6.getProtocol(), equalTo(Protocol.ICMP6));
    assertThat(serviceIcmp6.getProtocolEffective(), equalTo(Protocol.ICMP6));
    // Check defaults
    assertThat(serviceIcmp6.getIcmpCode(), nullValue());
    assertThat(serviceIcmp6.getIcmpType(), nullValue());

    assertThat(serviceIp.getProtocol(), equalTo(Protocol.IP));
    assertThat(serviceIp.getProtocolEffective(), equalTo(Protocol.IP));
    assertThat(serviceIp.getProtocolNumber(), equalTo(254));
    assertThat(serviceIp.getProtocolNumberEffective(), equalTo(254));

    assertThat(serviceChangeProtocol.getProtocol(), equalTo(Protocol.IP));
    assertThat(serviceChangeProtocol.getProtocolEffective(), equalTo(Protocol.IP));
    // Should revert to default after changing protocol
    assertThat(serviceChangeProtocol.getProtocolNumber(), nullValue());
    assertThat(
        serviceChangeProtocol.getProtocolNumberEffective(),
        equalTo(Service.DEFAULT_PROTOCOL_NUMBER));
    // Check that other protocol's values were cleared
    assertThat(serviceChangeProtocol.getIcmpCode(), nullValue());
    assertThat(serviceChangeProtocol.getIcmpType(), nullValue());
    assertThat(serviceChangeProtocol.getSctpPortRangeDst(), nullValue());
    assertThat(serviceChangeProtocol.getSctpPortRangeSrc(), nullValue());
    assertThat(serviceChangeProtocol.getTcpPortRangeDst(), nullValue());
    assertThat(serviceChangeProtocol.getTcpPortRangeSrc(), nullValue());
    assertThat(serviceChangeProtocol.getUdpPortRangeDst(), nullValue());
    assertThat(serviceChangeProtocol.getUdpPortRangeSrc(), nullValue());
  }

  @Test
  public void testServiceWarnings() throws IOException {
    String hostname = "service_warnings";

    Batfish batfish = getBatfishForConfigurationNames(hostname);
    Warnings warnings =
        getOnlyElement(
            batfish
                .loadParseVendorConfigurationAnswerElement(batfish.getSnapshot())
                .getWarnings()
                .values());
    assertThat(
        warnings,
        hasParseWarnings(
            containsInAnyOrder(
                hasComment("Illegal value for service name"),
                hasComment(
                    "Cannot set IP protocol number for service setting props for wrong protocol"
                        + " when protocol is not set to IP."),
                hasComment(
                    "Cannot set ICMP code for service setting props for wrong protocol when"
                        + " protocol is not set to ICMP or ICMP6."),
                hasComment(
                    "Cannot set ICMP type for service setting props for wrong protocol when"
                        + " protocol is not set to ICMP or ICMP6."),
                hasComment(
                    "Cannot set SCTP port range for service setting props for wrong protocol when"
                        + " protocol is not set to TCP/UDP/SCTP."),
                hasComment(
                    "Cannot set TCP port range for service setting props for wrong protocol when"
                        + " protocol is not set to TCP/UDP/SCTP."),
                hasComment(
                    "Cannot set UDP port range for service setting props for wrong protocol when"
                        + " protocol is not set to TCP/UDP/SCTP."))));
  }

  @Test
  public void testFirewallPolicyExtraction() {
    String hostname = "firewall_policy";
    FortiosConfiguration vc = parseVendorConfig(hostname);

    Map<String, Policy> policies = vc.getPolicies();
    assertThat(policies, hasKeys(contains("0", "4294967294", "1", "2")));
    Policy policyDisable = policies.get("0");
    Policy policyDeny = policies.get("4294967294");
    Policy policyAllow = policies.get("1");
    Policy policyAny = policies.get("2");

    Map<String, Service> services = vc.getServices();
    String service11 = "custom_tcp_11";
    String service11From12 = "custom_tcp_11_from_12";
    String serviceAll = "ALL";
    assertThat(services, hasKeys(containsInAnyOrder(service11, service11From12, serviceAll)));

    Map<String, Address> addresses = vc.getAddresses();
    String addr1 = "addr1";
    String addr2 = "addr2";
    String addrAll = "all";
    assertThat(addresses, hasKeys(containsInAnyOrder(addr1, addr2, addrAll)));

    Map<String, Interface> interfaces = vc.getInterfaces();
    String port1 = "port1";
    String port2 = "port2";
    assertThat(interfaces, hasKeys(containsInAnyOrder(port1, port2)));

    assertThat(policyDisable.getAction(), equalTo(Action.DENY));
    assertThat(policyDisable.getStatus(), equalTo(Policy.Status.DISABLE));
    assertThat(policyDisable.getStatusEffective(), equalTo(Policy.Status.DISABLE));
    assertThat(policyDisable.getService(), contains(service11));
    assertThat(policyDisable.getSrcIntf(), contains(port1));
    assertThat(policyDisable.getDstIntf(), contains(port2));
    assertThat(policyDisable.getSrcAddr(), contains(addr1));
    assertThat(policyDisable.getDstAddr(), contains(addr2));

    assertThat(policyDeny.getAction(), nullValue());
    assertThat(policyDeny.getActionEffective(), equalTo(Action.DENY));
    assertThat(policyDeny.getComments(), equalTo("firewall policy comments"));
    assertThat(policyDeny.getName(), equalTo("longest allowed firewall policy nam"));
    assertThat(policyDeny.getStatus(), nullValue());
    assertThat(policyDeny.getStatusEffective(), equalTo(Policy.Status.ENABLE));
    assertThat(policyDeny.getService(), contains(service11From12));
    assertThat(policyDeny.getSrcIntf(), contains(port1));
    assertThat(policyDeny.getDstIntf(), containsInAnyOrder(port1, port2));
    assertThat(policyDeny.getSrcAddr(), contains(addr1));
    assertThat(policyDeny.getDstAddr(), contains(addr2));

    assertThat(policyAllow.getAction(), equalTo(Action.ALLOW));
    assertThat(policyAllow.getStatus(), equalTo(Policy.Status.ENABLE));
    assertThat(policyAllow.getStatusEffective(), equalTo(Policy.Status.ENABLE));
    assertThat(policyAllow.getService(), containsInAnyOrder(service11, service11From12));
    assertThat(policyAllow.getSrcIntf(), containsInAnyOrder(port1, port2));
    assertThat(policyAllow.getDstIntf(), containsInAnyOrder(port1, port2));
    assertThat(policyAllow.getSrcAddr(), containsInAnyOrder(addr1, addr2));
    assertThat(policyAllow.getDstAddr(), containsInAnyOrder(addr1, addr2));

    assertThat(policyAny.getService(), contains(serviceAll));
    assertThat(policyAny.getSrcAddr(), contains(addrAll));
    assertThat(policyAny.getDstAddr(), contains(addrAll));
    assertThat(policyAny.getSrcIntf(), contains(Policy.ANY_INTERFACE));
    assertThat(policyAny.getDstIntf(), contains(Policy.ANY_INTERFACE));
  }

  /**
   * Test extraction of firewall policy when warnings are generated for invalid / pruned properties.
   */
  @Test
  public void testFirewallPolicyExtactionWithWarnings() {
    String hostname = "firewall_policy_warn";
    FortiosConfiguration vc = parseVendorConfig(hostname);

    Map<String, Policy> policies = vc.getPolicies();
    assertThat(policies, hasKeys(contains("1")));
    Policy policy = policies.get("1");

    Map<String, Service> services = vc.getServices();
    String service10 = "service10";
    String service20 = "service20";
    assertThat(services, hasKeys(containsInAnyOrder(service10, service20)));

    Map<String, Address> addresses = vc.getAddresses();
    String addr10 = "addr10";
    String addr20 = "addr20";
    assertThat(addresses, hasKeys(containsInAnyOrder(addr10, addr20)));

    Map<String, Interface> interfaces = vc.getInterfaces();
    String port10 = "port10";
    String port20 = "port20";
    assertThat(interfaces, hasKeys(containsInAnyOrder(port10, port20)));

    // Confirm invalid any/all specifiers are dropped when appropriate
    assertThat(policy.getSrcIntf(), contains(port10));
    assertThat(policy.getSrcAddr(), contains(addr10));
    assertThat(policy.getDstAddr(), contains(addr20));
    // Confirm a line with invalid ALL service is ignored; i.e. previous value isn't overwritten
    assertThat(policy.getService(), contains(service20));
    // Confirm a line with dstintf combining any and another interface is accepted
    assertThat(policy.getDstIntf(), containsInAnyOrder(Policy.ANY_INTERFACE, port20));
  }

  @Test
  public void testFirewallPolicyWarnings() throws IOException {
    String hostname = "firewall_policy_warn";

    Batfish batfish = getBatfishForConfigurationNames(hostname);
    Warnings warnings =
        getOnlyElement(
            batfish
                .loadParseVendorConfigurationAnswerElement(batfish.getSnapshot())
                .getWarnings()
                .values());
    assertThat(
        warnings,
        hasParseWarnings(
            containsInAnyOrder(
                hasComment("Expected policy number in range 0-4294967294, but got '4294967295'"),
                hasComment("Expected policy number in range 0-4294967294, but got 'not_a_number'"),
                hasComment("Illegal value for policy name"),
                hasComment(
                    "Interface/zone port1 is undefined and cannot be added to policy 4294967295"),
                hasComment(
                    "Interface/zone port2 is undefined and cannot be added to policy 4294967295"),
                hasComment(
                    "Interface/zone port3 is undefined and cannot be added to policy 4294967295"),
                hasComment(
                    "Interface/zone port4 is undefined and cannot be added to policy 4294967295"),
                hasComment(
                    "Service service1 is undefined and cannot be added to policy 4294967295"),
                hasComment(
                    "Service service2 is undefined and cannot be added to policy 4294967295"),
                hasComment("Address addr1 is undefined and cannot be added to policy 4294967295"),
                hasComment("Address addr2 is undefined and cannot be added to policy 4294967295"),
                hasComment("Address addr3 is undefined and cannot be added to policy 4294967295"),
                hasComment("Address addr4 is undefined and cannot be added to policy 4294967295"),
                hasComment("Cannot combine 'ALL' with other services"),
                allOf(
                    hasComment("When 'all' is set together with other address(es), it is removed"),
                    hasText("addr10 all")),
                allOf(
                    hasComment("When 'all' is set together with other address(es), it is removed"),
                    hasText("addr20 all")),
                allOf(
                    hasComment("When 'any' is set together with other interfaces, it is removed"),
                    hasText("any port10")))));
  }

  @Test
  public void testSystemRecovery() throws IOException {
    String hostname = "fortios_system_recovery";
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    batfish.getSettings().setDisableUnrecognized(false);
    FortiosConfiguration vc =
        (FortiosConfiguration)
            batfish.loadVendorConfigurations(batfish.getSnapshot()).get(hostname);
    assertThat(vc.getInterfaces(), hasKeys("port1"));

    // make sure the line was actually unrecognized
    Warnings warnings =
        getOnlyElement(
            batfish
                .loadParseVendorConfigurationAnswerElement(batfish.getSnapshot())
                .getWarnings()
                .values());
    assertThat(
        warnings,
        hasParseWarning(
            allOf(
                hasComment("This syntax is unrecognized"),
                hasText("set APropertyThatHopefullyDoesNotExist to a bunch of garbage"))));
  }

  @Test
  public void testPolicyDefinitionsAndReferences() throws IOException {
    String hostname = "policy_defs_refs";
    String filename = "configs/" + hostname;

    Batfish batfish = getBatfishForConfigurationNames(hostname);
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());

    assertThat(ccae, hasDefinedStructure(filename, FortiosStructureType.INTERFACE, "port1"));
    assertThat(ccae, hasDefinedStructure(filename, FortiosStructureType.INTERFACE, "port2"));
    assertThat(ccae, hasDefinedStructure(filename, FortiosStructureType.INTERFACE, "port3"));
    assertThat(
        ccae, hasDefinedStructure(filename, FortiosStructureType.SERVICE_CUSTOM, "service1"));
    assertThat(
        ccae, hasDefinedStructure(filename, FortiosStructureType.SERVICE_CUSTOM, "service2"));
    assertThat(
        ccae, hasDefinedStructure(filename, FortiosStructureType.SERVICE_CUSTOM, "service3"));
    assertThat(ccae, hasDefinedStructure(filename, FortiosStructureType.ADDRESS, "addr1"));
    assertThat(ccae, hasDefinedStructure(filename, FortiosStructureType.ADDRESS, "addr2"));
    assertThat(ccae, hasDefinedStructure(filename, FortiosStructureType.ADDRESS, "addr3"));
    assertThat(ccae, hasDefinedStructure(filename, FortiosStructureType.POLICY, "1"));

    // Confirm reference count is correct for used structure
    assertThat(ccae, hasNumReferrers(filename, FortiosStructureType.ADDRESS, "addr1", 2));
    assertThat(ccae, hasNumReferrers(filename, FortiosStructureType.ADDRESS, "addr2", 2));
    assertThat(ccae, hasNumReferrers(filename, FortiosStructureType.ADDRESS, "addr3", 1));
    // Interface refs include self-refs
    assertThat(ccae, hasNumReferrers(filename, FortiosStructureType.INTERFACE, "port1", 3));
    assertThat(ccae, hasNumReferrers(filename, FortiosStructureType.INTERFACE, "port2", 3));
    assertThat(ccae, hasNumReferrers(filename, FortiosStructureType.INTERFACE, "port3", 2));
    assertThat(ccae, hasNumReferrers(filename, FortiosStructureType.SERVICE_CUSTOM, "service1", 1));
    assertThat(ccae, hasNumReferrers(filename, FortiosStructureType.SERVICE_CUSTOM, "service2", 1));

    // Confirm undefined references are detected
    assertThat(
        ccae,
        hasUndefinedReference(
            filename,
            FortiosStructureType.INTERFACE_OR_ZONE,
            "UNDEFINED",
            FortiosStructureUsage.POLICY_DSTINTF));
    assertThat(
        ccae,
        hasUndefinedReference(
            filename,
            FortiosStructureType.INTERFACE_OR_ZONE,
            "UNDEFINED",
            FortiosStructureUsage.POLICY_SRCINTF));
    assertThat(
        ccae,
        hasUndefinedReference(
            filename,
            FortiosStructureType.ADDRESS_OR_ADDRGRP,
            "UNDEFINED",
            FortiosStructureUsage.POLICY_DSTADDR));
    assertThat(
        ccae,
        hasUndefinedReference(
            filename,
            FortiosStructureType.ADDRESS_OR_ADDRGRP,
            "UNDEFINED",
            FortiosStructureUsage.POLICY_SRCADDR));
    assertThat(
        ccae,
        hasUndefinedReference(
            filename,
            FortiosStructureType.SERVICE_CUSTOM_OR_SERVICE_GROUP,
            "UNDEFINED",
            FortiosStructureUsage.POLICY_SERVICE));
  }

  @Test
  public void testEditRecovery() throws IOException {
    String hostname = "edit_recovery";
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    batfish.getSettings().setDisableUnrecognized(false);
    FortiosConfiguration vc =
        (FortiosConfiguration)
            batfish.loadVendorConfigurations(batfish.getSnapshot()).get(hostname);
    assertThat(vc.getInterfaces(), hasKeys("port1"));
    assertThat(vc.getAddresses(), hasKeys("addr1"));
    assertThat(vc.getServices(), hasKeys("service1"));
    assertThat(vc.getPolicies(), hasKeys("1"));

    // Make sure the lines were actually unrecognized
    Warnings warnings =
        getOnlyElement(
            batfish
                .loadParseVendorConfigurationAnswerElement(batfish.getSnapshot())
                .getWarnings()
                .values());
    assertThat(
        warnings,
        hasParseWarnings(
            containsInAnyOrder(
                allOf(
                    hasComment("This syntax is unrecognized"),
                    hasText("set UNDEFINED_ADDR_PROP to a bunch of garbage")),
                allOf(
                    hasComment("This syntax is unrecognized"),
                    hasText("set UNDEFINED_SERVICE_PROP to a bunch of garbage")),
                allOf(
                    hasComment("This syntax is unrecognized"),
                    hasText("set UNDEFINED_IFACE_PROP to a bunch of garbage")),
                allOf(
                    hasComment("This syntax is unrecognized"),
                    hasText("set UNDEFINED_POLICY_PROP to a bunch of garbage")))));

    // Make sure other props were still set, after the unrecognized lines
    assertThat(vc.getInterfaces().get("port1").getVdom(), equalTo("root"));
    assertThat(vc.getAddresses().get("addr1").getComment(), equalTo("addr comment"));
    assertThat(vc.getServices().get("service1").getComment(), equalTo("service comment"));
    assertThat(vc.getPolicies().get("1").getComments(), equalTo("policy comments, plural"));
  }

  private static final BddTestbed BDD_TESTBED =
      new BddTestbed(ImmutableMap.of(), ImmutableMap.of());

  private static final BDD ZERO;

  @SuppressWarnings("unused")
  private static final IpAccessListToBdd ACL_TO_BDD;

  @SuppressWarnings("unused")
  private static final IpSpaceToBDD DST_IP_BDD;

  private static final IpSpaceToBDD SRC_IP_BDD;

  private static final String TESTCONFIGS_PREFIX = "org/batfish/grammar/fortios/testconfigs/";

  @SuppressWarnings("unused")
  private static final String SNAPSHOTS_PREFIX = "org/batfish/grammar/fortios/snapshots/";

  static {
    ZERO = BDD_TESTBED.getPkt().getFactory().zero();
    DST_IP_BDD = BDD_TESTBED.getDstIpBdd();
    SRC_IP_BDD = BDD_TESTBED.getSrcIpBdd();
    ACL_TO_BDD = BDD_TESTBED.getAclToBdd();
  }

  private @Nonnull Batfish getBatfishForConfigurationNames(String... configurationNames)
      throws IOException {
    String[] names =
        Arrays.stream(configurationNames).map(s -> TESTCONFIGS_PREFIX + s).toArray(String[]::new);
    Batfish batfish = BatfishTestUtils.getBatfishForTextConfigs(_folder, names);
    return batfish;
  }

  private @Nonnull Configuration parseConfig(String hostname) throws IOException {
    Map<String, Configuration> configs = parseTextConfigs(hostname);
    String canonicalHostname = hostname.toLowerCase();
    assertThat(configs, hasEntry(equalTo(canonicalHostname), hasHostname(canonicalHostname)));
    return configs.get(canonicalHostname);
  }

  private @Nonnull Map<String, Configuration> parseTextConfigs(String... configurationNames)
      throws IOException {
    IBatfish iBatfish = getBatfishForConfigurationNames(configurationNames);
    return iBatfish.loadConfigurations(iBatfish.getSnapshot());
  }

  private @Nonnull FortiosConfiguration parseVendorConfig(String hostname) {
    String src = readResource(TESTCONFIGS_PREFIX + hostname, UTF_8);
    Settings settings = new Settings();
    configureBatfishTestSettings(settings);
    FortiosCombinedParser parser = new FortiosCombinedParser(src, settings);
    FortiosControlPlaneExtractor extractor =
        new FortiosControlPlaneExtractor(src, parser, new Warnings());
    ParserRuleContext tree =
        Batfish.parse(parser, new BatfishLogger(BatfishLogger.LEVELSTR_FATAL, false), settings);
    extractor.processParseTree(TEST_SNAPSHOT, tree);
    FortiosConfiguration vendorConfiguration =
        (FortiosConfiguration) extractor.getVendorConfiguration();
    vendorConfiguration.setFilename(TESTCONFIGS_PREFIX + hostname);
    // crash if not serializable
    return SerializationUtils.clone(vendorConfiguration);
  }
}
