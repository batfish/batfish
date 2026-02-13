package org.batfish.grammar.fortios;

import static com.google.common.collect.Iterables.getOnlyElement;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.matchers.ParseWarningMatchers.hasComment;
import static org.batfish.common.matchers.ParseWarningMatchers.hasText;
import static org.batfish.common.matchers.WarningsMatchers.hasParseWarning;
import static org.batfish.common.matchers.WarningsMatchers.hasParseWarnings;
import static org.batfish.common.matchers.WarningsMatchers.hasRedFlags;
import static org.batfish.common.util.Resources.readResource;
import static org.batfish.datamodel.Names.generatedBgpPeerExportPolicyName;
import static org.batfish.datamodel.matchers.BgpProcessMatchers.hasMultipathEbgp;
import static org.batfish.datamodel.matchers.BgpProcessMatchers.hasMultipathIbgp;
import static org.batfish.datamodel.matchers.BgpProcessMatchers.hasRouterId;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasHostname;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasInterface;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasBandwidth;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasDefinedStructure;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasDefinedStructureWithDefinitionLines;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasNumReferrers;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasOutgoingFilter;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasReferencedStructure;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasUndefinedReference;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasAddress;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasDescription;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasInterfaceType;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasSpeed;
import static org.batfish.datamodel.matchers.IpAccessListMatchers.accepts;
import static org.batfish.datamodel.matchers.IpAccessListMatchers.rejects;
import static org.batfish.datamodel.matchers.MapMatchers.hasKeys;
import static org.batfish.main.BatfishTestUtils.DUMMY_SNAPSHOT_1;
import static org.batfish.main.BatfishTestUtils.configureBatfishTestSettings;
import static org.batfish.representation.fortios.FortiosConfiguration.computeVrfName;
import static org.batfish.representation.fortios.FortiosPolicyConversions.computeOutgoingFilterName;
import static org.batfish.representation.fortios.FortiosPolicyConversions.getPolicyName;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import net.sf.javabdd.BDD;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Warnings;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.BDDSourceManager;
import org.batfish.common.bdd.IpAccessListToBdd;
import org.batfish.common.bdd.IpAccessListToBddImpl;
import org.batfish.common.bdd.IpSpaceToBDD;
import org.batfish.common.bdd.PermitAndDenyBdds;
import org.batfish.common.matchers.WarningMatchers;
import org.batfish.common.plugin.IBatfish;
import org.batfish.config.Settings;
import org.batfish.datamodel.AbstractRouteDecorator;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.BddTestbed;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Interface.Dependency;
import org.batfish.datamodel.Interface.DependencyType;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpRange;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Names;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.bgp.AddressFamily;
import org.batfish.datamodel.route.nh.NextHopInterface;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.representation.fortios.AccessList;
import org.batfish.representation.fortios.AccessListRule;
import org.batfish.representation.fortios.Address;
import org.batfish.representation.fortios.Addrgrp;
import org.batfish.representation.fortios.BatfishUUID;
import org.batfish.representation.fortios.BfdSettings;
import org.batfish.representation.fortios.BgpNeighbor;
import org.batfish.representation.fortios.BgpNetwork;
import org.batfish.representation.fortios.BgpProcess;
import org.batfish.representation.fortios.FortiosConfiguration;
import org.batfish.representation.fortios.FortiosStructureType;
import org.batfish.representation.fortios.FortiosStructureUsage;
import org.batfish.representation.fortios.Interface;
import org.batfish.representation.fortios.Interface.Speed;
import org.batfish.representation.fortios.Interface.Status;
import org.batfish.representation.fortios.Interface.Type;
import org.batfish.representation.fortios.InternetServiceName;
import org.batfish.representation.fortios.Ippool;
import org.batfish.representation.fortios.Policy;
import org.batfish.representation.fortios.Policy.Action;
import org.batfish.representation.fortios.PrefixList;
import org.batfish.representation.fortios.PrefixListRule;
import org.batfish.representation.fortios.RouteMap;
import org.batfish.representation.fortios.RouteMapRule;
import org.batfish.representation.fortios.SecondaryIp;
import org.batfish.representation.fortios.Service;
import org.batfish.representation.fortios.Service.Protocol;
import org.batfish.representation.fortios.ServiceGroup;
import org.batfish.representation.fortios.StaticRoute;
import org.batfish.representation.fortios.Zone;
import org.batfish.representation.fortios.Zone.IntrazoneAction;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

public final class FortiosGrammarTest {

  @Test
  public void testIgnoredConfigBlocks() {
    FortiosConfiguration c = parseVendorConfig("fortios_ignored");
    // Valid syntax before ignored block is parsed
    assertThat(c.getHostname(), equalTo("ignored"));
    // Valid syntax after an ignored block is parsed (and before another one)
    assertThat(c.getAddresses(), hasKeys("Extracted Address"));
  }

  @Test
  public void testSystemGlobalIgnoredBlocks() {
    FortiosConfiguration c = parseVendorConfig("fortios_system_ignored");
    assertThat(c.getHostname(), equalTo("fg-ignored"));
    assertThat(c.getAddresses(), hasKeys("addr1"));
  }

  @Test
  public void testHostnameExtraction() {
    String filename = "fortios_hostname";
    String hostname = "my_fortios-hostname1";
    assertThat(parseVendorConfig(filename).getHostname(), equalTo(hostname));
  }

  @Test
  public void testCanonicalHostnameExtraction() {
    String hostname = "fortios_UPPER_hostname";
    assertThat(parseVendorConfig(hostname).getHostname(), equalTo(hostname.toLowerCase()));
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
  public void testHumanName() throws IOException {
    Configuration c = parseConfig("humanname");
    assertThat(c.getHumanName(), equalTo("HUMANNAME"));
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
  public void testInternetServiceNameExtraction() {
    String hostname = "internet_service_name";
    FortiosConfiguration vc = parseVendorConfig(hostname);

    Map<String, InternetServiceName> isns = vc.getInternetServiceNames();
    assertThat(isns, hasKeys("Google-DNS", "Google-Gmail", "custom_isn_for_testing"));

    InternetServiceName dns = isns.get("Google-DNS");
    InternetServiceName gmail = isns.get("Google-Gmail");
    InternetServiceName custom = isns.get("custom_isn_for_testing");

    assertThat(dns.getInternetServiceId(), equalTo(65539L));
    assertNull(dns.getType());
    assertThat(dns.getTypeEffective(), equalTo(InternetServiceName.DEFAULT_TYPE));
    assertThat(gmail.getInternetServiceId(), equalTo(65646L));
    assertThat(gmail.getType(), equalTo(InternetServiceName.Type.DEFAULT));
    assertThat(gmail.getTypeEffective(), equalTo(InternetServiceName.Type.DEFAULT));
    assertThat(custom.getType(), equalTo(InternetServiceName.Type.LOCATION));
    assertThat(custom.getTypeEffective(), equalTo(InternetServiceName.Type.LOCATION));
    assertThat(custom.getInternetServiceId(), equalTo(4294967295L));
  }

  @Test
  public void testInternetServiceNameWarnings() throws IOException {
    String hostname = "internet_service_name_warn";
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    Warnings w =
        getOnlyElement(
            batfish
                .loadParseVendorConfigurationAnswerElement(batfish.getSnapshot())
                .getWarnings()
                .values());

    assertThat(
        w,
        hasParseWarnings(
            containsInAnyOrder(
                hasComment(
                    "Expected internet-service-id in range 0-4294967295, but got '4294967296'"),
                hasComment("Illegal value for internet-service-name name"),
                hasComment("Internet-service-name edit block ignored: name is invalid"))));
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
            "ipmask-default",
            "iprange",
            "iprange-default",
            "fqdn",
            "dynamic",
            "geography",
            "interface-subnet",
            "mac",
            "wildcard",
            "wildcard-default",
            "undefined-refs",
            "abcdefghijklmnopqrstuvwxyz abcdefghijklmnopqrstuvwxyz abcdefghijklmnopqrstuvwxy"));

    Address ipmask = addresses.get("ipmask");
    Address ipmaskDefault = addresses.get("ipmask-default");
    Address iprange = addresses.get("iprange");
    Address iprangeDefault = addresses.get("iprange-default");
    Address fqdn = addresses.get("fqdn");
    Address dynamic = addresses.get("dynamic");
    Address geography = addresses.get("geography");
    Address interfaceSubnet = addresses.get("interface-subnet");
    Address undefinedRefs = addresses.get("undefined-refs");
    Address mac = addresses.get("mac");
    Address wildcard = addresses.get("wildcard");
    Address wildcardDefault = addresses.get("wildcard-default");
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
    assertThat(ipmask.getTypeSpecificFields().getIp1(), equalTo(Ip.parse("1.1.1.0")));
    assertThat(ipmask.getTypeSpecificFields().getIp2(), equalTo(Ip.parse("255.255.255.0")));
    assertThat(iprange.getTypeSpecificFields().getIp1(), equalTo(Ip.parse("1.1.1.0")));
    assertThat(iprange.getTypeSpecificFields().getIp2(), equalTo(Ip.parse("1.1.1.255")));
    assertThat(interfaceSubnet.getTypeSpecificFields().getInterface(), equalTo("port1"));
    // Configured IP is 2.2.2.2, but mask should canonicalize it to 2.0.0.2
    assertThat(wildcard.getTypeSpecificFields().getIp1(), equalTo(Ip.parse("2.0.0.2")));
    assertThat(wildcard.getTypeSpecificFields().getIp2(), equalTo(Ip.parse("255.0.0.255")));
    assertThat(longName.getTypeSpecificFields().getIp1(), equalTo(Ip.parse("1.1.1.0")));
    assertThat(longName.getTypeSpecificFields().getIp2(), equalTo(Ip.parse("255.255.255.0")));

    // Test that type-specific field defaults are correct
    assertThat(ipmaskDefault.getTypeSpecificFields().getIp1Effective(), equalTo(Ip.ZERO));
    assertThat(ipmaskDefault.getTypeSpecificFields().getIp2Effective(), equalTo(Ip.ZERO));
    assertThat(iprangeDefault.getTypeSpecificFields().getIp1Effective(), equalTo(Ip.ZERO));
    // Skip ip2 for iprange; end-ip must be set.
    assertThat(wildcardDefault.getTypeSpecificFields().getIp1Effective(), equalTo(Ip.ZERO));
    assertThat(wildcardDefault.getTypeSpecificFields().getIp2Effective(), equalTo(Ip.ZERO));

    // Test explicitly set values
    assertThat(ipmask.getAllowRouting(), equalTo(true));
    assertThat(ipmask.getAssociatedInterface(), equalTo("port1"));
    assertThat(ipmask.getComment(), equalTo("Hello world"));
    assertThat(ipmask.getFabricObject(), equalTo(true));
    assertThat(iprange.getAssociatedInterfaceZone(), equalTo("zoneA"));

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
  public void testAddressTypeSwitchingExtraction() throws IOException {
    String hostname = "address_type_switching";
    FortiosConfiguration vc = parseVendorConfig(hostname);

    Map<String, Address> addresses = vc.getAddresses();
    assertThat(
        addresses,
        hasKeys(
            "mask-to-range",
            "mask-to-wildcard",
            "range-to-mask",
            "range-to-wildcard",
            "wildcard-to-mask",
            "wildcard-to-range"));

    Address maskToRange = addresses.get("mask-to-range");
    Address maskToWildcard = addresses.get("mask-to-wildcard");
    Address rangeToMask = addresses.get("range-to-mask");
    Address rangeToWildcard = addresses.get("range-to-wildcard");
    Address wildcardToMask = addresses.get("wildcard-to-mask");
    Address wildcardToRange = addresses.get("wildcard-to-range");

    // Set subnet 1.2.2.0 1.1.1.255, then set type iprange.
    assertThat(maskToRange.getTypeSpecificFields().getIp1(), equalTo(Ip.parse("1.0.0.0")));
    assertThat(maskToRange.getTypeSpecificFields().getIp2(), equalTo(Ip.parse("1.1.1.255")));

    // Set subnet 1.2.2.0 1.1.1.255, then set type wildcard.
    assertThat(maskToWildcard.getTypeSpecificFields().getIp1(), equalTo(Ip.parse("1.0.0.0")));
    assertThat(maskToWildcard.getTypeSpecificFields().getIp2(), equalTo(Ip.parse("1.1.1.255")));

    // Set start-ip 255.1.1.1 and end-ip 255.0.0.0, then set type ipmask.
    // Switching type does not canonicalize subnet IP.
    assertThat(rangeToMask.getTypeSpecificFields().getIp1(), equalTo(Ip.parse("255.1.1.1")));
    assertThat(rangeToMask.getTypeSpecificFields().getIp2(), equalTo(Ip.parse("255.0.0.0")));

    // Set start-ip 255.1.1.1 and end-ip 128.0.255.0, then set type wildcard.
    // Switching type does not canonicalize wildcard IP.
    assertThat(rangeToWildcard.getTypeSpecificFields().getIp1(), equalTo(Ip.parse("255.1.1.1")));
    assertThat(rangeToWildcard.getTypeSpecificFields().getIp2(), equalTo(Ip.parse("128.0.255.0")));

    // Set wildcard 1.1.1.1 255.255.0.0, then set type ipmask.
    assertThat(wildcardToMask.getTypeSpecificFields().getIp1(), equalTo(Ip.parse("1.1.0.0")));
    assertThat(wildcardToMask.getTypeSpecificFields().getIp2(), equalTo(Ip.parse("255.255.0.0")));

    // Set wildcard 1.1.1.1 255.255.0.0, then set type iprange.
    assertThat(wildcardToRange.getTypeSpecificFields().getIp1(), equalTo(Ip.parse("1.1.0.0")));
    assertThat(wildcardToRange.getTypeSpecificFields().getIp2(), equalTo(Ip.parse("255.255.0.0")));
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
    warningMatchers.add(hasComment("Address edit block ignored: name is invalid"));

    // Expect warnings for each undefined reference in the config (in an otherwise legal context)
    warningMatchers.add(hasComment("No interface or zone named undefined_iface"));
    warningMatchers.add(hasComment("Cannot reference zoned interface zoned_iface"));
    warningMatchers.add(hasComment("No interface named undefined_iface"));

    warningMatchers.add(
        hasComment(
            "If this address is used as an addrgrp exclude, the FortiOS CLI will reject this"
                + " line"));

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
            "ipmask-default",
            "iprange",
            "iprange-default",
            "fqdn",
            "dynamic",
            "geography",
            "interface-subnet",
            "mac",
            "wildcard",
            "wildcard-default",
            // TODO undefined-refs shouldn't be converted
            "undefined-refs",
            longName));

    BDD prefix1110 = Prefix.parse("1.1.1.0/24").toIpSpace().accept(_srcIpBdd);
    assertThat(ipSpaces.get("ipmask").accept(_srcIpBdd), equalTo(prefix1110));
    assertThat(ipSpaces.get("iprange").accept(_srcIpBdd), equalTo(prefix1110));
    assertThat(ipSpaces.get(longName).accept(_srcIpBdd), equalTo(prefix1110));
    assertThat(
        ipSpaces.get("wildcard").accept(_srcIpBdd),
        equalTo(
            // Configured mask is 255.0.0.255, but in IpWildcard, the set bits mean "don't care"
            IpWildcard.ipWithWildcardMask(Ip.parse("2.2.2.2"), Ip.parse("0.255.255.0"))
                .toIpSpace()
                .accept(_srcIpBdd)));

    BDD allSrcIps = UniverseIpSpace.INSTANCE.accept(_srcIpBdd);
    assertThat(ipSpaces.get("ipmask-default").accept(_srcIpBdd), equalTo(allSrcIps));
    assertThat(
        ipSpaces.get("iprange-default").accept(_srcIpBdd),
        equalTo(IpRange.range(Ip.ZERO, Ip.parse("1.1.1.1")).accept(_srcIpBdd)));
    assertThat(ipSpaces.get("wildcard-default").accept(_srcIpBdd), equalTo(allSrcIps));

    // Unsupported types
    Stream.of("fqdn", "dynamic", "geography", "interface-subnet", "mac", "undefined-refs")
        .forEach(t -> assertThat(ipSpaces.get(t).accept(_srcIpBdd), equalTo(_zero)));
  }

  @Test
  public void testAddressTypeSwitchingConversion() throws IOException {
    String hostname = "address_type_switching";
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    Warnings w =
        getOnlyElement(
            batfish
                .loadParseVendorConfigurationAnswerElement(batfish.getSnapshot())
                .getWarnings()
                .values());

    assertThat(
        w.getParseWarnings(),
        containsInAnyOrder(
            // Set subnet 0.0.0.0/0, then set type iprange
            hasComment("Address edit block ignored: end-ip cannot be 0"),
            // Set type iprange, set start-ip to 1.1.1.1 and end-ip to 2.2.2.2, then set type ipmask
            hasComment("Address edit block ignored: 2.2.2.2 is not a valid subnet mask"),
            // Set type wildcard, set wildcard 1.1.1.1 255.0.255.0, then set type ipmask
            hasComment("Address edit block ignored: 255.0.255.0 is not a valid subnet mask"),
            // Set type iprange, set start-ip to 1.1.1.1 and end-ip to 0.0.255.255, set type
            // wildcard (non-canonical IP 1.1.1.1 is preserved), set type iprange
            hasComment("Address edit block ignored: end-ip must be greater than start-ip")));

    Configuration c = batfish.loadConfigurations(batfish.getSnapshot()).get(hostname);
    Map<String, IpSpace> ipSpaces = c.getIpSpaces();
    assertThat(
        ipSpaces,
        hasKeys(
            "mask-to-range",
            "mask-to-wildcard",
            "range-to-mask",
            "range-to-wildcard",
            "wildcard-to-mask",
            "wildcard-to-range"));

    IpSpace maskToRange = ipSpaces.get("mask-to-range");
    IpSpace maskToWildcard = ipSpaces.get("mask-to-wildcard");
    IpSpace rangeToMask = ipSpaces.get("range-to-mask");
    IpSpace rangeToWildcard = ipSpaces.get("range-to-wildcard");
    IpSpace wildcardToMask = ipSpaces.get("wildcard-to-mask");
    IpSpace wildcardToRange = ipSpaces.get("wildcard-to-range");

    // Set subnet 1.2.2.0 1.1.1.255, then set type iprange.
    assertThat(
        maskToRange.accept(_srcIpBdd),
        equalTo(IpRange.range(Ip.parse("1.0.0.0"), Ip.parse("1.1.1.255")).accept(_srcIpBdd)));

    // Set subnet 1.2.2.0 1.1.1.255, then set type wildcard.
    assertThat(
        maskToWildcard.accept(_srcIpBdd),
        equalTo(
            IpWildcard.ipWithWildcardMask(Ip.parse("1.0.0.0"), Ip.parse("1.1.1.255").inverted())
                .toIpSpace()
                .accept(_srcIpBdd)));

    // Set start-ip 255.1.1.1 and end-ip 255.0.0.0, then set type ipmask.
    assertThat(
        rangeToMask.accept(_srcIpBdd),
        equalTo(Prefix.parse("255.0.0.0/8").toIpSpace().accept(_srcIpBdd)));

    // Set start-ip 255.1.1.1 and end-ip 128.0.255.0, then set type wildcard.
    assertThat(
        rangeToWildcard.accept(_srcIpBdd),
        equalTo(
            IpWildcard.ipWithWildcardMask(Ip.parse("255.1.1.1"), Ip.parse("128.0.255.0").inverted())
                .toIpSpace()
                .accept(_srcIpBdd)));

    // Set wildcard 1.1.1.1 255.255.0.0, then set type ipmask.
    assertThat(
        wildcardToMask.accept(_srcIpBdd),
        equalTo(Prefix.parse("1.1.0.0/16").toIpSpace().accept(_srcIpBdd)));

    // Set wildcard 1.1.1.1 255.255.0.0, then set type iprange.
    assertThat(
        wildcardToRange.accept(_srcIpBdd),
        equalTo(IpRange.range(Ip.parse("1.1.0.0"), Ip.parse("255.255.0.0")).accept(_srcIpBdd)));
  }

  @Test
  public void testAddrgrpExtraction() {
    String hostname = "addrgrp";
    FortiosConfiguration vc = parseVendorConfig(hostname);

    Map<String, Addrgrp> addrgrps = vc.getAddrgrps();
    assertThat(
        addrgrps,
        hasKeys(
            "this is longest possible firewall addrgrp group name that is accepted by device",
            "grp1",
            "grp2"));

    Addrgrp grpLongName =
        addrgrps.get(
            "this is longest possible firewall addrgrp group name that is accepted by device");
    Addrgrp grp1 = addrgrps.get("grp1");
    Addrgrp grp2 = addrgrps.get("grp2");

    // Check defaults
    assertNull(grpLongName.getComment());
    assertNull(grpLongName.getExclude());
    assertThat(grpLongName.getExcludeEffective(), equalTo(Addrgrp.DEFAULT_EXCLUDE));
    assertNull(grpLongName.getFabricObject());
    assertThat(grpLongName.getFabricObjectEffective(), equalTo(Addrgrp.DEFAULT_FABRIC_OBJECT));
    assertNull(grpLongName.getType());
    assertThat(grpLongName.getTypeEffective(), equalTo(Addrgrp.DEFAULT_TYPE));

    assertTrue(grp1.getExclude());
    assertTrue(grp1.getExcludeEffective());
    assertThat(grp1.getType(), equalTo(Addrgrp.Type.FOLDER));
    assertThat(grp1.getTypeEffective(), equalTo(Addrgrp.Type.FOLDER));
    assertThat(grp1.getMember(), containsInAnyOrder("addr1", "addr5"));
    assertThat(grp1.getExcludeMember(), containsInAnyOrder("addr4", "addr5"));

    assertThat(grp2.getComment(), equalTo("some addrgrp comment"));
    assertFalse(grp2.getExclude());
    assertFalse(grp2.getExcludeEffective());
    assertThat(grp2.getType(), equalTo(Addrgrp.Type.DEFAULT));
    assertThat(grp2.getTypeEffective(), equalTo(Addrgrp.Type.DEFAULT));
    assertThat(grp2.getMember(), containsInAnyOrder("addr4", "grp1"));
  }

  @Test
  public void testAddrgrpWarnings() throws IOException {
    String hostname = "addrgrp_warnings";

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
                hasComment("Illegal value for address name"),
                allOf(
                    hasComment("Addrgrp edit block ignored: name is invalid"),
                    hasText(
                        containsString(
                            "this is longer than the longest firewall addrgrp name that is"
                                + " accepted by device"))),
                hasComment("Addrgrp edit block ignored: addrgrp requires at least one member"),
                hasComment(
                    "Addrgrp edit block ignored: addrgrp requires at least one exclude-member when"
                        + " exclude is enabled"),
                hasComment("Address valid is undefined and cannot be referenced"),
                allOf(
                    hasComment("Cannot set exclude-member when exclude is not enabled"),
                    hasText(containsString("exclude-member addr2"))),
                allOf(
                    hasComment("Cannot set exclude-member when exclude is not enabled"),
                    hasText(containsString("exclude-member addr3"))),
                hasComment("Addrgrp cycles cannot be added to valid as it would create a cycle"),
                hasComment("Addrgrp valid cannot be added to valid as it would create a cycle"),
                hasComment("Addrgrp type folder is not yet supported"),
                allOf(
                    hasComment("The type of address group cannot be changed"),
                    hasText(containsString("type default"))))));
  }

  @Test
  public void testBgpRouteMapDefinitionsAndReferences() throws IOException {
    String hostname = "bgp_routemap_defs_refs";
    String filename = "configs/" + hostname;

    Batfish batfish = getBatfishForConfigurationNames(hostname);
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());

    assertThat(ccae, hasDefinedStructure(filename, FortiosStructureType.ROUTE_MAP, "rm1"));
    assertThat(ccae, hasDefinedStructure(filename, FortiosStructureType.ROUTE_MAP, "rm2"));

    assertThat(ccae, hasNumReferrers(filename, FortiosStructureType.ROUTE_MAP, "rm1", 1));
    assertThat(ccae, hasNumReferrers(filename, FortiosStructureType.ROUTE_MAP, "rm2", 1));

    assertThat(
        ccae,
        hasUndefinedReference(
            filename,
            FortiosStructureType.ROUTE_MAP,
            "UNDEFINED_IN",
            FortiosStructureUsage.BGP_NEIGHBOR_ROUTE_MAP_IN));
    assertThat(
        ccae,
        hasUndefinedReference(
            filename,
            FortiosStructureType.ROUTE_MAP,
            "UNDEFINED_OUT",
            FortiosStructureUsage.BGP_NEIGHBOR_ROUTE_MAP_OUT));
  }

  @Test
  public void testBgpExtraction() throws IOException {
    String hostname = "bgp";
    FortiosConfiguration vc = parseVendorConfig(hostname);

    // Ensure no warnings were generated
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    assertThat(
        batfish.loadParseVendorConfigurationAnswerElement(batfish.getSnapshot()).getWarnings(),
        anEmptyMap());

    BgpProcess bgpProcess = vc.getBgpProcess();
    assert bgpProcess != null;
    assertThat(bgpProcess.getAs(), equalTo(1L));
    assertThat(bgpProcess.getRouterId(), equalTo(Ip.parse("1.1.1.1")));
    assertNull(bgpProcess.getEbgpMultipath());
    assertNull(bgpProcess.getIbgpMultipath());
    assertFalse(bgpProcess.getEbgpMultipathEffective());
    assertFalse(bgpProcess.getIbgpMultipathEffective());

    Map<Ip, BgpNeighbor> neighbors = bgpProcess.getNeighbors();
    Ip ip1 = Ip.parse("2.2.2.2");
    Ip ip2 = Ip.parse("11.11.11.2");
    assertThat(neighbors.keySet(), containsInAnyOrder(ip1, ip2));
    BgpNeighbor neighbor1 = neighbors.get(ip1);
    BgpNeighbor neighbor2 = neighbors.get(ip2);
    assertThat(neighbor1.getIp(), equalTo(ip1));
    assertThat(neighbor2.getIp(), equalTo(ip2));
    assertThat(neighbor1.getRemoteAs(), equalTo(1L));
    assertThat(neighbor2.getRemoteAs(), equalTo(4294967295L));
    assertThat(neighbor1.getRouteMapIn(), equalTo("rm1"));
    assertThat(neighbor1.getRouteMapOut(), equalTo("rm2"));
    assertNull(neighbor2.getRouteMapIn());
    assertNull(neighbor2.getRouteMapOut());
    assertThat(neighbor1.getUpdateSource(), equalTo("port1"));
    assertNull(neighbor2.getUpdateSource());

    Map<Long, BgpNetwork> networks = bgpProcess.getNetworks();
    assertThat(networks.keySet(), containsInAnyOrder(1L, 4294967295L));
    BgpNetwork network1 = networks.get(1L);
    BgpNetwork network2 = networks.get(4294967295L);
    assertThat(network1.getId(), equalTo(1L));
    assertThat(network2.getId(), equalTo(4294967295L));
    assertThat(network1.getPrefix(), equalTo(Prefix.parse("3.3.3.0/24")));
    assertThat(network2.getPrefix(), equalTo(Prefix.parse("4.4.4.0/24")));
  }

  @Test
  public void testBgpConversion() throws IOException {
    String hostname = "bgp";
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    Configuration c = batfish.loadConfigurations(batfish.getSnapshot()).get(hostname);

    // Ensure no warnings were generated
    assertThat(
        batfish.loadParseVendorConfigurationAnswerElement(batfish.getSnapshot()).getWarnings(),
        anEmptyMap());

    // Neighbor IDs
    Ip ip1 = Ip.parse("2.2.2.2");
    Ip ip2 = Ip.parse("11.11.11.2");

    // Setup for testing routing policies
    Bgpv4Route.Builder bgpRouteBuilder = Bgpv4Route.testBuilder();
    org.batfish.datamodel.StaticRoute.Builder staticRouteBuilder =
        org.batfish.datamodel.StaticRoute.testBuilder();

    // Default VRF BGP process: should only have neighbor 1
    String defaultVrfName = computeVrfName("root", 0);
    org.batfish.datamodel.BgpProcess bgpProcessDefaultVrf =
        c.getVrfs().get(defaultVrfName).getBgpProcess();
    assertThat(bgpProcessDefaultVrf, hasRouterId(Ip.parse("1.1.1.1")));
    assertThat(bgpProcessDefaultVrf, hasMultipathEbgp(false));
    assertThat(bgpProcessDefaultVrf, hasMultipathIbgp(false));

    Map<Ip, BgpActivePeerConfig> defaultVrfNeighbors = bgpProcessDefaultVrf.getActiveNeighbors();
    assertThat(defaultVrfNeighbors, hasKeys(ip1));
    BgpActivePeerConfig neighbor1 = defaultVrfNeighbors.get(ip1);
    assertThat(neighbor1.getLocalAs(), equalTo(1L));
    // port1 is the explicit update-source
    assertThat(neighbor1.getLocalIp(), equalTo(Ip.parse("10.10.10.1")));
    assertThat(neighbor1.getPeerAddress(), equalTo(ip1));
    assertThat(neighbor1.getRemoteAsns().enumerate(), contains(1L));
    AddressFamily ipv4Af1 = neighbor1.getAddressFamily(AddressFamily.Type.IPV4_UNICAST);
    assertThat(ipv4Af1.getImportPolicy(), equalTo("rm1"));
    String neighbor1ExportPolicyName =
        generatedBgpPeerExportPolicyName(defaultVrfName, ip1.toString());
    assertThat(ipv4Af1.getExportPolicy(), equalTo(neighbor1ExportPolicyName));

    // Test export policy. The BGP process has network statements for 3.3.3.0/24 and 4.4.4.0/24.
    // The neighbor's route-map-out permits 3.3.3.0/24 and 9.9.9.0/24.
    RoutingPolicy neighbor1ExportPolicy = c.getRoutingPolicies().get(neighbor1ExportPolicyName);
    org.batfish.datamodel.StaticRoute static3330 =
        staticRouteBuilder.setNetwork(Prefix.parse("3.3.3.0/24")).build();
    org.batfish.datamodel.StaticRoute static4440 =
        staticRouteBuilder.setNetwork(Prefix.parse("4.4.4.0/24")).build();
    org.batfish.datamodel.StaticRoute static9990 =
        staticRouteBuilder.setNetwork(Prefix.parse("9.9.9.0/24")).build();
    Bgpv4Route bgp8880 = bgpRouteBuilder.setNetwork(Prefix.parse("8.8.8.0/24")).build();
    Bgpv4Route bgp9990 = bgpRouteBuilder.setNetwork(Prefix.parse("9.9.9.0/24")).build();
    // Matches network statement; permitted by route-map
    assertPolicyTakesAction(neighbor1ExportPolicy, true, static3330, c);
    // Matches network statement, but denied by route-map
    assertPolicyTakesAction(neighbor1ExportPolicy, false, static4440, c);
    // Permitted by route-map, but doesn't match network statement
    assertPolicyTakesAction(neighbor1ExportPolicy, false, static9990, c);
    // BGP is allowed out by default, but this route is blocked by route-map
    assertPolicyTakesAction(neighbor1ExportPolicy, false, bgp8880, c);
    // BGP is allowed out by default and this route is permitted by route-map
    assertPolicyTakesAction(neighbor1ExportPolicy, true, bgp9990, c);

    // VRF 5 BGP process: should only have neighbor 2
    String vrf5Name = computeVrfName("root", 5);
    org.batfish.datamodel.BgpProcess bgpProcessVrf5 = c.getVrfs().get(vrf5Name).getBgpProcess();
    assertThat(bgpProcessVrf5, hasRouterId(Ip.parse("1.1.1.1")));

    Map<Ip, BgpActivePeerConfig> vrf5Neighbors = bgpProcessVrf5.getActiveNeighbors();
    assertThat(vrf5Neighbors, hasKeys(ip2));
    BgpActivePeerConfig neighbor2 = vrf5Neighbors.get(ip2);
    assertThat(neighbor2.getLocalAs(), equalTo(1L));
    // port2 is the inferred update-source (its network includes ip2)
    assertThat(neighbor2.getLocalIp(), equalTo(Ip.parse("11.11.11.1")));
    assertThat(neighbor2.getPeerAddress(), equalTo(ip2));
    assertThat(neighbor2.getRemoteAsns().enumerate(), contains(4294967295L));
    AddressFamily ipv4Af2 = neighbor2.getAddressFamily(AddressFamily.Type.IPV4_UNICAST);
    assertNull(ipv4Af2.getImportPolicy());
    String neighbor2ExportPolicyName = generatedBgpPeerExportPolicyName(vrf5Name, ip2.toString());
    assertThat(ipv4Af2.getExportPolicy(), equalTo(neighbor2ExportPolicyName));

    // Test export policy. The BGP process has network statements for 3.3.3.0/24 and 4.4.4.0/24.
    // There is no route-map-out on the neighbor.
    RoutingPolicy neighbor2ExportPolicy = c.getRoutingPolicies().get(neighbor2ExportPolicyName);
    // Match network statements
    assertPolicyTakesAction(neighbor2ExportPolicy, true, static3330, c);
    assertPolicyTakesAction(neighbor2ExportPolicy, true, static4440, c);
    // Doesn't match network statement
    assertPolicyTakesAction(neighbor2ExportPolicy, false, static9990, c);
    // BGP is allowed out by default
    assertPolicyTakesAction(neighbor2ExportPolicy, true, bgp9990, c);
  }

  @Test
  public void testBgpExtractionWarnings() throws IOException {
    String hostname = "bgp_warnings";
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    Warnings parseWarnings =
        getOnlyElement(
            batfish
                .loadParseVendorConfigurationAnswerElement(batfish.getSnapshot())
                .getWarnings()
                .values());
    assertThat(
        parseWarnings.getParseWarnings(),
        containsInAnyOrder(
            hasComment("Expected BGP AS in range 0-4294967295, but got '4294967296'"),
            hasComment("Expected BGP AS in range 0-4294967295, but got 'hello'"),
            hasComment("Cannot use 0.0.0.0 as BGP router-id"),
            hasComment("BGP neighbor edit block ignored: neighbor ID is invalid"),
            hasComment("Expected BGP remote AS in range 1-4294967295, but got '0'"),
            hasComment("Expected BGP remote AS in range 1-4294967295, but got '4294967296'"),
            hasComment("Expected BGP remote AS in range 1-4294967295, but got 'hello'"),
            hasComment("BGP neighbor edit block ignored: remote-as must be set"),
            hasComment("Expected BGP network ID in range 0-4294967295, but got '4294967296'"),
            hasComment("BGP network edit block ignored: name is invalid"),
            hasComment("Prefix 0.0.0.0/0 is not allowed for a BGP network prefix"),
            hasComment(
                "Cannot set BGP network prefix 1.1.1.0/24: Another BGP network already has this"
                    + " prefix"),
            hasComment("BGP network edit block ignored: prefix must be set"),
            hasComment("Redistribution into BGP is not yet supported")));
  }

  @Test
  public void testBgpConversionWarnings() throws IOException {
    String hostname = "bgp_conversion_warnings";
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    Warnings warnings =
        batfish
            .loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot())
            .getWarnings()
            .get(hostname);
    assertThat(
        warnings.getRedFlagWarnings(),
        containsInAnyOrder(
            WarningMatchers.hasText(
                "Ignoring BGP neighbor 2.2.2.2: Update-source port1 has no address"),
            WarningMatchers.hasText(
                "BGP neighbor 3.3.3.3 has an inactive update-source interface port2. Attempting to"
                    + " infer another update-source for this neighbor"),
            WarningMatchers.hasText(
                "Ignoring BGP neighbor 3.3.3.3: Unable to infer its update source"),
            WarningMatchers.hasText(
                "Ignoring BGP neighbor 4.4.4.4: Unable to infer its update source"),
            WarningMatchers.hasText(
                "Interface port3 has unsupported type WL_MESH and will not be converted"),
            WarningMatchers.hasText(
                "Ignoring BGP neighbor 5.5.5.5: Unable to infer its update source")));
  }

  @Test
  public void testBgpConversionNoAs() throws IOException {
    String hostname = "bgp_no_as";
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    // No warnings generated, indicating that BGP conversion was skipped (if it had attempted to
    // convert the neighbor, it would have generated a warning)
    assertThat(
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot()).getWarnings(),
        not(hasKey(hostname)));
  }

  @Test
  public void testBgpMultipath() throws IOException {
    // Tests extraction and conversion for ebgp-multipath and ibgp-multipath properties when
    // explicitly set. Defaults are tested in testBgpExtraction/testBgpConversion. Can't test more
    // than one option per config because each config can only have one top-level BGP configuration.
    String hostname1 = "bgp_multipath_1";
    String hostname2 = "bgp_multipath_2";

    // First config has multipath enabled for EBGP, disabled for IBGP; vice versa for second config
    FortiosConfiguration vc1 = parseVendorConfig(hostname1);
    BgpProcess vcProc1 = vc1.getBgpProcess();
    assert vcProc1 != null;
    assertThat(vcProc1.getEbgpMultipath(), equalTo(true));
    assertThat(vcProc1.getIbgpMultipath(), equalTo(false));
    assertThat(vcProc1.getEbgpMultipathEffective(), equalTo(true));
    assertThat(vcProc1.getIbgpMultipathEffective(), equalTo(false));

    FortiosConfiguration vc2 = parseVendorConfig(hostname2);
    BgpProcess vcProc2 = vc2.getBgpProcess();
    assert vcProc2 != null;
    assertThat(vcProc2.getEbgpMultipath(), equalTo(false));
    assertThat(vcProc2.getIbgpMultipath(), equalTo(true));
    assertThat(vcProc2.getEbgpMultipathEffective(), equalTo(false));
    assertThat(vcProc2.getIbgpMultipathEffective(), equalTo(true));

    Configuration c1 = parseConfig(hostname1);
    org.batfish.datamodel.BgpProcess proc1 =
        c1.getVrfs().get(computeVrfName("root", 0)).getBgpProcess();
    assertThat(proc1, hasMultipathEbgp(true));
    assertThat(proc1, hasMultipathIbgp(false));

    Configuration c2 = parseConfig(hostname2);
    org.batfish.datamodel.BgpProcess proc2 =
        c2.getVrfs().get(computeVrfName("root", 0)).getBgpProcess();
    assertThat(proc2, hasMultipathEbgp(false));
    assertThat(proc2, hasMultipathIbgp(true));
  }

  @Test
  public void testBgpConversionInvalidAs() throws IOException {
    String hostname = "bgp_invalid_as";
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    Warnings warnings =
        batfish
            .loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot())
            .getWarnings()
            .get(hostname);
    assertThat(
        warnings.getRedFlagWarnings(),
        contains(
            WarningMatchers.hasText(
                "Ignoring BGP process: AS 4294967295 is proscribed by RFC 7300")));
  }

  @Test
  public void testBgpConversionNoRouterId() throws IOException {
    String hostname = "bgp_no_router_id";
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    Warnings warnings =
        batfish
            .loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot())
            .getWarnings()
            .get(hostname);
    assertThat(
        warnings.getRedFlagWarnings(),
        contains(WarningMatchers.hasText("Ignoring BGP process: No router ID configured")));
  }

  @Test
  public void testBfdParsing() {
    FortiosConfiguration cc = parseVendorConfig("fortios_bfd");

    // Test global BFD settings
    assertThat(cc.getBfdSettings().getIntervalEffective(), equalTo(100));
    assertThat(cc.getBfdSettings().getMinRxEffective(), equalTo(100));
    assertThat(cc.getBfdSettings().getMinTxEffective(), equalTo(100));
    assertThat(cc.getBfdSettings().getMultiplierEffective(), equalTo(5));

    // Test BGP neighbor BFD
    BgpProcess bgpProcess = cc.getBgpProcess();
    assert bgpProcess != null;
    BgpNeighbor neighbor1 = bgpProcess.getNeighbors().get(Ip.parse("10.0.0.2"));
    assertThat(neighbor1.getBfdEffective(), equalTo(true));

    BgpNeighbor neighbor2 = bgpProcess.getNeighbors().get(Ip.parse("10.0.1.2"));
    assertThat(neighbor2.getBfdEffective(), equalTo(false));

    // Test static route BFD
    StaticRoute route1 = cc.getStaticRoutes().get("1");
    assertThat(route1.getBfdEffective(), equalTo(true));

    StaticRoute route2 = cc.getStaticRoutes().get("2");
    assertThat(route2.getBfdEffective(), equalTo(false));
  }

  @Test
  public void testBfdDefaultValues() {
    FortiosConfiguration cc = parseVendorConfig("fortios_bfd_edge_cases");

    // Global BFD with only interval set - others should use defaults
    assertThat(cc.getBfdSettings().getIntervalEffective(), equalTo(200));
    assertThat(cc.getBfdSettings().getMinRxEffective(), equalTo(50)); // default
    assertThat(cc.getBfdSettings().getMinTxEffective(), equalTo(50)); // default
    assertThat(cc.getBfdSettings().getMultiplierEffective(), equalTo(3)); // default

    // BGP neighbor with BFD not set - should default to false
    BgpProcess bgpProcess = cc.getBgpProcess();
    assert bgpProcess != null;
    BgpNeighbor neighbor = bgpProcess.getNeighbors().get(Ip.parse("10.0.0.1"));
    assertThat(neighbor.getBfdEffective(), equalTo(false));

    // Static route with BFD not set - should default to false
    StaticRoute route = cc.getStaticRoutes().get("1");
    assertThat(route.getBfdEffective(), equalTo(false));
  }

  @Test
  public void testBfdMixedConfigurations() {
    FortiosConfiguration cc = parseVendorConfig("fortios_bfd_mixed");

    BgpProcess bgpProcess = cc.getBgpProcess();
    assert bgpProcess != null;
    Map<Ip, BgpNeighbor> neighbors = bgpProcess.getNeighbors();
    assertThat(neighbors.get(Ip.parse("10.0.0.1")).getBfdEffective(), equalTo(true));
    assertThat(neighbors.get(Ip.parse("10.0.0.2")).getBfdEffective(), equalTo(false));
    assertThat(neighbors.get(Ip.parse("10.0.0.3")).getBfdEffective(), equalTo(false));

    Map<String, StaticRoute> routes = cc.getStaticRoutes();
    assertThat(routes.get("1").getBfdEffective(), equalTo(true));
    assertThat(routes.get("2").getBfdEffective(), equalTo(false));
    assertThat(routes.get("3").getBfdEffective(), equalTo(false));
  }

  @Test
  public void testBfdValidationWarnings() throws IOException {
    String hostname = "fortios_bfd_validation";
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    Warnings warnings =
        getOnlyElement(
            batfish
                .loadParseVendorConfigurationAnswerElement(batfish.getSnapshot())
                .getWarnings()
                .values());

    // Should have warnings for out-of-range BFD values
    assertThat(
        warnings,
        hasParseWarnings(
            containsInAnyOrder(
                hasComment("Expected BFD interval in range 50-5000, but got '60000'"),
                hasComment("Expected BFD minimum RX in range 50-5000, but got '1'"),
                hasComment("Expected BFD minimum TX in range 50-5000, but got '999999'"),
                hasComment("Expected BFD multiplier in range 3-50, but got '100'"))));
  }

  @Test
  public void testBfdValidValues() {
    FortiosConfiguration vc = parseVendorConfig("fortios_bfd_valid");

    // Verify values are parsed correctly
    assertThat(vc.getBfdSettings().getInterval(), equalTo(50));
    assertThat(vc.getBfdSettings().getMinRx(), equalTo(5000));
    assertThat(vc.getBfdSettings().getMinTx(), equalTo(100));
    assertThat(vc.getBfdSettings().getMultiplier(), equalTo(50));
  }

  @Test
  public void testBfdSettingsGettersWithNulls() {
    // Test BfdSettings effective getters when all values are null (should use defaults)
    BfdSettings bfdSettings = new BfdSettings();

    // All getters should return defaults when values are null
    assertThat(bfdSettings.getIntervalEffective(), equalTo(50));
    assertThat(bfdSettings.getMinRxEffective(), equalTo(50));
    assertThat(bfdSettings.getMinTxEffective(), equalTo(50));
    assertThat(bfdSettings.getMultiplierEffective(), equalTo(3));

    // Verify raw getters return null
    assertThat(bfdSettings.getInterval(), nullValue());
    assertThat(bfdSettings.getMinRx(), nullValue());
    assertThat(bfdSettings.getMinTx(), nullValue());
    assertThat(bfdSettings.getMultiplier(), nullValue());
  }

  @Test
  public void testBfdSettingsEffectiveGetterCombinations() {
    // Test various combinations of set and unset values
    BfdSettings bfdSettings = new BfdSettings();

    // Set only interval
    bfdSettings.setInterval(100);
    assertThat(bfdSettings.getIntervalEffective(), equalTo(100));
    assertThat(bfdSettings.getMinRxEffective(), equalTo(50)); // default
    assertThat(bfdSettings.getMinTxEffective(), equalTo(50)); // default
    assertThat(bfdSettings.getMultiplierEffective(), equalTo(3)); // default

    // Set all values
    bfdSettings.setMinRx(200);
    bfdSettings.setMinTx(300);
    bfdSettings.setMultiplier(10);
    assertThat(bfdSettings.getIntervalEffective(), equalTo(100));
    assertThat(bfdSettings.getMinRxEffective(), equalTo(200));
    assertThat(bfdSettings.getMinTxEffective(), equalTo(300));
    assertThat(bfdSettings.getMultiplierEffective(), equalTo(10));
  }

  @Test
  public void testBgpNeighborBfdEffectiveWithNull() {
    // Test BgpNeighbor.getBfdEffective() with null value
    BgpNeighbor neighbor = new BgpNeighbor(Ip.parse("10.0.0.1"));

    // Initially null, should return false
    assertThat(neighbor.getBfd(), nullValue());
    assertThat(neighbor.getBfdEffective(), equalTo(false));

    // Set to false
    neighbor.setBfd(false);
    assertThat(neighbor.getBfdEffective(), equalTo(false));

    // Set to true
    neighbor.setBfd(true);
    assertThat(neighbor.getBfdEffective(), equalTo(true));

    // Set back to null
    neighbor.setBfd(null);
    assertThat(neighbor.getBfdEffective(), equalTo(false));
  }

  @Test
  public void testStaticRouteBfdEffectiveWithNull() {
    // Test StaticRoute.getBfdEffective() with null value
    StaticRoute route = new StaticRoute("1");

    // Initially null, should return DEFAULT_BFD (false)
    assertThat(route.getBfd(), nullValue());
    assertThat(route.getBfdEffective(), equalTo(false));

    // Set to false explicitly
    route.setBfd(false);
    assertThat(route.getBfdEffective(), equalTo(false));

    // Set to true
    route.setBfd(true);
    assertThat(route.getBfdEffective(), equalTo(true));

    // Set back to null
    route.setBfd(null);
    assertThat(route.getBfdEffective(), equalTo(false)); // uses DEFAULT_BFD
  }

  @Test
  public void testIppoolGettersAndSetters() {
    // Test Ippool getters and setters to increase coverage
    BatfishUUID uuid = new BatfishUUID(1);
    Ippool pool = new Ippool("test-pool", uuid);

    // Test getName
    assertThat(pool.getName(), equalTo("test-pool"));

    // Test setName
    pool.setName("new-pool");
    assertThat(pool.getName(), equalTo("new-pool"));

    // Test getBatfishUUID
    assertThat(pool.getBatfishUUID(), equalTo(uuid));

    // Test other setters/getters
    pool.setStartip(Ip.parse("10.0.0.1"));
    assertThat(pool.getStartip(), equalTo(Ip.parse("10.0.0.1")));

    pool.setEndip(Ip.parse("10.0.0.100"));
    assertThat(pool.getEndip(), equalTo(Ip.parse("10.0.0.100")));

    pool.setAssociatedInterface("port1");
    assertThat(pool.getAssociatedInterface(), equalTo("port1"));

    pool.setType(Ippool.Type.OVERLOAD);
    assertThat(pool.getType(), equalTo(Ippool.Type.OVERLOAD));

    pool.setComments("test comments");
    assertThat(pool.getComments(), equalTo("test comments"));
  }

  @Test
  public void testInterfaceExtraction() {
    String hostname = "iface";
    FortiosConfiguration vc = parseVendorConfig(hostname);

    Map<String, Interface> ifaces = vc.getInterfaces();
    assertThat(
        ifaces.keySet(),
        containsInAnyOrder(
            "port1", "port2", "longest if name", "tunnel", "loopback123", "emac", "vlan", "wl"));

    Interface port1 = ifaces.get("port1");
    Interface port2 = ifaces.get("port2");
    Interface longName = ifaces.get("longest if name");
    Interface tunnel = ifaces.get("tunnel");
    Interface loopback = ifaces.get("loopback123");
    Interface emac = ifaces.get("emac");
    Interface vlan = ifaces.get("vlan");
    Interface wl = ifaces.get("wl");

    assertThat(port1.getVdom(), equalTo("root"));
    assertThat(port1.getIp(), equalTo(ConcreteInterfaceAddress.parse("192.168.122.2/24")));
    assertThat(port1.getType(), equalTo(Type.PHYSICAL));
    assertThat(port1.getAlias(), equalTo("longest possibl alias str"));
    assertThat(port1.getDescription(), equalTo("quoted description w/ spaces and more"));
    assertTrue(port1.getSecondaryIp());
    assertTrue(port1.getSecondaryIpEffective());
    assertThat(port1.getSecondaryip().keySet(), containsInAnyOrder("4294967295", "1", "2"));
    SecondaryIp sipA = port1.getSecondaryip().get("4294967295");
    SecondaryIp sipB = port1.getSecondaryip().get("1");
    SecondaryIp sipC = port1.getSecondaryip().get("2");
    assertThat(sipA.getIp(), equalTo(ConcreteInterfaceAddress.create(Ip.parse("10.0.1.1"), 24)));
    assertThat(sipB.getIp(), equalTo(ConcreteInterfaceAddress.create(Ip.parse("10.1.1.1"), 24)));
    assertNull(sipC.getIp());
    // Check defaults
    assertThat(port1.getStatus(), equalTo(Status.UNKNOWN));
    assertTrue(port1.getStatusEffective());
    assertThat(port1.getMtu(), nullValue());
    assertThat(port1.getMtuEffective(), equalTo(Interface.DEFAULT_INTERFACE_MTU));
    assertNull(port1.getSpeed());
    assertThat(port1.getSpeedEffective(), equalTo(Interface.DEFAULT_SPEED));
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
    assertThat(port2.getSpeed(), equalTo(Speed.AUTO));
    assertThat(port2.getSpeedEffective(), equalTo(Speed.AUTO));
    // Check defaults
    assertThat(port2.getType(), nullValue());
    assertThat(port2.getTypeEffective(), equalTo(Type.VLAN));
    assertNull(port2.getSecondaryIp());
    assertFalse(port2.getSecondaryIpEffective());

    assertThat(longName.getIp(), equalTo(ConcreteInterfaceAddress.parse("169.254.1.1/24")));
    assertThat(longName.getAlias(), equalTo(""));
    // Check overriding defaults
    assertTrue(longName.getStatusEffective());
    assertThat(longName.getStatus(), equalTo(Status.UP));
    assertThat(longName.getVrf(), equalTo(31));
    assertThat(longName.getVrfEffective(), equalTo(31));
    assertFalse(longName.getSecondaryIp());
    assertFalse(longName.getSecondaryIpEffective());
    assertThat(longName.getSpeed(), equalTo(Speed.HUNDRED_GFULL));
    assertThat(longName.getSpeedEffective(), equalTo(Speed.HUNDRED_GFULL));
    // Despite being disabled, the secondaryip should persist
    assertThat(longName.getSecondaryip().keySet(), containsInAnyOrder("1"));
    assertThat(
        longName.getSecondaryip().get("1").getIp(),
        equalTo(ConcreteInterfaceAddress.create(Ip.parse("10.2.1.1"), 24)));

    assertThat(tunnel.getStatus(), equalTo(Status.DOWN));
    assertFalse(tunnel.getStatusEffective());
    assertThat(tunnel.getType(), equalTo(Type.TUNNEL));
    // MTU is set, but not used since override isn't set
    assertThat(tunnel.getMtuOverride(), nullValue());
    assertThat(tunnel.getMtu(), equalTo(65535));
    assertThat(tunnel.getMtuEffective(), equalTo(Interface.DEFAULT_INTERFACE_MTU));

    assertThat(vlan.getType(), equalTo(Type.VLAN));
    assertThat(vlan.getInterface(), equalTo("port1"));
    assertThat(vlan.getVlanid(), equalTo(4094));

    assertThat(loopback.getType(), equalTo(Type.LOOPBACK));
    assertThat(emac.getType(), equalTo(Type.EMAC_VLAN));
    assertThat(wl.getType(), equalTo(Type.WL_MESH));
  }

  @Test
  public void testInterfaceConversion() throws IOException {
    String hostname = "iface";
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    Warnings warnings =
        batfish
            .loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot())
            .getWarnings()
            .get(hostname);

    // WL_MESH aren't yet supported; confirm in warnings
    assertThat(
        warnings.getRedFlagWarnings(),
        hasItems(
            WarningMatchers.hasText(
                String.format(
                    "Interface %s has unsupported type %s and will not be converted",
                    "wl", Type.WL_MESH))));

    Configuration c = batfish.loadConfigurations(batfish.getSnapshot()).get(hostname);
    Map<String, org.batfish.datamodel.Interface> ifaces = c.getAllInterfaces();
    assertThat(
        ifaces.keySet(),
        containsInAnyOrder("port1", "port2", "longest if name", "tunnel", "loopback123", "vlan"));
    org.batfish.datamodel.Interface port1 = ifaces.get("port1");
    org.batfish.datamodel.Interface port2 = ifaces.get("port2");
    org.batfish.datamodel.Interface longName = ifaces.get("longest if name");
    org.batfish.datamodel.Interface tunnel = ifaces.get("tunnel");
    org.batfish.datamodel.Interface loopback = ifaces.get("loopback123");
    org.batfish.datamodel.Interface vlan = ifaces.get("vlan");

    // Check active
    assertFalse(tunnel.getActive()); // explicitly set to down
    Stream.of(port1, port2, longName, loopback, vlan)
        .forEach(iface -> assertTrue(iface.getName() + " is up", iface.getActive()));

    // Check VRFs
    assertThat(longName.getVrf().getName(), equalTo(computeVrfName("root", 31)));
    String defaultVrf = computeVrfName("root", 0);
    Stream.of(port1, port2, tunnel, loopback, vlan)
        .forEach(iface -> assertThat(iface.getVrf().getName(), equalTo(defaultVrf)));

    // Check addresses
    assertThat(port1.getAddress(), equalTo(ConcreteInterfaceAddress.parse("192.168.122.2/24")));
    assertThat(
        port1.getAllAddresses(),
        containsInAnyOrder(
            ConcreteInterfaceAddress.parse("192.168.122.2/24"),
            ConcreteInterfaceAddress.parse("10.0.1.1/24"),
            ConcreteInterfaceAddress.parse("10.1.1.1/24")));
    assertThat(longName.getAddress(), equalTo(ConcreteInterfaceAddress.parse("169.254.1.1/24")));
    assertThat(
        longName.getAllAddresses(), contains(ConcreteInterfaceAddress.parse("169.254.1.1/24")));
    Stream.of(port2, tunnel, loopback, vlan).forEach(iface -> assertNull(iface.getAddress()));

    // Check interface types
    assertThat(port1.getInterfaceType(), equalTo(InterfaceType.PHYSICAL));
    assertThat(port2.getInterfaceType(), equalTo(InterfaceType.LOGICAL));
    assertThat(longName.getInterfaceType(), equalTo(InterfaceType.LOGICAL));
    assertThat(tunnel.getInterfaceType(), equalTo(InterfaceType.TUNNEL));
    assertThat(loopback.getInterfaceType(), equalTo(InterfaceType.LOOPBACK));
    assertThat(vlan.getInterfaceType(), equalTo(InterfaceType.LOGICAL));

    // Check MTUs
    assertThat(port2.getMtu(), equalTo(1234));
    Stream.of(port1, longName, tunnel, loopback, vlan)
        .forEach(iface -> assertThat(iface.getMtu(), equalTo(Interface.DEFAULT_INTERFACE_MTU)));

    // Check speeds and bandwidths
    assertThat(port1, allOf(hasSpeed(10e9), hasBandwidth(10e9)));
    assertThat(port2, allOf(hasSpeed(10e9), hasBandwidth(10e9)));
    assertThat(longName, allOf(hasSpeed(100e9), hasBandwidth(100e9)));

    // Check aliases
    assertThat(port1.getDeclaredNames(), contains("longest possibl alias str"));
    assertThat(port2.getDeclaredNames(), contains("no_spaces"));
    assertThat(longName.getDeclaredNames(), contains(""));
    Stream.of(tunnel, loopback, vlan)
        .forEach(iface -> assertThat(iface.getDeclaredNames(), empty()));

    // Check descriptions
    assertThat(port1.getDescription(), equalTo("quoted description w/ spaces and more"));
    assertThat(port2.getDescription(), equalTo("no_spaces_descr"));
    Stream.of(longName, tunnel, loopback, vlan)
        .forEach(iface -> assertNull(iface.getDescription()));

    // Check VLAN properties and dependencies
    Dependency port1Dependency = new Dependency("port1", DependencyType.BIND);
    assertThat(port2.getEncapsulationVlan(), equalTo(1236));
    assertThat(port2.getDependencies(), contains(port1Dependency));
    assertThat(longName.getEncapsulationVlan(), equalTo(1235));
    assertThat(longName.getDependencies(), contains(port1Dependency));
    assertThat(vlan.getEncapsulationVlan(), equalTo(4094));
    assertThat(vlan.getDependencies(), contains(port1Dependency));
  }

  @Test
  public void testInterfaceWarnings() throws IOException {
    String hostname = "iface_warn";
    FortiosConfiguration vc = parseVendorConfig(hostname);
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    Warnings parseWarnings =
        getOnlyElement(
            batfish
                .loadParseVendorConfigurationAnswerElement(batfish.getSnapshot())
                .getWarnings()
                .values());
    assertThat(
        parseWarnings,
        hasParseWarnings(
            containsInAnyOrder(
                allOf(
                    hasComment("Illegal value for interface name"),
                    hasText(containsString("name is too long for iface"))),
                allOf(
                    hasComment("Illegal value for interface alias"),
                    hasText(containsString("alias string is too long to associate with iface"))),
                hasComment("Interface edit block ignored: name conflicts with a zone name"),
                hasComment("Expected vlanid in range 1-4094, but got '4095'"),
                hasComment("Interface iface_undefined is undefined"),
                allOf(
                    hasComment("The type of an interface cannot be changed"),
                    hasText(containsString("type loopback"))),
                hasComment("Interface edit block ignored: name is invalid"),
                hasComment("Interface edit block ignored: vlanid must be set"),
                hasComment("Interface edit block ignored: interface must be set"),
                hasComment(
                    "Secondaryip edit block ignored: cannot configure a secondaryip when"
                        + " secondary-IP is not enabled"),
                hasComment(
                    "Primary ip address must be configured before a secondaryip can be configured"),
                hasComment("Secondaryip edit block ignored: name is invalid"),
                hasComment(
                    "Expected secondaryip number in range 0-4294967295, but got '4294967296'"),
                allOf(
                    hasComment(
                        "This secondaryip will be ignored; must use a secondaryip other than the"
                            + " primary ip address"),
                    hasText("ip 10.0.0.1/30")),
                allOf(
                    hasComment(
                        "This secondaryip will be ignored; must use a secondaryip other than"
                            + " existing secondaryip addresses"),
                    hasText("ip 10.0.0.2/30")))));

    // Also check extraction to make sure the conflicting-name lines are discarded, i.e. no VS
    // object is created when the name conflicts
    assertThat(vc.getInterfaces(), hasKeys("port1", "vlan1", "secondary"));
  }

  // Test for github.com/batfish/batfish/issues/6995
  @Test
  public void testAggregateInterfaceExtraction() {
    String hostname = "iface_aggregate";
    FortiosConfiguration vc = parseVendorConfig(hostname);

    Map<String, Interface> ifaces = vc.getInterfaces();
    assertThat(
        ifaces.keySet(),
        containsInAnyOrder("port1", "port2", "port3", "Uplink", "redundant1", "test_wan_vlan"));

    Interface port1 = ifaces.get("port1");
    Interface port2 = ifaces.get("port2");
    Interface port3 = ifaces.get("port3");
    Interface uplink = ifaces.get("Uplink");
    Interface redundant1 = ifaces.get("redundant1");
    Interface wan_vlan = ifaces.get("test_wan_vlan");

    assertThat(port1.getVdom(), equalTo("root"));
    assertThat(port1.getType(), equalTo(Type.PHYSICAL));

    assertThat(port2.getVdom(), equalTo("root"));
    assertThat(port2.getType(), equalTo(Type.PHYSICAL));

    assertThat(port3.getVdom(), equalTo("root"));
    assertThat(port3.getType(), equalTo(Type.PHYSICAL));

    assertThat(uplink.getDescription(), equalTo("Uplink to DC switches"));
    assertThat(uplink.getType(), equalTo(Type.AGGREGATE));
    // test aggregate members
    assertThat(uplink.getMembers(), containsInAnyOrder("port1", "port2"));

    assertThat(redundant1.getType(), equalTo(Type.REDUNDANT));
    assertThat(redundant1.getMembers(), containsInAnyOrder("port3"));

    assertThat(wan_vlan.getVdom(), equalTo("test"));
    assertThat(wan_vlan.getIp(), equalTo(ConcreteInterfaceAddress.parse("10.10.1.1/24")));
    assertThat(wan_vlan.getType(), equalTo(null));
    assertThat(wan_vlan.getTypeEffective(), equalTo(Type.VLAN));
    assertThat(wan_vlan.getInterface(), equalTo("Uplink"));
    assertThat(wan_vlan.getVlanid(), equalTo(10));
  }

  // Test for github.com/batfish/batfish/issues/6995
  @Test
  public void testAggregateInterfaceConversion() throws IOException {
    String hostname = "iface_aggregate";
    Batfish batfish = getBatfishForConfigurationNames(hostname);

    Configuration c = batfish.loadConfigurations(batfish.getSnapshot()).get(hostname);
    Map<String, org.batfish.datamodel.Interface> ifaces = c.getAllInterfaces();
    assertThat(ifaces, hasKeys("port1", "port2", "port3", "Uplink", "redundant1", "test_wan_vlan"));
    org.batfish.datamodel.Interface port1 = ifaces.get("port1");
    org.batfish.datamodel.Interface port2 = ifaces.get("port2");
    org.batfish.datamodel.Interface port3 = ifaces.get("port3");
    org.batfish.datamodel.Interface uplink = ifaces.get("Uplink");
    org.batfish.datamodel.Interface redundant = ifaces.get("redundant1");
    org.batfish.datamodel.Interface test_wan_vlan = ifaces.get("test_wan_vlan");

    // Check addresses
    assertThat(test_wan_vlan, hasAddress("10.10.1.1/24"));
    assertThat(port1, hasAddress(nullValue()));
    assertThat(port2, hasAddress(nullValue()));
    assertThat(uplink, hasAddress(nullValue()));

    // Check interface types
    assertThat(port1, hasInterfaceType(InterfaceType.PHYSICAL));
    assertThat(port2, hasInterfaceType(InterfaceType.PHYSICAL));
    assertThat(port3, hasInterfaceType(InterfaceType.PHYSICAL));
    assertThat(uplink, hasInterfaceType(InterfaceType.AGGREGATED));
    assertThat(redundant, hasInterfaceType(InterfaceType.REDUNDANT));
    assertThat(test_wan_vlan, hasInterfaceType(InterfaceType.LOGICAL));

    // Check aliases
    Stream.of(port1, port2, port3, uplink, redundant, test_wan_vlan)
        .forEach(iface -> assertThat(iface.getDeclaredNames(), empty()));

    // Check descriptions
    assertThat(uplink.getDescription(), equalTo("Uplink to DC switches"));
    Stream.of(port1, port2, port3, redundant, test_wan_vlan)
        .forEach(iface -> assertThat(iface, hasDescription(nullValue())));

    // Check VLAN properties
    assertThat(test_wan_vlan.getEncapsulationVlan(), equalTo(10));

    // Dependencies
    Dependency port1Dependency = new Dependency("port1", DependencyType.AGGREGATE);
    Dependency port2Dependency = new Dependency("port2", DependencyType.AGGREGATE);
    Dependency port3Dependency = new Dependency("port3", DependencyType.AGGREGATE);
    Dependency uplinkDependency = new Dependency("Uplink", DependencyType.BIND);
    assertThat(uplink.getDependencies(), containsInAnyOrder(port1Dependency, port2Dependency));
    assertThat(redundant.getDependencies(), contains(port3Dependency));
    assertThat(test_wan_vlan.getDependencies(), contains(uplinkDependency));

    // Aggregated bandwidths
    assertThat(port1, hasSpeed(10e9));
    assertThat(port1, hasBandwidth(10e9));
    assertThat(uplink, hasBandwidth(20e9));
    assertThat(redundant, hasSpeed(10e9));
  }

  // Test for github.com/batfish/batfish/issues/6995
  @Test
  public void testAggregateInferfaceWarning() throws IOException {
    // https://docs.fortinet.com/document/fortigate/7.6.2/administration-guide/567758/aggregation-and-redundancy
    // An interface is available to be an aggregate/redondante interface if:
    // - It is a physical interface and not a VLAN interface or subinterface.
    // - It is not already part of an aggregate or redundant interface.
    // - It is in the same VDOM as the aggregated interface. Aggregate ports cannot span multiple
    // VDOMs.
    // - It does not have an IP address and is not configured for DHCP or PPPoE.
    String hostname = "iface_aggregate_warn";
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    Warnings parseWarnings =
        getOnlyElement(
            batfish
                .loadParseVendorConfigurationAnswerElement(batfish.getSnapshot())
                .getWarnings()
                .values());
    assertThat(
        parseWarnings,
        hasParseWarnings(
            containsInAnyOrder(
                allOf(
                    hasComment("Interface is undefined"),
                    hasText(containsString("iface_undefined"))),
                allOf(
                    hasComment("Interface is already in another aggregate/redundant interface"),
                    hasText(containsString("port1"))),
                allOf(
                    hasComment("Only physical interfaces are allowed"),
                    hasText(containsString("vlan"))),
                allOf(
                    hasComment(
                        "Interface is in a different vdom (customer1) than the current interface"
                            + " (root)"),
                    hasText(containsString("port4"))),
                hasComment("Interface has an IP address (192.168.1.1/24)"))));
  }

  @Test
  public void testZoneExtraction() {
    String hostname = "zone";
    FortiosConfiguration vc = parseVendorConfig(hostname);

    Map<String, Zone> zones = vc.getZones();
    assertThat(
        zones.keySet(),
        containsInAnyOrder("zone1", "zone2", "longest possible valid name for zon"));

    Zone zone1 = zones.get("zone1");
    Zone zone2 = zones.get("zone2");
    Zone zoneLongName = zones.get("longest possible valid name for zon");

    assertThat(zone1.getInterface(), containsInAnyOrder("port1", "port2"));
    // Defaults
    assertThat(zone1.getIntrazone(), nullValue());
    assertThat(zone1.getIntrazoneEffective(), equalTo(Zone.DEFAULT_INTRAZONE_ACTION));
    assertThat(zone1.getDescription(), nullValue());

    assertThat(zone2.getDescription(), equalTo("zone2 description"));
    assertThat(zone2.getIntrazone(), equalTo(IntrazoneAction.DENY));
    assertThat(zone2.getIntrazoneEffective(), equalTo(IntrazoneAction.DENY));
    assertThat(zone2.getInterface(), contains("port3"));

    assertThat(zoneLongName.getIntrazone(), equalTo(IntrazoneAction.ALLOW));
    assertThat(zoneLongName.getIntrazoneEffective(), equalTo(IntrazoneAction.ALLOW));
    assertThat(zoneLongName.getInterface(), containsInAnyOrder("port4", "port5"));
  }

  @Test
  public void testZoneConversion() throws IOException {
    String hostname = "zone";
    Configuration c = parseConfig(hostname);

    Map<String, org.batfish.datamodel.Zone> zones = c.getZones();
    assertThat(
        zones.keySet(),
        containsInAnyOrder("zone1", "zone2", "longest possible valid name for zon"));

    org.batfish.datamodel.Zone zone1 = zones.get("zone1");
    org.batfish.datamodel.Zone zone2 = zones.get("zone2");
    org.batfish.datamodel.Zone zoneLongName = zones.get("longest possible valid name for zon");

    assertThat(zone1.getInterfaces(), containsInAnyOrder("port1", "port2"));
    assertThat(zone2.getInterfaces(), contains("port3"));
    assertThat(zoneLongName.getInterfaces(), containsInAnyOrder("port4", "port5"));

    // Ensure that all zones' interfaces know what they're in
    Stream.of(zone1, zone2, zoneLongName)
        .forEach(
            zone ->
                zone.getInterfaces()
                    .forEach(
                        zoneIface ->
                            assertThat(
                                c.getAllInterfaces().get(zoneIface).getZoneName(),
                                equalTo(zone.getName()))));
  }

  @Test
  public void testZoneWarnings() throws IOException {
    String hostname = "zone_warn";

    FortiosConfiguration vc = parseVendorConfig(hostname);

    Batfish batfish = getBatfishForConfigurationNames(hostname);
    Warnings parseWarnings =
        getOnlyElement(
            batfish
                .loadParseVendorConfigurationAnswerElement(batfish.getSnapshot())
                .getWarnings()
                .values());
    assertThat(
        parseWarnings,
        hasParseWarnings(
            containsInAnyOrder(
                allOf(
                    hasComment("Zone edit block ignored: interface must be set"),
                    hasText(containsString("zone1"))),
                hasComment("Illegal value for zone name"),
                hasComment("Zone edit block ignored: name is invalid"),
                hasComment("Interface UNDEFINED is undefined and cannot be added to zone zone3"),
                hasComment(
                    "Interface port1 is already in another zone and cannot be added to zone"
                        + " zone4"),
                hasComment(
                    "Zone edit block ignored: name conflicts with a system interface name"))));

    // Also check extraction to make sure the right lines/blocks are discarded
    Map<String, Zone> zones = vc.getZones();
    assertThat(zones, hasKeys("zone3", "zone4"));

    Zone zone3 = zones.get("zone3");
    Zone zone4 = zones.get("zone4");

    assertThat(zone3.getInterface(), contains("port1"));
    // Only port2 should show up, since port1 is already taken
    assertThat(zone4.getInterface(), contains("port2"));
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
            "custom_tcp",
            "explicit_tcp",
            "src_port_defaults",
            "custom_icmp",
            "custom_icmp6",
            "custom_ip",
            "change_protocol",
            "unset_icmp_1",
            "unset_icmp_2",
            "unset_icmp_3"));

    Service serviceLongName =
        services.get(
            "longest possible firewall service custom service name that is accepted by devic");
    Service serviceTcpDefault = services.get("custom_tcp");
    Service serviceTcpExplicit = services.get("explicit_tcp");
    Service serviceSrcPortDefaults = services.get("src_port_defaults");
    Service serviceIcmp = services.get("custom_icmp");
    Service serviceIcmp6 = services.get("custom_icmp6");
    Service serviceIp = services.get("custom_ip");
    Service serviceChangeProtocol = services.get("change_protocol");
    Service serviceUnsetIcmp1 = services.get("unset_icmp_1");
    Service serviceUnsetIcmp2 = services.get("unset_icmp_2");
    Service serviceUnsetIcmp3 = services.get("unset_icmp_3");

    assertThat(serviceLongName.getComment(), equalTo("service custom comment"));

    // Check default protocol
    assertThat(serviceTcpDefault.getProtocol(), nullValue());
    assertThat(serviceTcpDefault.getProtocolEffective(), equalTo(Service.DEFAULT_PROTOCOL));
    // Check defaults
    assertThat(serviceTcpDefault.getSctpPortRangeDst(), nullValue());
    assertThat(serviceTcpDefault.getSctpPortRangeSrc(), nullValue());
    assertThat(serviceTcpDefault.getTcpPortRangeSrc(), nullValue());
    assertThat(serviceTcpDefault.getUdpPortRangeDst(), nullValue());
    assertThat(serviceTcpDefault.getUdpPortRangeSrc(), nullValue());

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

    assertThat(serviceTcpExplicit.getProtocol(), equalTo(Protocol.TCP_UDP_SCTP));
    assertThat(serviceTcpExplicit.getProtocolEffective(), equalTo(Protocol.TCP_UDP_SCTP));
    // Check variety of port range syntax
    // TCP
    assertThat(serviceTcpDefault.getTcpPortRangeDst(), equalTo(IntegerSpace.of(1)));
    assertThat(
        serviceTcpExplicit.getTcpPortRangeDst(),
        equalTo(IntegerSpace.builder().including(1, 2, 10, 11, 13).build()));
    assertThat(
        serviceTcpExplicit.getTcpPortRangeSrc(),
        equalTo(IntegerSpace.builder().including(3, 4, 6, 7).build()));
    // UDP
    assertThat(
        serviceTcpExplicit.getUdpPortRangeDst(),
        equalTo(IntegerSpace.builder().including(100).build()));
    assertThat(serviceTcpExplicit.getUdpPortRangeSrc(), nullValue());
    // SCTP
    assertThat(
        serviceTcpExplicit.getSctpPortRangeDst(),
        equalTo(IntegerSpace.builder().including(200, 201).build()));
    assertThat(
        serviceTcpExplicit.getSctpPortRangeSrc(),
        equalTo(IntegerSpace.builder().including(300).build()));

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

    // Unsetting icmpcode works
    assertThat(serviceUnsetIcmp1.getIcmpType(), equalTo(5));
    assertThat(serviceUnsetIcmp1.getIcmpCode(), nullValue());
    // Unsetting icmptype clears code too, even if type is reset
    assertThat(serviceUnsetIcmp2.getIcmpType(), equalTo(3));
    assertThat(serviceUnsetIcmp2.getIcmpCode(), nullValue());
    // Unsetting icmptype clears code too
    assertThat(serviceUnsetIcmp3.getIcmpType(), nullValue());
    assertThat(serviceUnsetIcmp3.getIcmpCode(), nullValue());
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
                allOf(
                    hasComment("Service edit block ignored: name is invalid"),
                    hasText(
                        containsString(
                            "edit \"longer than longest possible firewall service custom service"
                                + " name that is accepted by device\""))),
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
                        + " protocol is not set to TCP/UDP/SCTP."),
                hasComment(
                    "Cannot set ICMP code for service icmp code before type when ICMP type is not"
                        + " set."))));
  }

  @Test
  public void testServiceGroupExtraction() {
    String hostname = "service_group";
    FortiosConfiguration vc = parseVendorConfig(hostname);

    Map<String, ServiceGroup> serviceGroups = vc.getServiceGroups();
    assertThat(
        serviceGroups.keySet(),
        containsInAnyOrder(
            "this is longest possible firewall service group name that is accepted by device",
            "grp1",
            "grp2"));

    ServiceGroup grpLongName =
        serviceGroups.get(
            "this is longest possible firewall service group name that is accepted by device");
    ServiceGroup grp1 = serviceGroups.get("grp1");
    ServiceGroup grp2 = serviceGroups.get("grp2");

    assertThat(grpLongName.getComment(), equalTo("service group comment"));

    assertThat(grp1.getMember(), contains("custom_tcp3"));
    assertNull(grp1.getComment());
    assertThat(grp2.getMember(), contains("custom_tcp3", "grp1"));
    assertNull(grp2.getComment());
  }

  @Test
  public void testServiceGroupWarnings() throws IOException {
    String hostname = "service_group_warnings";

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
                allOf(
                    hasComment("Service group edit block ignored: name is invalid"),
                    hasText(
                        containsString(
                            "longer than longest possible firewall service group name that is"
                                + " accepted by the device"))),
                hasComment(
                    "Service group edit block ignored: service group requires at least one member"),
                hasComment(
                    "Service group self_ref_not_allowed cannot be added to valid as it would"
                        + " create a cycle"),
                hasComment(
                    "Service group valid cannot be added to valid as it would"
                        + " create a cycle"))));
  }

  @Test
  public void testServiceGroupRename() throws IOException {
    String hostname = "service_group_rename";
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    FortiosConfiguration vc =
        (FortiosConfiguration)
            batfish.loadVendorConfigurations(batfish.getSnapshot()).get(hostname);

    assertThat(vc.getPolicies(), hasKeys("0"));
    assertThat(vc.getServiceGroups(), hasKeys("new_group1", "new_group2"));

    Policy policy = vc.getPolicies().get("0");
    // Policy should be using renamed structures
    // Whether or not they were renamed after initial reference
    assertThat(policy.getService(), containsInAnyOrder("new_group1", "new_group2"));
  }

  @Test
  public void testServiceGroupRenameWarnings() throws IOException {
    String hostname = "service_group_rename";
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    Warnings warnings =
        getOnlyElement(
            batfish
                .loadParseVendorConfigurationAnswerElement(batfish.getSnapshot())
                .getWarnings()
                .values());

    // Should get warnings when trying to use a an old structure name
    // Or trying to use an undefined structure that will be defined (renamed) later
    assertThat(
        warnings,
        hasParseWarnings(
            containsInAnyOrder(
                hasComment(
                    "Service or service group old_group1 is undefined and cannot be referenced"),
                hasComment(
                    "Service or service group new_group2 is undefined and cannot be referenced"),
                hasComment("Cannot rename non-existent service group undefined"),
                hasComment(
                    "Renaming service group new_group1 conflicts with an existing object"
                        + " new_group2, ignoring this rename operation"),
                hasComment(
                    "Renaming service group new_group1 conflicts with an existing object service1,"
                        + " ignoring this rename operation"),
                hasComment(
                    "Renaming service custom service1 conflicts with an existing object"
                        + " new_group1, ignoring this rename operation"),
                allOf(
                    hasComment("Illegal value for service name"),
                    hasText(
                        containsString(
                            "a name that very very very very very very very long and is too long"
                                + " to use for this object type"))),
                hasComment("Policy edit block ignored: service must be set"))));
  }

  @Test
  public void testServiceGroupRenameReferences() throws IOException {
    String hostname = "service_group_rename";
    String filename = "configs/" + hostname;

    Batfish batfish = getBatfishForConfigurationNames(hostname);
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());

    // Should have defs for the renamed structures and rename should be part of the defs
    assertThat(
        ccae,
        hasDefinedStructureWithDefinitionLines(
            filename, FortiosStructureType.SERVICE_GROUP, "new_group1", contains(16, 17, 18, 23)));
    assertThat(
        ccae,
        hasDefinedStructureWithDefinitionLines(
            filename, FortiosStructureType.SERVICE_GROUP, "new_group2", contains(19, 20, 21, 47)));

    // Should have references for the renamed structures, even if the renaming happened after the
    // reference
    assertThat(
        ccae, hasNumReferrers(filename, FortiosStructureType.SERVICE_GROUP, "new_group1", 1));
    assertThat(
        ccae, hasNumReferrers(filename, FortiosStructureType.SERVICE_GROUP, "new_group2", 1));

    // Should have undefined references where either:
    //   1. New names are used before the structure is renamed
    //   2. Old names are used after the structure is renamed
    assertThat(
        ccae,
        hasUndefinedReference(
            filename, FortiosStructureType.SERVICE_CUSTOM_OR_SERVICE_GROUP, "old_group1"));
    assertThat(
        ccae,
        hasUndefinedReference(
            filename, FortiosStructureType.SERVICE_CUSTOM_OR_SERVICE_GROUP, "new_group2"));
  }

  @Test
  public void testAddrgrpRename() throws IOException {
    String hostname = "addrgrp_rename";
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    FortiosConfiguration vc =
        (FortiosConfiguration)
            batfish.loadVendorConfigurations(batfish.getSnapshot()).get(hostname);

    assertThat(vc.getPolicies(), hasKeys("0"));
    assertThat(vc.getAddrgrps(), hasKeys("new_group1", "new_group2"));

    Policy policy = vc.getPolicies().get("0");
    // Policy should be using renamed structures
    // Whether or not they were renamed after initial reference
    assertThat(policy.getSrcAddr(), contains("new_group1"));
    assertThat(policy.getDstAddr(), contains("new_group2"));
  }

  @Test
  public void testAddrgrpRenameWarnings() throws IOException {
    String hostname = "addrgrp_rename";
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    Warnings warnings =
        getOnlyElement(
            batfish
                .loadParseVendorConfigurationAnswerElement(batfish.getSnapshot())
                .getWarnings()
                .values());

    // Should get warnings when trying to use a an old structure name
    // Or trying to use an undefined structure that will be defined (renamed) later
    assertThat(
        warnings,
        hasParseWarnings(
            containsInAnyOrder(
                hasComment("Address or addrgrp old_group1 is undefined and cannot be referenced"),
                hasComment("Address or addrgrp new_group2 is undefined and cannot be referenced"),
                hasComment("Cannot rename non-existent addrgrp undefined"),
                hasComment(
                    "Renaming addrgrp new_group1 conflicts with an existing object"
                        + " new_group2, ignoring this rename operation"),
                hasComment(
                    "Renaming addrgrp new_group1 conflicts with an existing object addr1,"
                        + " ignoring this rename operation"),
                hasComment(
                    "Renaming address addr1 conflicts with an existing object"
                        + " new_group1, ignoring this rename operation"),
                allOf(
                    hasComment("Illegal value for address name"),
                    hasText(
                        containsString(
                            "a name that is very very very very very very very long and is too long"
                                + " to use for this object type"))),
                hasComment("Policy edit block ignored: srcaddr must be set"))));
  }

  @Test
  public void testAddrgrpRenameReferences() throws IOException {
    String hostname = "addrgrp_rename";
    String filename = "configs/" + hostname;

    Batfish batfish = getBatfishForConfigurationNames(hostname);
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());

    // Should have defs for the renamed structures and rename should be part of the defs
    assertThat(
        ccae,
        hasDefinedStructureWithDefinitionLines(
            filename, FortiosStructureType.ADDRGRP, "new_group1", contains(15, 16, 17, 22)));
    assertThat(
        ccae,
        hasDefinedStructureWithDefinitionLines(
            filename, FortiosStructureType.ADDRGRP, "new_group2", contains(18, 19, 20, 50)));

    // Should have references for the renamed structures, even if the renaming happened after the
    // reference
    assertThat(ccae, hasNumReferrers(filename, FortiosStructureType.ADDRGRP, "new_group1", 1));
    assertThat(ccae, hasNumReferrers(filename, FortiosStructureType.ADDRGRP, "new_group2", 1));

    // Should have undefined references where either:
    //   1. New names are used before the structure is renamed
    //   2. Old names are used after the structure is renamed
    assertThat(
        ccae,
        hasUndefinedReference(filename, FortiosStructureType.ADDRESS_OR_ADDRGRP, "old_group1"));
    assertThat(
        ccae,
        hasUndefinedReference(filename, FortiosStructureType.ADDRESS_OR_ADDRGRP, "new_group2"));
  }

  @Test
  public void testFirewallPolicyExtraction() {
    String hostname = "firewall_policy";
    FortiosConfiguration vc = parseVendorConfig(hostname);

    Map<String, Policy> policies = vc.getPolicies();
    assertThat(policies, hasKeys(contains("0", "4294967294", "1", "2", "3")));
    Policy policyDisable = policies.get("0");
    Policy policyDeny = policies.get("4294967294");
    Policy policyAllow = policies.get("1");
    Policy policyAny = policies.get("2");
    Policy policyZone = policies.get("3");

    Map<String, Service> services = vc.getServices();
    String service11 = "custom_tcp_11";
    String service12From11 = "custom_tcp_12_from_11";
    String serviceAll = "ALL";
    assertThat(services, hasKeys(containsInAnyOrder(service11, service12From11, serviceAll)));

    Map<String, Address> addresses = vc.getAddresses();
    String addr1 = "addr1";
    String addr2 = "addr2";
    String addrAll = "all";
    assertThat(addresses, hasKeys(containsInAnyOrder(addr1, addr2, addrAll)));

    Map<String, Interface> interfaces = vc.getInterfaces();
    String port1 = "port1";
    String port2 = "port2";
    String port3 = "port3";
    String port4 = "port4";
    String port5 = "port5";
    assertThat(interfaces, hasKeys(containsInAnyOrder(port1, port2, port3, port4, port5)));

    Map<String, Zone> zones = vc.getZones();
    String zone1 = "zone1";
    String zone2 = "zone2";
    String zone3 = "zone3";
    assertThat(zones, hasKeys(containsInAnyOrder(zone1, zone2, zone3)));

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
    assertThat(policyDeny.getService(), contains(service12From11));
    assertThat(policyDeny.getSrcIntf(), contains(port1));
    assertThat(policyDeny.getDstIntf(), containsInAnyOrder(port1, port2));
    assertThat(policyDeny.getSrcAddr(), contains(addr1));
    assertThat(policyDeny.getDstAddr(), contains(addr2));

    assertThat(policyAllow.getAction(), equalTo(Action.ACCEPT));
    assertThat(policyAllow.getStatus(), equalTo(Policy.Status.ENABLE));
    assertThat(policyAllow.getStatusEffective(), equalTo(Policy.Status.ENABLE));
    assertThat(policyAllow.getService(), containsInAnyOrder(service11, service12From11));
    assertThat(policyAllow.getSrcIntf(), containsInAnyOrder(port1, port2));
    assertThat(policyAllow.getDstIntf(), containsInAnyOrder(port1, port2));
    assertThat(policyAllow.getSrcAddr(), containsInAnyOrder(addr1, addr2));
    assertThat(policyAllow.getDstAddr(), containsInAnyOrder(addr1, addr2));

    assertThat(policyAny.getService(), contains(serviceAll));
    assertThat(policyAny.getSrcAddr(), contains(addrAll));
    assertThat(policyAny.getDstAddr(), contains(addrAll));
    assertThat(policyAny.getSrcIntf(), contains(Policy.ANY_INTERFACE));
    assertThat(policyAny.getDstIntf(), contains(Policy.ANY_INTERFACE));

    assertThat(policyZone.getSrcIntfZones(), containsInAnyOrder(zone1, zone2, zone3));
    assertThat(policyZone.getSrcIntf(), contains(port2));
    assertThat(policyZone.getDstIntfZones(), containsInAnyOrder(zone1, zone3));
  }

  @Test
  public void testFirewallPolicyConversion() {
    String hostname = "firewall_policy";
    FortiosConfiguration vc = parseVendorConfig(hostname);

    // Policy 0 should not be converted because it's disabled
    String denyName = "4294967294";
    String allowName = "1";
    String anyName = "2";
    String zonePolicyName = "3";

    Map<String, AclLine> convertedPolicies = vc.getConvertedPolicies(vc.getAddresses().keySet());
    assertThat(convertedPolicies, hasKeys(denyName, allowName, anyName, zonePolicyName));

    AclLine deny = convertedPolicies.get(denyName);
    AclLine allow = convertedPolicies.get(allowName);
    AclLine any = convertedPolicies.get(anyName);
    AclLine zonePolicy = convertedPolicies.get(zonePolicyName);

    assertThat(
        deny.getName(), equalTo(getPolicyName(denyName, "longest allowed firewall policy nam")));
    assertThat(allow.getName(), equalTo(getPolicyName(allowName, "Permit Custom TCP Traffic")));
    assertThat(any.getName(), equalTo(getPolicyName(anyName, null)));
    assertThat(zonePolicy.getName(), equalTo(getPolicyName(zonePolicyName, null)));

    // Create IpAccessListToBdd to convert ACLs.
    Map<String, IpSpace> namedIpSpaces =
        ImmutableMap.of(
            "addr1",
            Prefix.parse("10.0.1.0/24").toIpSpace(),
            "addr2",
            Prefix.parse("10.0.2.0/24").toIpSpace(),
            "all",
            UniverseIpSpace.INSTANCE);
    IpAccessListToBdd aclToBdd =
        new IpAccessListToBddImpl(
            _pkt, BDDSourceManager.empty(_pkt), ImmutableMap.of(), namedIpSpaces);

    // Make BDDs representing components of defined policies
    BDD addr1AsSrc = _srcIpBdd.toBDD(Prefix.parse("10.0.1.0/24"));
    BDD addr2AsSrc = _srcIpBdd.toBDD(Prefix.parse("10.0.2.0/24"));
    BDD addr1AsDst = _dstIpBdd.toBDD(Prefix.parse("10.0.1.0/24"));
    BDD addr2AsDst = _dstIpBdd.toBDD(Prefix.parse("10.0.2.0/24"));
    BDD service11 =
        _bddTestbed.toBDD(
            HeaderSpace.builder()
                .setIpProtocols(IpProtocol.TCP)
                .setSrcPorts(Service.DEFAULT_SOURCE_PORT_RANGE.getSubRanges())
                .setDstPorts(SubRange.singleton(11))
                .build());
    BDD service12From11 =
        _bddTestbed.toBDD(
            HeaderSpace.builder()
                .setIpProtocols(IpProtocol.TCP)
                .setSrcPorts(SubRange.singleton(11))
                .setDstPorts(SubRange.singleton(12))
                .build());
    {
      // Deny service custom_tcp_12_from_11 from addr1 to addr2
      PermitAndDenyBdds expected =
          new PermitAndDenyBdds(_zero, addr1AsSrc.and(addr2AsDst).and(service12From11));
      assertThat(aclToBdd.toPermitAndDenyBdds(deny), equalTo(expected));
    }
    {
      // Allow services custom_tcp_11, custom_tcp_11_from_12 from addr1, addr2 to addr1, addr2
      BDD services = service11.or(service12From11);
      BDD srcAddrs = addr1AsSrc.or(addr2AsSrc);
      BDD dstAddrs = addr1AsDst.or(addr2AsDst);
      PermitAndDenyBdds expected =
          new PermitAndDenyBdds(services.and(srcAddrs).and(dstAddrs), _zero);
      assertThat(aclToBdd.toPermitAndDenyBdds(allow), equalTo(expected));
    }
    {
      // Allow all
      PermitAndDenyBdds expected = new PermitAndDenyBdds(_one, _zero);
      assertThat(aclToBdd.toPermitAndDenyBdds(any), equalTo(expected));
      assertThat(aclToBdd.toPermitAndDenyBdds(zonePolicy), equalTo(expected));
    }
  }

  @Test
  public void testFirewallPolicyAddrgrpConversion() {
    String hostname = "firewall_policy_addrgrp";
    FortiosConfiguration vc = parseVendorConfig(hostname);

    String policyName = "1";

    Map<String, AclLine> convertedPolicies =
        vc.getConvertedPolicies(
            ImmutableSet.<String>builder()
                .addAll(vc.getAddresses().keySet())
                .addAll(vc.getAddrgrps().keySet())
                .build());
    assertThat(convertedPolicies, hasKeys(policyName));

    AclLine policyAclLine = convertedPolicies.get(policyName);

    // Create IpAccessListToBdd to convert ACLs.
    Map<String, IpSpace> namedIpSpaces =
        ImmutableMap.<String, IpSpace>builder()
            .put("addr1", Prefix.parse("10.0.1.0/24").toIpSpace())
            .put("addr1b", Prefix.parse("10.0.1.64/29").toIpSpace())
            .put("addr2", Prefix.parse("10.0.2.0/24").toIpSpace())
            .put("all", UniverseIpSpace.INSTANCE)
            .put(
                "grp1_parent",
                AclIpSpace.builder()
                    .thenRejecting(new IpSpaceReference("addr1b"))
                    .thenPermitting(new IpSpaceReference("addr1"))
                    .build())
            .put("grp2", AclIpSpace.builder().thenPermitting(new IpSpaceReference("addr2")).build())
            .build();
    IpAccessListToBdd aclToBdd =
        new IpAccessListToBddImpl(
            _pkt, BDDSourceManager.empty(_pkt), ImmutableMap.of(), namedIpSpaces);

    // Make BDDs representing components of defined policy
    BDD addr1AsSrc = _srcIpBdd.toBDD(Prefix.parse("10.0.1.0/24"));
    BDD addr1bAsSrc = _srcIpBdd.toBDD(Prefix.parse("10.0.1.64/29"));
    BDD addr2AsDst = _dstIpBdd.toBDD(Prefix.parse("10.0.2.0/24"));
    BDD service11 =
        _bddTestbed.toBDD(
            HeaderSpace.builder()
                .setIpProtocols(IpProtocol.TCP)
                .setSrcPorts(Service.DEFAULT_SOURCE_PORT_RANGE.getSubRanges())
                .setDstPorts(SubRange.singleton(11))
                .build());

    BDD srcAddrs = addr1AsSrc.diff(addr1bAsSrc);
    PermitAndDenyBdds expected =
        new PermitAndDenyBdds(service11.and(srcAddrs).and(addr2AsDst), _zero);
    assertThat(aclToBdd.toPermitAndDenyBdds(policyAclLine), equalTo(expected));
  }

  @Test
  public void testViAcls() throws IOException {
    String hostname = "firewall_vi_policy";
    Configuration c = parseConfig(hostname);

    // Configuration contains unzoned interface port1 and zone zone1 containing port2 and port3
    String port1IntrazoneName = Names.zoneToZoneFilter("port1", "port1");
    String zone1IntrazoneName = Names.zoneToZoneFilter("zone1", "zone1");
    String port1ToZone1Name = Names.zoneToZoneFilter("port1", "zone1");
    String zone1ToPort1Name = Names.zoneToZoneFilter("zone1", "port1");
    String port1OutgoingName = computeOutgoingFilterName("interface", "port1");
    String zone1OutgoingName = computeOutgoingFilterName("zone", "zone1");

    Map<String, IpAccessList> acls = c.getIpAccessLists();
    assertThat(
        acls.keySet(),
        containsInAnyOrder(
            port1IntrazoneName,
            zone1IntrazoneName,
            port1ToZone1Name,
            zone1ToPort1Name,
            port1OutgoingName,
            zone1OutgoingName));

    IpAccessList port1Intrazone = acls.get(port1IntrazoneName);
    IpAccessList zone1Intrazone = acls.get(zone1IntrazoneName);
    IpAccessList port1ToZone1 = acls.get(port1ToZone1Name);
    IpAccessList zone1ToPort1 = acls.get(zone1ToPort1Name);
    IpAccessList port1Outgoing = acls.get(port1OutgoingName);
    IpAccessList zone1Outgoing = acls.get(zone1OutgoingName);

    // Create IpAccessListToBdd to convert ACLs.
    Prefix addr1 = Prefix.parse("10.0.1.0/24");
    Prefix addr2 = Prefix.parse("10.0.0.0/16");
    Map<String, IpSpace> namedIpSpaces =
        ImmutableMap.of(
            "addr1",
            addr1.toIpSpace(),
            "addr2",
            addr2.toIpSpace(),
            "all",
            UniverseIpSpace.INSTANCE);
    BDDSourceManager srcMgr =
        BDDSourceManager.forInterfaces(_pkt, c.getActiveInterfaces().keySet());
    IpAccessListToBdd aclToBdd = new IpAccessListToBddImpl(_pkt, srcMgr, acls, namedIpSpaces);

    // Make BDDs representing components of defined policies
    BDD addr1AsDst = _dstIpBdd.toBDD(addr1);
    BDD addr2AsDst = _dstIpBdd.toBDD(addr2);

    // No policies apply to traffic from port1 to port1
    assertThat(aclToBdd.toBdd(port1Intrazone), equalTo(_zero));

    // Only policy 2 applies to zone1 intrazone traffic
    assertThat(aclToBdd.toBdd(zone1Intrazone), equalTo(addr2AsDst));

    // Policy 1 denies 10.0.1.0/24, then policy 2 permits 10.0.0.0/16
    assertThat(aclToBdd.toBdd(port1ToZone1), equalTo(addr2AsDst.diff(addr1AsDst)));

    // No policies apply to traffic from zone1 to port1
    assertThat(aclToBdd.toBdd(zone1ToPort1), equalTo(_zero));

    // No policies apply to traffic leaving port1
    BDD originatedTraffic = srcMgr.getOriginatingFromDeviceBDD();
    assertThat(aclToBdd.toBdd(port1Outgoing), equalTo(originatedTraffic));

    // Should reflect that policy 1 blocks traffic from port1 to addr1, and that traffic from zone1
    // to addr2 is permitted
    BDD fromPort1 = srcMgr.getSourceInterfaceBDD("port1");
    BDD fromZone1 = srcMgr.getSourceInterfaceBDD("port2").or(srcMgr.getSourceInterfaceBDD("port3"));
    BDD permittedFromPort1 = fromPort1.and(addr2AsDst.diff(addr1AsDst));
    BDD permittedFromZone1 = fromZone1.and(addr2AsDst);
    assertThat(
        aclToBdd.toBdd(zone1Outgoing),
        equalTo(permittedFromPort1.or(permittedFromZone1).or(originatedTraffic)));
  }

  /**
   * Test extraction of firewall policy when warnings are generated for invalid / pruned properties.
   */
  @Test
  public void testFirewallPolicyExtractionWithWarnings() {
    String hostname = "firewall_policy_warn";
    FortiosConfiguration vc = parseVendorConfig(hostname);

    Map<String, Policy> policies = vc.getPolicies();
    assertThat(policies, hasKeys(containsInAnyOrder("1", "3")));
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
    Warnings parseWarnings =
        getOnlyElement(
            batfish
                .loadParseVendorConfigurationAnswerElement(batfish.getSnapshot())
                .getWarnings()
                .values());
    assertThat(
        parseWarnings,
        hasParseWarnings(
            containsInAnyOrder(
                hasComment("Expected policy number in range 0-4294967294, but got '4294967295'"),
                hasComment("Expected policy number in range 0-4294967294, but got 'not_a_number'"),
                hasComment("Illegal value for policy name"),
                allOf(
                    hasComment("Policy edit block ignored: name is invalid"),
                    hasText(containsString("4294967295"))),
                allOf(
                    hasComment("Policy edit block ignored: name is invalid"),
                    hasText(containsString("not_a_number"))),
                allOf(
                    hasComment("Policy edit block ignored: service must be set"),
                    hasText(containsString("edit 2"))),
                hasComment(
                    "Interface/zone port1 is undefined and cannot be added to policy 4294967295"),
                hasComment(
                    "Interface/zone port2 is undefined and cannot be added to policy 4294967295"),
                hasComment(
                    "Interface/zone port3 is undefined and cannot be added to policy 4294967295"),
                hasComment(
                    "Interface/zone port4 is undefined and cannot be added to policy 4294967295"),
                hasComment(
                    "Service or service group service1 is undefined and cannot be referenced"),
                hasComment(
                    "Service or service group service2 is undefined and cannot be referenced"),
                hasComment("Address or addrgrp addr1 is undefined and cannot be referenced"),
                hasComment("Address or addrgrp addr2 is undefined and cannot be referenced"),
                hasComment("Address or addrgrp addr3 is undefined and cannot be referenced"),
                hasComment("Address or addrgrp addr4 is undefined and cannot be referenced"),
                hasComment("Cannot combine 'ALL' with other services"),
                allOf(
                    hasComment("When 'all' is set together with other address(es), it is removed"),
                    hasText("addr10 all")),
                allOf(
                    hasComment("When 'all' is set together with other address(es), it is removed"),
                    hasText("addr20 all")),
                allOf(
                    hasComment("When 'any' is set together with other interfaces, it is removed"),
                    hasText("any port10")),
                hasComment("Cannot move a non-existent policy 99999"),
                hasComment("Cannot move around a non-existent policy 99999"),
                hasComment("Cannot clone a non-existent policy 99999"),
                allOf(
                    hasComment("Cannot create a cloned policy with an invalid name"),
                    hasText("clone 3 to foobar")),
                hasComment("Expected policy number in range 0-4294967294, but got 'foobar'"),
                hasComment("Cannot clone, policy 1 already exists"))));

    Warnings conversionWarnings =
        batfish
            .loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot())
            .getWarnings()
            .get(hostname);
    assertThat(
        conversionWarnings,
        hasRedFlags(
            contains(WarningMatchers.hasText("Ignoring policy 3: Action IPSEC is not supported"))));
  }

  @Test
  public void testStaticRouteExtraction() {
    String hostname = "static_routes";
    FortiosConfiguration vc = parseVendorConfig(hostname);

    assertThat(vc.getStaticRoutes(), hasKeys("0", "1", "2", "4294967295"));

    // All values explicitly configured
    StaticRoute r0 = vc.getStaticRoutes().get("0");
    assertThat(r0.getDevice(), equalTo("port1"));
    assertThat(r0.getDistance(), equalTo(20));
    assertThat(r0.getDst(), equalTo(Prefix.parse("1.1.1.0/24")));
    assertThat(r0.getGateway(), equalTo(Ip.parse("2.2.2.2")));
    assertThat(r0.getSdwanEnabled(), equalTo(false));
    assertThat(r0.getStatus(), equalTo(StaticRoute.Status.ENABLE));

    // All values default except device (which is required to be set)
    StaticRoute r1 = vc.getStaticRoutes().get("1");
    assertThat(r1.getDevice(), equalTo("port1"));
    assertNull(r1.getDistance());
    assertThat(r1.getDistanceEffective(), equalTo(StaticRoute.DEFAULT_DISTANCE));
    assertNull(r1.getDst());
    assertThat(r1.getDstEffective(), equalTo(StaticRoute.DEFAULT_DST));
    assertNull(r1.getGateway());
    assertNull(r1.getSdwanEnabled());
    assertFalse(r1.getSdwanEnabledEffective());
    assertNull(r1.getStatus());
    assertTrue(r1.getStatusEffective());

    // SD-WAN enabled; default distance should reflect that
    StaticRoute r2 = vc.getStaticRoutes().get("2");
    assertThat(r2.getSdwanEnabled(), equalTo(true));
    assertThat(r2.getDistanceEffective(), equalTo(StaticRoute.DEFAULT_DISTANCE_SDWAN));

    // Disabled (also max seq num)
    StaticRoute r4294967295 = vc.getStaticRoutes().get("4294967295");
    assertThat(r4294967295.getStatus(), equalTo(StaticRoute.Status.DISABLE));
  }

  @Test
  public void testStaticRouteConversion() throws IOException {
    String hostname = "static_routes";
    Configuration c = parseConfig(hostname);

    Set<org.batfish.datamodel.StaticRoute> staticRoutes =
        c.getVrfs().get(computeVrfName("root", Interface.DEFAULT_VRF)).getStaticRoutes();

    org.batfish.datamodel.StaticRoute expected0 =
        org.batfish.datamodel.StaticRoute.builder()
            .setAdmin(20)
            .setNetwork(Prefix.parse("1.1.1.0/24"))
            .setNextHop(NextHopInterface.of("port1", Ip.parse("2.2.2.2")))
            .build();
    org.batfish.datamodel.StaticRoute expected1 =
        org.batfish.datamodel.StaticRoute.builder()
            .setAdmin(StaticRoute.DEFAULT_DISTANCE)
            .setNetwork(Prefix.ZERO)
            .setNextHop(NextHopInterface.of("port1"))
            .build();
    org.batfish.datamodel.StaticRoute expected2 =
        org.batfish.datamodel.StaticRoute.builder()
            .setAdmin(StaticRoute.DEFAULT_DISTANCE_SDWAN)
            .setNetwork(Prefix.parse("2.2.2.0/24"))
            .setNextHop(NextHopInterface.of("port1"))
            .build();

    // Route 4294967295 doesn't get converted because it's disabled
    assertThat(staticRoutes, containsInAnyOrder(expected0, expected1, expected2));
  }

  @Test
  public void testStaticRouteWarnings() throws IOException {
    String hostname = "static_route_warnings";
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    Warnings parseWarnings =
        getOnlyElement(
            batfish
                .loadParseVendorConfigurationAnswerElement(batfish.getSnapshot())
                .getWarnings()
                .values());
    assertThat(
        parseWarnings,
        hasParseWarnings(
            containsInAnyOrder(
                hasComment(
                    "Expected static route sequence number in range 0-4294967295, but got"
                        + " '4294967296'"),
                hasComment(
                    "Expected static route sequence number in range 0-4294967295, but got"
                        + " 'not_a_number'"),
                allOf(
                    hasComment("Static route edit block ignored: sequence number is invalid"),
                    hasText(containsString("edit 4294967296"))),
                allOf(
                    hasComment("Static route edit block ignored: sequence number is invalid"),
                    hasText(containsString("edit not_a_number"))),
                // Route 1 tries to set device to port1 (undefined) and an invalid interface name
                hasComment("Interface port1 is undefined"),
                allOf(
                    hasComment("Static route edit block ignored: device must be set"),
                    hasText(containsString("edit 1"))))));

    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    assertThat(
        ccae,
        hasUndefinedReference(
            "configs/" + hostname,
            FortiosStructureType.INTERFACE,
            "port1",
            FortiosStructureUsage.STATIC_ROUTE_DEVICE));
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
  public void testNestedConfigRecovery() throws IOException {
    String hostname = "nested_config_recovery";
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    batfish.getSettings().setDisableUnrecognized(false);
    FortiosConfiguration vc =
        (FortiosConfiguration)
            batfish.loadVendorConfigurations(batfish.getSnapshot()).get(hostname);

    // Confirm unrecognized lines exist as expected
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
                hasText(containsString("set tags tag1")))));

    // Confirm config after unrecognized lines are still applied as expected
    // i.e. context is not lost
    assertThat(vc.getAddresses(), hasKeys("tagging"));
    Address addr = vc.getAddresses().get("tagging");
    assertThat(addr.getTypeEffective(), equalTo(Address.Type.IPMASK));
    assertThat(addr.getTypeSpecificFields().getIp1(), equalTo(Ip.parse("1.1.1.0")));
    assertThat(addr.getTypeSpecificFields().getIp2(), equalTo(Ip.parse("255.255.255.0")));
  }

  @Test
  public void testRouteMapExtraction() {
    String hostname = "route_map";
    FortiosConfiguration vc = parseVendorConfig(hostname);

    assertThat(vc.getRouteMaps(), hasKeys("longest_route_map_name_allowed_by_f", "route_map1"));
    RouteMap longName = vc.getRouteMaps().get("longest_route_map_name_allowed_by_f");
    RouteMap rm1 = vc.getRouteMaps().get("route_map1");
    // Original rule ordering should be preserved
    assertThat(rm1.getRules().keySet(), contains("9999", "1", "2"));
    RouteMapRule rule9999 = rm1.getRules().get("9999");
    RouteMapRule rule1 = rm1.getRules().get("1");
    RouteMapRule rule2 = rm1.getRules().get("2");

    // Defaults
    assertNull(longName.getComments());
    assertThat(longName.getRules(), anEmptyMap());
    assertNull(rule2.getAction());
    assertThat(rule2.getActionEffective(), equalTo(RouteMapRule.DEFAULT_ACTION));
    assertNull(rule2.getMatchIpAddress());

    // Explicitly configured properties
    assertThat(rm1.getComments(), equalTo("comment for route_map1"));
    assertThat(rule9999.getAction(), equalTo(RouteMapRule.Action.PERMIT));
    assertThat(rule9999.getActionEffective(), equalTo(RouteMapRule.Action.PERMIT));
    assertThat(rule9999.getMatchIpAddress(), equalTo("acl1"));
    assertThat(rule1.getAction(), equalTo(RouteMapRule.Action.DENY));
    assertThat(rule1.getActionEffective(), equalTo(RouteMapRule.Action.DENY));
    assertThat(rule1.getMatchIpAddress(), equalTo("acl2"));
  }

  @Test
  public void testRouteMapConversion() throws IOException {
    String hostname = "route_map";
    Configuration c = parseConfig(hostname);

    assertThat(
        c.getRoutingPolicies(), hasKeys("longest_route_map_name_allowed_by_f", "route_map1"));
    RoutingPolicy longName = c.getRoutingPolicies().get("longest_route_map_name_allowed_by_f");
    RoutingPolicy rm1 = c.getRoutingPolicies().get("route_map1");
    Bgpv4Route.Builder rb = Bgpv4Route.testBuilder();

    // This route-map only has a default-deny rule
    assertThat(longName.getStatements(), contains(Statements.ReturnFalse.toStaticStatement()));
    rb.setNetwork(Prefix.parse("10.10.10.0/24"));
    assertPolicyTakesAction(longName, false, rb.build(), c);

    // rm1 permits 10.10.10.0/24 or longer, then denies 10.10.0.0/16 or longer, then permits all

    // Networks permitted by rule 9999
    rb.setNetwork(Prefix.parse("10.10.10.0/24"));
    assertPolicyTakesAction(rm1, true, rb.build(), c);
    rb.setNetwork(Prefix.parse("10.10.10.128/25"));
    assertPolicyTakesAction(rm1, true, rb.build(), c);

    // Networks denied by rule 1
    rb.setNetwork(Prefix.parse("10.10.0.0/16"));
    assertPolicyTakesAction(rm1, false, rb.build(), c);
    rb.setNetwork(Prefix.parse("10.10.128.0/17"));
    assertPolicyTakesAction(rm1, false, rb.build(), c);

    // Networks permitted by rule 2
    rb.setNetwork(Prefix.parse("10.10.0.0/15"));
    assertPolicyTakesAction(rm1, true, rb.build(), c);
    rb.setNetwork(Prefix.ZERO);
    assertPolicyTakesAction(rm1, true, rb.build(), c);
  }

  /**
   * Asserts that the given {@link RoutingPolicy} explicitly takes the specified {@code action}
   * (permit if true, deny if false) on the given route. Will fail if the policy falls through.
   */
  private void assertPolicyTakesAction(
      RoutingPolicy policy, boolean action, AbstractRouteDecorator route, Configuration c) {
    assertThat(
        policy
            // Set environment's default action to !action to ensure failure if policy falls through
            .call(Environment.builder(c).setDefaultAction(!action).setOriginalRoute(route).build())
            .getBooleanValue(),
        equalTo(action));
  }

  @Test
  public void testRouteMapWarnings() throws IOException {
    String hostname = "route_map_warnings";
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    Warnings warnings =
        getOnlyElement(
            batfish
                .loadParseVendorConfigurationAnswerElement(batfish.getSnapshot())
                .getWarnings()
                .values());
    FortiosConfiguration vc =
        (FortiosConfiguration)
            batfish.loadVendorConfigurations(batfish.getSnapshot()).get(hostname);

    assertThat(
        warnings,
        hasParseWarnings(
            containsInAnyOrder(
                hasComment(
                    "Access-list or prefix-list acl_undefined is undefined and cannot be"
                        + " referenced"),
                hasComment(
                    "Expected route-map rule number in range 0-4294967295, but got '4294967296'"),
                hasComment("Route-map rule edit block ignored: name is invalid"),
                allOf(
                    hasComment("Illegal value for route-map name"),
                    hasText(containsString("too_long_to_use_for_a_route_map_name"))),
                hasComment("Route-map edit block ignored: name is invalid"))));

    // Ensure existing route-map rule is not obliterated by an invalid edit
    assertThat(vc.getRouteMaps(), hasKeys("route_map1"));
    RouteMap rm1 = vc.getRouteMaps().get("route_map1");
    assertThat(rm1.getRules(), hasKeys("1"));
    RouteMapRule rule1 = rm1.getRules().get("1");
    assertThat(rule1.getMatchIpAddress(), equalTo("acl_name1"));
  }

  @Test
  public void testRename() throws IOException {
    String hostname = "rename";
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    // batfish.getSettings().setDisableUnrecognized(false);
    FortiosConfiguration vc =
        (FortiosConfiguration)
            batfish.loadVendorConfigurations(batfish.getSnapshot()).get(hostname);

    // Policy 1 should not convert because it doesn't have any valid src or dst addresses
    assertThat(vc.getPolicies(), hasKeys("0"));
    assertThat(vc.getAddresses(), hasKeys("new_addr1", "new_addr2"));
    assertThat(vc.getServices(), hasKeys("new_service1", "new_service2"));
    assertThat(vc.getZones(), hasKeys("new_zone1", "new_zone2"));

    Policy policy = vc.getPolicies().get("0");
    // Policy should be using renamed structures
    // Whether or not they were renamed after initial reference
    assertThat(policy.getSrcIntfZones(), contains("new_zone1"));
    assertThat(policy.getDstIntfZones(), contains("new_zone2"));
    assertThat(policy.getSrcAddr(), contains("new_addr1"));
    assertThat(policy.getDstAddr(), contains("new_addr2"));
    assertThat(policy.getService(), containsInAnyOrder("new_service1", "new_service2"));
  }

  @Test
  public void testRenameWarnings() throws IOException {
    String hostname = "rename";
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    Warnings warnings =
        getOnlyElement(
            batfish
                .loadParseVendorConfigurationAnswerElement(batfish.getSnapshot())
                .getWarnings()
                .values());

    // Should get warnings when trying to use a renamed structure
    // Or trying to use an undefined structure that will be defined (renamed) later
    assertThat(
        warnings,
        hasParseWarnings(
            containsInAnyOrder(
                hasComment("Address or addrgrp old_addr1 is undefined and cannot be referenced"),
                hasComment("Address or addrgrp new_addr2 is undefined and cannot be referenced"),
                hasComment("Interface/zone old_zone1 is undefined and cannot be added to policy 1"),
                hasComment("Interface/zone new_zone2 is undefined and cannot be added to policy 1"),
                hasComment(
                    "Service or service group old_service1 is undefined and cannot be referenced"),
                hasComment(
                    "Service or service group new_service2 is undefined and cannot be referenced"),
                hasComment("Cannot rename non-existent address undefined"),
                hasComment("Cannot rename non-existent service custom undefined"),
                hasComment("Cannot rename non-existent zone undefined"),
                hasComment(
                    "Renaming zone new_zone1 conflicts with an existing object port1, ignoring"
                        + " this rename operation"),
                allOf(
                    hasComment("Illegal value for zone name"),
                    hasText(containsString("a name that is too long to use for this object type"))),
                hasComment("Policy edit block ignored: srcintf must be set"))));
  }

  @Test
  public void testRenameReferences() throws IOException {
    String hostname = "rename";
    String filename = "configs/" + hostname;

    Batfish batfish = getBatfishForConfigurationNames(hostname);
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());

    // Should have definitions for the renamed structures
    assertThat(ccae, hasDefinedStructure(filename, FortiosStructureType.ADDRESS, "new_addr1"));
    assertThat(
        ccae, hasDefinedStructure(filename, FortiosStructureType.SERVICE_CUSTOM, "new_service1"));

    // Rename should be part of the definitions
    assertThat(
        ccae,
        hasDefinedStructureWithDefinitionLines(
            filename, FortiosStructureType.ADDRESS, "new_addr2", contains(18, 19, 20, 76)));
    assertThat(
        ccae,
        hasDefinedStructureWithDefinitionLines(
            filename, FortiosStructureType.SERVICE_CUSTOM, "new_service2", contains(8, 9, 10, 73)));
    assertThat(
        ccae,
        hasDefinedStructureWithDefinitionLines(
            filename, FortiosStructureType.ZONE, "new_zone2", contains(41, 42, 43, 79)));

    // Should have references for the renamed structures, even if the renaming happened after the
    // reference
    assertThat(ccae, hasNumReferrers(filename, FortiosStructureType.ADDRESS, "new_addr1", 1));
    assertThat(ccae, hasNumReferrers(filename, FortiosStructureType.ADDRESS, "new_addr2", 1));
    assertThat(
        ccae, hasNumReferrers(filename, FortiosStructureType.SERVICE_CUSTOM, "new_service1", 1));
    assertThat(
        ccae, hasNumReferrers(filename, FortiosStructureType.SERVICE_CUSTOM, "new_service2", 1));

    // Should have undefined references where either:
    //   1. New names are used before the structure is renamed
    //   2. Old names are used after the structure is renamed
    assertThat(
        ccae,
        hasUndefinedReference(filename, FortiosStructureType.ADDRESS_OR_ADDRGRP, "old_addr1"));
    assertThat(
        ccae,
        hasUndefinedReference(filename, FortiosStructureType.ADDRESS_OR_ADDRGRP, "new_addr2"));
    assertThat(
        ccae,
        hasUndefinedReference(
            filename, FortiosStructureType.SERVICE_CUSTOM_OR_SERVICE_GROUP, "old_service1"));
    assertThat(
        ccae,
        hasUndefinedReference(
            filename, FortiosStructureType.SERVICE_CUSTOM_OR_SERVICE_GROUP, "new_service2"));
  }

  @Test
  public void testAddressDefinitionsAndReferences() throws IOException {
    String hostname = "address_defs_refs";
    String filename = "configs/" + hostname;

    Batfish batfish = getBatfishForConfigurationNames(hostname);
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());

    assertThat(ccae, hasDefinedStructure(filename, FortiosStructureType.ADDRESS, "iface_ref"));
    assertThat(ccae, hasDefinedStructure(filename, FortiosStructureType.ADDRESS, "zone_ref"));
    assertThat(ccae, hasDefinedStructure(filename, FortiosStructureType.ADDRESS, "undef_ref"));

    // Interface and zone refs include self-refs
    assertThat(ccae, hasNumReferrers(filename, FortiosStructureType.INTERFACE, "port1", 2));
    assertThat(ccae, hasNumReferrers(filename, FortiosStructureType.ZONE, "zoneA", 2));

    assertThat(
        ccae,
        hasUndefinedReference(
            filename,
            FortiosStructureType.INTERFACE_OR_ZONE,
            "UNDEFINED",
            FortiosStructureUsage.ADDRESS_ASSOCIATED_INTERFACE));
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
    assertThat(
        ccae,
        hasDefinedStructureWithDefinitionLines(
            filename, FortiosStructureType.POLICY, "2", contains(75)));

    // Confirm reference count is correct for used structure
    assertThat(ccae, hasNumReferrers(filename, FortiosStructureType.ADDRESS, "addr1", 2));
    assertThat(ccae, hasNumReferrers(filename, FortiosStructureType.ADDRESS, "addr2", 4));
    assertThat(ccae, hasNumReferrers(filename, FortiosStructureType.ADDRESS, "addr3", 1));
    // Interface, zone, and policy refs include self-refs
    assertThat(ccae, hasNumReferrers(filename, FortiosStructureType.ZONE, "zoneA", 3));
    assertThat(ccae, hasNumReferrers(filename, FortiosStructureType.INTERFACE, "port2", 3));
    assertThat(ccae, hasNumReferrers(filename, FortiosStructureType.INTERFACE, "port3", 2));
    assertThat(ccae, hasNumReferrers(filename, FortiosStructureType.SERVICE_CUSTOM, "service1", 1));
    assertThat(ccae, hasNumReferrers(filename, FortiosStructureType.SERVICE_CUSTOM, "service2", 2));
    assertThat(ccae, hasNumReferrers(filename, FortiosStructureType.POLICY, "1", 1));
    // Cloned policy should also show up with a self-ref
    assertThat(ccae, hasNumReferrers(filename, FortiosStructureType.POLICY, "2", 1));

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
    assertThat(
        ccae,
        hasUndefinedReference(
            filename, FortiosStructureType.POLICY, "2", FortiosStructureUsage.POLICY_DELETE));
    assertThat(
        ccae,
        hasUndefinedReference(
            filename, FortiosStructureType.POLICY, "3", FortiosStructureUsage.POLICY_CLONE));

    // Deleted structure should not have a definition or self-ref
    assertThat(ccae, not(hasDefinedStructure(filename, FortiosStructureType.POLICY, "5")));
    assertThat(
        ccae,
        not(
            hasReferencedStructure(
                filename,
                FortiosStructureType.POLICY,
                "5",
                FortiosStructureUsage.POLICY_SELF_REF)));
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

  private Flow createFlow(String ingressIface, Ip src, Ip dst, int port) {
    return Flow.builder()
        .setIngressNode("node")
        .setIngressInterface(ingressIface)
        .setIpProtocol(IpProtocol.TCP)
        .setSrcIp(src)
        .setDstIp(dst)
        .setSrcPort(9999) // Arbitrary src port
        .setDstPort(port)
        .build();
  }

  /** Test that policies are converted correctly for use as interface outgoing filters. */
  @Test
  public void testInterfaceOutgoingFilterPolicyConversion() throws IOException {
    String hostname = "policy";
    Configuration c = parseConfig(hostname);

    int dstPortAllowed = 2345;
    int dstPortDenied = 1234;
    int dstPortDeniedIndirect = 1235;

    String port1 = "port1";
    Ip port1Addr = Ip.parse("10.0.1.2");
    String port2 = "port2";
    Ip port2Addr = Ip.parse("10.0.2.2");
    String port3 = "port3";
    Ip port3Addr = Ip.parse("10.0.3.2");
    String port4 = "port4";
    Ip port4Addr = Ip.parse("10.0.4.2");

    // Explicitly permitted
    Flow p1ToP3 = createFlow(port1, port1Addr, port3Addr, dstPortAllowed);
    Flow p1ToP4 = createFlow(port1, port1Addr, port3Addr, dstPortAllowed);
    Flow p2ToP3 = createFlow(port2, port2Addr, port4Addr, dstPortAllowed);
    Flow p2ToP4 = createFlow(port2, port2Addr, port4Addr, dstPortAllowed);

    // Explicitly denied
    Flow p1ToP3Denied = createFlow(port1, port1Addr, port3Addr, dstPortDenied);
    Flow p2ToP3Denied = createFlow(port2, port2Addr, port4Addr, dstPortDenied);
    // Denied through a service group member, not directly through service
    Flow p1ToP3DeniedIndirect = createFlow(port1, port1Addr, port3Addr, dstPortDeniedIndirect);

    // No-match, denied
    Flow p3ToP1 = createFlow(port3, port3Addr, port1Addr, dstPortAllowed);

    // Explicitly permitted
    assertThat(c, hasInterface(port3, hasOutgoingFilter(accepts(p1ToP3, port1, c))));
    assertThat(c, hasInterface(port4, hasOutgoingFilter(accepts(p1ToP4, port1, c))));
    assertThat(c, hasInterface(port3, hasOutgoingFilter(accepts(p2ToP3, port2, c))));
    assertThat(c, hasInterface(port4, hasOutgoingFilter(accepts(p2ToP4, port2, c))));

    // No-match, denied
    assertThat(c, hasInterface(port1, hasOutgoingFilter(rejects(p3ToP1, port3, c))));

    // Explicitly denied
    assertThat(c, hasInterface(port3, hasOutgoingFilter(rejects(p1ToP3Denied, port1, c))));
    assertThat(c, hasInterface(port3, hasOutgoingFilter(rejects(p1ToP3DeniedIndirect, port1, c))));
    assertThat(c, hasInterface(port3, hasOutgoingFilter(rejects(p2ToP3Denied, port2, c))));
  }

  @Test
  public void testPolicyClone() throws IOException {
    String hostname = "policy_clone";
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    FortiosConfiguration vc =
        (FortiosConfiguration)
            batfish.loadVendorConfigurations(batfish.getSnapshot()).get(hostname);

    assertThat(vc.getPolicies().keySet(), contains("1", "2", "3", "4"));

    // Confirm the clone has the correct properties, i.e. everything identical to parent except num
    Policy clone = vc.getPolicies().get("3");
    assertThat(clone.getAction(), equalTo(Action.ACCEPT));
    assertThat(clone.getDstIntf(), contains("any"));
    assertThat(clone.getSrcIntf(), contains("any"));
    assertThat(clone.getDstAddr(), contains("all"));
    assertThat(clone.getSrcAddr(), contains("all"));
    assertThat(clone.getService(), contains("ALL_TCP"));
    assertThat(clone.getNumber(), equalTo("3"));
  }

  @Test
  public void testPolicyDelete() throws IOException {
    String hostname = "policy_delete";
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    FortiosConfiguration vc =
        (FortiosConfiguration)
            batfish.loadVendorConfigurations(batfish.getSnapshot()).get(hostname);

    // 3 doesn't show up since it is deleted
    // 1 is last, since it was deleted then re-created
    assertThat(vc.getPolicies().keySet(), contains("2", "1"));
  }

  @Test
  public void testPolicyMove() throws IOException {
    String hostname = "policy_move";
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    FortiosConfiguration vc =
        (FortiosConfiguration)
            batfish.loadVendorConfigurations(batfish.getSnapshot()).get(hostname);

    assertThat(vc.getPolicies().keySet(), contains("5", "1", "2", "4", "3"));
  }

  @Test
  public void testAccessListExtraction() throws IOException {
    String hostname = "access_list";
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    FortiosConfiguration vc =
        (FortiosConfiguration)
            batfish.loadVendorConfigurations(batfish.getSnapshot()).get(hostname);

    assertThat(vc.getAccessLists(), hasKeys("the_longest_access_list_name_possib", "acl_name1"));
    AccessList longName = vc.getAccessLists().get("the_longest_access_list_name_possib");
    AccessList acl1 = vc.getAccessLists().get("acl_name1");

    assertThat(longName.getRules(), anEmptyMap());
    // Rules should be in original insert order
    assertThat(acl1.getRules().keySet(), contains("12", "1", "2"));
    AccessListRule rule12 = acl1.getRules().get("12");
    AccessListRule rule1 = acl1.getRules().get("1");
    AccessListRule rule2 = acl1.getRules().get("2");

    // Defaults
    assertNull(longName.getComments());
    assertNull(rule2.getAction());
    assertThat(rule2.getActionEffective(), equalTo(AccessListRule.DEFAULT_ACTION));
    assertNull(rule2.getExactMatch());
    assertThat(rule2.getExactMatchEffective(), equalTo(AccessListRule.DEFAULT_EXACT_MATCH));
    assertNull(rule2.getWildcard());

    // Explicitly (re)configured values
    assertThat(acl1.getComments(), equalTo("comment for acl_name1"));
    assertThat(rule12.getAction(), equalTo(AccessListRule.Action.PERMIT));
    assertThat(rule12.getActionEffective(), equalTo(AccessListRule.Action.PERMIT));
    assertTrue(rule12.getExactMatch());
    assertTrue(rule12.getExactMatchEffective());
    assertThat(rule12.getPrefix(), equalTo(Prefix.parse("1.2.3.0/24")));
    assertNull(rule12.getWildcard());
    assertThat(rule1.getAction(), equalTo(AccessListRule.Action.DENY));
    assertThat(rule1.getActionEffective(), equalTo(AccessListRule.Action.DENY));
    assertFalse(rule1.getExactMatch());
    assertFalse(rule1.getExactMatchEffective());
    assertThat(
        rule1.getWildcard(),
        equalTo(IpWildcard.ipWithWildcardMask(Ip.parse("1.0.0.0"), Ip.parse("0.255.255.255"))));
    assertNull(rule1.getPrefix());
    assertThat(rule2.getPrefix(), equalTo(Prefix.ZERO));
  }

  @Test
  public void testAccessListConversion() throws IOException {
    String hostname = "access_list";
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    Configuration c = batfish.loadConfigurations(batfish.getSnapshot()).get(hostname);

    assertThat(
        c.getRouteFilterLists(), hasKeys("the_longest_access_list_name_possib", "acl_name1"));
    RouteFilterList longName = c.getRouteFilterLists().get("the_longest_access_list_name_possib");
    RouteFilterList acl1 = c.getRouteFilterLists().get("acl_name1");

    // Access-list with no lines doesn't permit anything
    assertThat(longName.getLines(), empty());
    assertFalse(longName.permits(Prefix.ZERO));

    // acl1 permits 1.2.3.0/24, then denies everything in 1.0.0.0/8, then permits all
    assertTrue(acl1.permits(Prefix.parse("1.2.3.0/24")));
    assertFalse(acl1.permits(Prefix.parse("1.2.3.0/25"))); // permit 1.2.3.0/24 is exact-match
    assertFalse(acl1.permits(Prefix.parse("1.2.3.0/23")));
    assertFalse(acl1.permits(Prefix.parse("1.0.0.0/8")));
    assertTrue(acl1.permits(Prefix.parse("0.0.0.0/0")));
  }

  @Test
  public void testAccessListReference() throws IOException {
    String hostname = "access_list_reference";
    String filename = "configs/" + hostname;

    Batfish batfish = getBatfishForConfigurationNames(hostname);
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());

    assertThat(
        ccae,
        hasDefinedStructureWithDefinitionLines(
            filename,
            FortiosStructureType.ACCESS_LIST,
            "acl_name1",
            contains(5, 6, 9, 10, 11, 12, 13, 14, 15, 16)));
    assertThat(
        ccae,
        hasDefinedStructureWithDefinitionLines(
            filename, FortiosStructureType.ACCESS_LIST, "acl_name2", contains(7, 8)));

    assertThat(ccae, hasNumReferrers(filename, FortiosStructureType.ACCESS_LIST, "acl_name1", 2));
    assertThat(ccae, hasNumReferrers(filename, FortiosStructureType.ACCESS_LIST, "acl_name2", 0));

    assertThat(
        ccae,
        hasUndefinedReference(
            filename, FortiosStructureType.ACCESS_LIST_OR_PREFIX_LIST, "acl_undefined"));
  }

  @Test
  public void testAccessListWarnings() throws IOException {
    String hostname = "access_list_warnings";
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    Warnings warnings =
        getOnlyElement(
            batfish
                .loadParseVendorConfigurationAnswerElement(batfish.getSnapshot())
                .getWarnings()
                .values());
    FortiosConfiguration vc =
        (FortiosConfiguration)
            batfish.loadVendorConfigurations(batfish.getSnapshot()).get(hostname);

    assertThat(
        warnings,
        hasParseWarnings(
            containsInAnyOrder(
                hasComment("Access-list edit block ignored: name is invalid"),
                hasComment("Illegal value for access-list name"),
                hasComment("Access-list rule edit block ignored: name is invalid"),
                hasComment(
                    "Expected access-list rule number in range 0-4294967295, but got '4294967296'"),
                hasComment(
                    "Access-list rule edit block ignored: prefix or wildcard must be set"))));

    // None of the invalid access-lists or rule should make it into VS model
    assertThat(vc.getAccessLists(), hasKeys("acl_name1"));
    assertThat(vc.getAccessLists().get("acl_name1").getRules(), anEmptyMap());
  }

  ////////////////////
  // Prefix-list tests //
  ////////////////////

  @Test
  public void testPrefixListExtraction() throws IOException {
    String hostname = "prefix_list";
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    FortiosConfiguration vc =
        (FortiosConfiguration)
            batfish.loadVendorConfigurations(batfish.getSnapshot()).get(hostname);

    assertThat(vc.getPrefixLists(), hasKeys("pl_name1", "pl_name2"));
    PrefixList pl1 = vc.getPrefixLists().get("pl_name1");
    PrefixList pl2 = vc.getPrefixLists().get("pl_name2");

    // Check comments
    assertThat(pl1.getComments(), equalTo("comment for pl_name1"));
    assertNull(pl2.getComments());

    // Check rules are in insert order
    assertThat(pl1.getRules().keySet(), contains("1", "2", "3", "4"));
    assertThat(pl2.getRules().keySet(), contains("1"));

    PrefixListRule rule1 = pl1.getRules().get("1");
    PrefixListRule rule2 = pl1.getRules().get("2");
    PrefixListRule rule3 = pl1.getRules().get("3");
    PrefixListRule rule4 = pl1.getRules().get("4");
    PrefixListRule rule_pl2_1 = pl2.getRules().get("1");

    // Rule 1: set prefix 10.0.0.0 255.255.0.0, set le 32
    assertThat(rule1.getPrefix(), equalTo(Prefix.parse("10.0.0.0/16")));
    assertThat(rule1.getGe(), equalTo(PrefixListRule.DEFAULT_GE));
    assertThat(rule1.getLe(), equalTo(32));

    // Rule 2: set prefix 192.168.0.0/24, unset ge, set le 24
    assertThat(rule2.getPrefix(), equalTo(Prefix.parse("192.168.0.0/24")));
    assertThat(rule2.getGe(), equalTo(PrefixListRule.DEFAULT_GE));
    assertThat(rule2.getLe(), equalTo(24));

    // Rule 3: set prefix 172.16.0.0/16, set ge 16, set le 24
    assertThat(rule3.getPrefix(), equalTo(Prefix.parse("172.16.0.0/16")));
    assertThat(rule3.getGe(), equalTo(16));
    assertThat(rule3.getLe(), equalTo(24));

    // Rule 4: set prefix 10.1.0.0 255.255.0.0, unset ge, unset le
    assertThat(rule4.getPrefix(), equalTo(Prefix.parse("10.1.0.0/16")));
    assertThat(rule4.getGe(), equalTo(PrefixListRule.DEFAULT_GE));
    assertThat(rule4.getLe(), equalTo(PrefixListRule.DEFAULT_LE));

    // Rule in pl2: set prefix 192.168.1.0 255.255.255.0 (no ge/le)
    assertThat(rule_pl2_1.getPrefix(), equalTo(Prefix.parse("192.168.1.0/24")));
    assertThat(rule_pl2_1.getGe(), equalTo(PrefixListRule.DEFAULT_GE));
    assertThat(rule_pl2_1.getLe(), equalTo(PrefixListRule.DEFAULT_LE));
  }

  @Test
  public void testPrefixListConversion() throws IOException {
    String hostname = "prefix_list";
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    Configuration c = batfish.loadConfigurations(batfish.getSnapshot()).get(hostname);

    assertThat(c.getRouteFilterLists(), hasKeys("pl_name1", "pl_name2"));
    RouteFilterList pl1 = c.getRouteFilterLists().get("pl_name1");
    RouteFilterList pl2 = c.getRouteFilterLists().get("pl_name2");

    // Rule 1: 10.0.0.0/16 le 32 -> matches /16 through /32
    assertTrue(pl1.permits(Prefix.parse("10.0.0.0/16")));
    assertTrue(pl1.permits(Prefix.parse("10.0.1.0/24")));
    assertTrue(pl1.permits(Prefix.parse("10.0.1.128/32")));
    assertFalse(pl1.permits(Prefix.parse("10.0.0.0/15"))); // outside range

    // Rule 3: 172.16.0.0/16 ge 16 le 24 -> matches /16 through /24
    assertTrue(pl1.permits(Prefix.parse("172.16.0.0/16")));
    assertTrue(pl1.permits(Prefix.parse("172.16.1.0/24")));
    assertFalse(pl1.permits(Prefix.parse("172.16.1.128/25"))); // > 24
    assertFalse(pl1.permits(Prefix.parse("172.16.0.0/15"))); // < 16

    // Rule 4: 10.1.0.0/16 with no ge/le -> matches /16 through /32 (default)
    assertTrue(pl1.permits(Prefix.parse("10.1.0.0/16")));
    assertTrue(pl1.permits(Prefix.parse("10.1.1.0/24")));
    assertTrue(pl1.permits(Prefix.parse("10.1.1.1/32")));

    // pl2: 192.168.1.0/24 with no ge/le -> matches /24 through /32
    assertTrue(pl2.permits(Prefix.parse("192.168.1.0/24")));
    assertTrue(pl2.permits(Prefix.parse("192.168.1.128/25")));
    assertFalse(pl2.permits(Prefix.parse("192.168.1.0/23"))); // < 24
  }

  @Test
  public void testPrefixListRouteMapReference() throws IOException {
    String hostname = "prefix_list_route_map";
    String filename = "configs/" + hostname;

    Batfish batfish = getBatfishForConfigurationNames(hostname);
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());

    // Verify prefix-lists are defined
    assertThat(ccae, hasDefinedStructure(filename, FortiosStructureType.PREFIX_LIST, "PL-IN-1"));
    assertThat(ccae, hasDefinedStructure(filename, FortiosStructureType.PREFIX_LIST, "PL-OUT-1"));

    // Verify route-maps reference prefix-lists
    assertThat(ccae, hasNumReferrers(filename, FortiosStructureType.PREFIX_LIST, "PL-IN-1", 1));
    assertThat(ccae, hasNumReferrers(filename, FortiosStructureType.PREFIX_LIST, "PL-OUT-1", 1));

    // Verify route-maps are converted to routing policies
    Configuration c = batfish.loadConfigurations(batfish.getSnapshot()).get(hostname);
    assertThat(c.getRoutingPolicies(), hasKeys("RM-IN-1", "RM-OUT-1"));
  }

  ////////////////////////
  // Setup / test infra //
  ////////////////////////

  @Rule public TemporaryFolder _folder = new TemporaryFolder();
  @Rule public ExpectedException _thrown = ExpectedException.none();

  private static final String TESTCONFIGS_PREFIX = "org/batfish/grammar/fortios/testconfigs/";

  private final BddTestbed _bddTestbed = new BddTestbed(ImmutableMap.of(), ImmutableMap.of());
  private final BDDPacket _pkt = _bddTestbed.getPkt();
  private final BDD _zero = _bddTestbed.getPkt().getFactory().zero();
  private final BDD _one = _bddTestbed.getPkt().getFactory().one();
  private final IpSpaceToBDD _dstIpBdd = _bddTestbed.getDstIpBdd();
  private final IpSpaceToBDD _srcIpBdd = _bddTestbed.getSrcIpBdd();

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
        new FortiosControlPlaneExtractor(src, parser, new Warnings(), new SilentSyntaxCollection());
    ParserRuleContext tree =
        Batfish.parse(parser, new BatfishLogger(BatfishLogger.LEVELSTR_FATAL, false), settings);
    extractor.processParseTree(DUMMY_SNAPSHOT_1, tree);
    FortiosConfiguration vendorConfiguration =
        (FortiosConfiguration) extractor.getVendorConfiguration();
    vendorConfiguration.setFilename(TESTCONFIGS_PREFIX + hostname);
    // crash if not serializable
    return SerializationUtils.clone(vendorConfiguration);
  }
}
