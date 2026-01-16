package org.batfish.grammar.cisco_ftd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.Map;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.Warnings;
import org.batfish.config.Settings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpsecAuthenticationAlgorithm;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.transformation.Transformation;
import org.batfish.datamodel.transformation.TransformationStep;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.main.BatfishTestUtils;
import org.batfish.representation.cisco_ftd.FtdAccessGroup;
import org.batfish.representation.cisco_ftd.FtdAccessList;
import org.batfish.representation.cisco_ftd.FtdAccessListLine;
import org.batfish.representation.cisco_ftd.FtdConfiguration;
import org.batfish.representation.cisco_ftd.FtdNatRule;
import org.batfish.representation.cisco_ftd.FtdNetworkObject;
import org.batfish.representation.cisco_ftd.FtdNetworkObjectGroup;
import org.batfish.representation.cisco_ftd.FtdPolicyMap;
import org.batfish.representation.cisco_ftd.FtdServiceObjectGroup;
import org.batfish.representation.cisco_ftd.FtdServicePolicy;
import org.batfish.representation.cisco_ftd.FtdCryptoMapEntry;
import org.batfish.representation.cisco_ftd.FtdCryptoMapSet;
import org.batfish.representation.cisco_ftd.FtdIpsecTransformSet;
import org.batfish.representation.cisco_ftd.FtdIpsecProfile;
import org.batfish.representation.cisco_ftd.FtdIkev2Policy;
import org.batfish.representation.cisco_ftd.FtdTunnelGroup;
import org.batfish.datamodel.DiffieHellmanGroup;
import org.batfish.datamodel.EncryptionAlgorithm;
import org.batfish.datamodel.IkeHashingAlgorithm;
import static org.hamcrest.Matchers.equalTo;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class FtdGrammarTest {
  @Rule
  public TemporaryFolder _folder = new TemporaryFolder();

  protected FtdConfiguration parseVendorConfig(String fileText) {
    Settings settings = new Settings();
    BatfishTestUtils.configureBatfishTestSettings(settings);

    FtdCombinedParser parser = new FtdCombinedParser(fileText, settings);

    Warnings warnings = new Warnings(true, true, true);
    SilentSyntaxCollection silentSyntax = new SilentSyntaxCollection();

    FtdControlPlaneExtractor extractor = new FtdControlPlaneExtractor(fileText, parser, warnings, silentSyntax);

    extractor.processParseTree(BatfishTestUtils.DUMMY_SNAPSHOT_1, parser.parse());

    FtdConfiguration vc = (FtdConfiguration) extractor.getVendorConfiguration();
    vc.setVendor(ConfigurationFormat.CISCO_FTD);
    vc.setWarnings(warnings);

    return vc;
  }

  protected FtdConfiguration parseVendorConfigWithSerialization(String fileText) {
    FtdConfiguration vc = parseVendorConfig(fileText);
    FtdConfiguration cloned = SerializationUtils.clone(vc);
    return cloned;
  }

  protected Configuration parseConfig(String fileText) throws IOException {
    return parseConfig("ftd-test", fileText);
  }

  protected Configuration parseConfig(String hostname, String fileText) throws IOException {
    Map<String, Configuration> configs = parseTextConfigs(fileText);
    String canonicalHostname = hostname.toLowerCase();
    if (!configs.containsKey(canonicalHostname)) {
      throw new IllegalArgumentException("Configuration not found: " + canonicalHostname);
    }
    return configs.get(canonicalHostname);
  }

  private Map<String, Configuration> parseTextConfigs(String fileText) throws IOException {
    return BatfishTestUtils.getBatfishForTextConfigs(_folder, fileText)
        .loadConfigurations(BatfishTestUtils.DUMMY_SNAPSHOT_1);
  }

  protected static String join(String... lines) {
    return String.join("\n", lines) + "\n";
  }

  @Test
  public void testColonComments() {
    String config = ": this is a comment\n";
    FtdConfiguration vc = parseVendorConfig(config);
    assertThat(vc, notNullValue());
  }

  @Test
  public void testNatParsing() {
    String config = "nat (inside,outside) source static real_obj mapped_obj\n";
    FtdConfiguration vc = parseVendorConfig(config);

    assertThat(vc, notNullValue());
    assertThat(vc.getNatRules(), hasSize(1));
    FtdNatRule rule = vc.getNatRules().get(0);
    assertThat(rule.getSourceInterface(), equalTo("inside"));
    assertThat(rule.getDestinationInterface(), equalTo("outside"));
  }

  @Test
  public void testNatVendorConversion() throws IOException {
    String config = join(
        "interface Ethernet0/0",
        " nameif inside",
        "interface Ethernet0/1",
        " nameif outside",
        "object network REAL_ONE",
        " host 10.0.0.1",
        "object network MAPPED_ONE",
        " host 192.0.2.1",
        "object network REAL_TWO",
        " host 10.0.0.2",
        "object network MAPPED_TWO",
        " host 192.0.2.2",
        "nat (inside,outside) source static REAL_ONE MAPPED_ONE",
        "nat (inside,outside) destination static REAL_TWO MAPPED_TWO");

    FtdConfiguration vc = parseVendorConfig(config);
    Configuration c = vc.toVendorIndependentConfigurations().get(0);

    Transformation expectedOutgoing = Transformation.when(
        AclLineMatchExprs.and(
            AclLineMatchExprs.matchSrcInterface("Ethernet0/0"),
            AclLineMatchExprs.matchSrc(Ip.parse("10.0.0.1").toIpSpace())))
        .apply(TransformationStep.assignSourceIp(Ip.parse("192.0.2.1")))
        .build();

    Transformation expectedIncoming = Transformation.when(
        AclLineMatchExprs.and(
            AclLineMatchExprs.matchSrcInterface("Ethernet0/1"),
            AclLineMatchExprs.matchDst(Ip.parse("192.0.2.2").toIpSpace())))
        .apply(TransformationStep.assignDestinationIp(Ip.parse("10.0.0.2")))
        .build();

    assertThat(
        c.getAllInterfaces().get("Ethernet0/0").getOutgoingTransformation(),
        equalTo(expectedOutgoing));
    assertThat(
        c.getAllInterfaces().get("Ethernet0/1").getIncomingTransformation(),
        equalTo(expectedIncoming));
  }

  @Test
  public void testDynamicNatVendorConversion() {
    String config = join(
        "interface Ethernet0/0",
        " nameif inside",
        "object network REAL_DYNAMIC",
        " subnet 10.0.0.0 255.255.255.0",
        "object network MAPPED_POOL",
        " subnet 192.0.2.0 255.255.255.0",
        "nat (inside,outside) source dynamic REAL_DYNAMIC MAPPED_POOL");

    FtdConfiguration vc = parseVendorConfig(config);
    Configuration c = vc.toVendorIndependentConfigurations().get(0);

    Prefix mapped = Prefix.parse("192.0.2.0/24");
    Transformation expectedOutgoing = Transformation.when(
        AclLineMatchExprs.and(
            AclLineMatchExprs.matchSrcInterface("Ethernet0/0"),
            AclLineMatchExprs.matchSrc(Prefix.parse("10.0.0.0/24").toIpSpace())))
        .apply(TransformationStep.assignSourceIp(mapped.getStartIp(), mapped.getEndIp()))
        .build();

    assertThat(
        c.getAllInterfaces().get("Ethernet0/0").getOutgoingTransformation(),
        equalTo(expectedOutgoing));
  }

  @Test
  public void testNatServiceTranslationConversion() {
    String config = join(
        "interface Ethernet0/0",
        " nameif inside",
        "interface Ethernet0/1",
        " nameif outside",
        "object network REAL_SVC",
        " host 10.0.0.10",
        "object network MAPPED_SVC",
        " host 192.0.2.10",
        "nat (inside,outside) destination static REAL_SVC MAPPED_SVC service tcp 80 8080");

    FtdConfiguration vc = parseVendorConfig(config);
    Configuration c = vc.toVendorIndependentConfigurations().get(0);

    Transformation expectedIncoming = Transformation.when(
        AclLineMatchExprs.and(
            AclLineMatchExprs.matchSrcInterface("Ethernet0/1"),
            AclLineMatchExprs.matchDst(Ip.parse("192.0.2.10").toIpSpace()),
            AclLineMatchExprs.matchIpProtocol(IpProtocol.TCP),
            AclLineMatchExprs.matchDstPort(8080)))
        .apply(
            ImmutableList.of(
                TransformationStep.assignDestinationIp(Ip.parse("10.0.0.10")),
                TransformationStep.assignDestinationPort(80)))
        .build();

    assertThat(
        c.getAllInterfaces().get("Ethernet0/1").getIncomingTransformation(),
        equalTo(expectedIncoming));
  }

  @Test
  public void testNatServiceSourceTranslationConversion() {
    String config = join(
        "interface Ethernet0/0",
        " nameif inside",
        "object network REAL_SRC_SVC",
        " host 10.0.0.20",
        "object network MAPPED_SRC_SVC",
        " host 192.0.2.20",
        "nat (inside,outside) source static REAL_SRC_SVC MAPPED_SRC_SVC service tcp 12345 23456");

    FtdConfiguration vc = parseVendorConfig(config);
    Configuration c = vc.toVendorIndependentConfigurations().get(0);

    Transformation expectedOutgoing = Transformation.when(
        AclLineMatchExprs.and(
            AclLineMatchExprs.matchSrcInterface("Ethernet0/0"),
            AclLineMatchExprs.matchSrc(Ip.parse("10.0.0.20").toIpSpace()),
            AclLineMatchExprs.matchIpProtocol(IpProtocol.TCP),
            AclLineMatchExprs.matchSrcPort(12345)))
        .apply(
            ImmutableList.of(
                TransformationStep.assignSourceIp(Ip.parse("192.0.2.20")),
                TransformationStep.assignSourcePort(23456)))
        .build();

    assertThat(
        c.getAllInterfaces().get("Ethernet0/0").getOutgoingTransformation(),
        equalTo(expectedOutgoing));
  }

  @Test
  public void testNatServiceTranslationAlternatePortConversion() {
    String config = join(
        "interface Ethernet0/1",
        " nameif outside",
        "object network REAL_EQ",
        " host 10.0.0.30",
        "object network MAPPED_EQ",
        " host 192.0.2.30",
        "nat (inside,outside) destination static REAL_EQ MAPPED_EQ service tcp 443 8443");

    FtdConfiguration vc = parseVendorConfig(config);
    Configuration c = vc.toVendorIndependentConfigurations().get(0);

    Transformation expectedIncoming = Transformation.when(
        AclLineMatchExprs.and(
            AclLineMatchExprs.matchSrcInterface("Ethernet0/1"),
            AclLineMatchExprs.matchDst(Ip.parse("192.0.2.30").toIpSpace()),
            AclLineMatchExprs.matchIpProtocol(IpProtocol.TCP),
            AclLineMatchExprs.matchDstPort(8443)))
        .apply(
            ImmutableList.of(
                TransformationStep.assignDestinationIp(Ip.parse("10.0.0.30")),
                TransformationStep.assignDestinationPort(443)))
        .build();

    assertThat(
        c.getAllInterfaces().get("Ethernet0/1").getIncomingTransformation(),
        equalTo(expectedIncoming));
  }

  @Test
  public void testNatServiceTranslationUdpNamedPortConversion() {
    String config = join(
        "interface Ethernet0/1",
        " nameif outside",
        "object network REAL_DNS",
        " host 10.0.0.53",
        "object network MAPPED_DNS",
        " host 192.0.2.53",
        "nat (inside,outside) destination static REAL_DNS MAPPED_DNS service udp 53 5353");

    FtdConfiguration vc = parseVendorConfig(config);
    Configuration c = vc.toVendorIndependentConfigurations().get(0);

    Transformation expectedIncoming = Transformation.when(
        AclLineMatchExprs.and(
            AclLineMatchExprs.matchSrcInterface("Ethernet0/1"),
            AclLineMatchExprs.matchDst(Ip.parse("192.0.2.53").toIpSpace()),
            AclLineMatchExprs.matchIpProtocol(IpProtocol.UDP),
            AclLineMatchExprs.matchDstPort(5353)))
        .apply(
            ImmutableList.of(
                TransformationStep.assignDestinationIp(Ip.parse("10.0.0.53")),
                TransformationStep.assignDestinationPort(53)))
        .build();

    assertThat(
        c.getAllInterfaces().get("Ethernet0/1").getIncomingTransformation(),
        equalTo(expectedIncoming));
  }

  @Test
  public void testNatServiceTranslationNamedPortConversion() {
    String config = join(
        "interface Ethernet0/1",
        " nameif outside",
        "object network REAL_HTTP",
        " host 10.0.0.80",
        "object network MAPPED_HTTP",
        " host 192.0.2.80",
        "nat (inside,outside) destination static REAL_HTTP MAPPED_HTTP service tcp http 8080");

    FtdConfiguration vc = parseVendorConfig(config);
    Configuration c = vc.toVendorIndependentConfigurations().get(0);

    Transformation expectedIncoming = Transformation.when(
        AclLineMatchExprs.and(
            AclLineMatchExprs.matchSrcInterface("Ethernet0/1"),
            AclLineMatchExprs.matchDst(Ip.parse("192.0.2.80").toIpSpace()),
            AclLineMatchExprs.matchIpProtocol(IpProtocol.TCP),
            AclLineMatchExprs.matchDstPort(8080)))
        .apply(
            ImmutableList.of(
                TransformationStep.assignDestinationIp(Ip.parse("10.0.0.80")),
                TransformationStep.assignDestinationPort(80)))
        .build();

    assertThat(
        c.getAllInterfaces().get("Ethernet0/1").getIncomingTransformation(),
        equalTo(expectedIncoming));
  }

  @Test
  public void testNatMixedOrderingConversion() {
    String config = join(
        "interface Ethernet0/0",
        " nameif inside",
        "object network REAL_ONE",
        " host 10.0.0.1",
        "object network MAPPED_ONE",
        " host 192.0.2.1",
        "object network REAL_NET",
        " subnet 10.0.0.0 255.255.255.0",
        "object network POOL_NET",
        " subnet 192.0.2.0 255.255.255.0",
        "nat (inside,outside) source static REAL_ONE MAPPED_ONE",
        "nat (inside,outside) source dynamic REAL_NET POOL_NET");

    FtdConfiguration vc = parseVendorConfig(config);
    Configuration c = vc.toVendorIndependentConfigurations().get(0);

    Transformation second = Transformation.when(
        AclLineMatchExprs.and(
            AclLineMatchExprs.matchSrcInterface("Ethernet0/0"),
            AclLineMatchExprs.matchSrc(Prefix.parse("10.0.0.0/24").toIpSpace())))
        .apply(
            TransformationStep.assignSourceIp(
                Prefix.parse("192.0.2.0/24").getStartIp(),
                Prefix.parse("192.0.2.0/24").getEndIp()))
        .build();

    Transformation expectedOutgoing = Transformation.when(
        AclLineMatchExprs.and(
            AclLineMatchExprs.matchSrcInterface("Ethernet0/0"),
            AclLineMatchExprs.matchSrc(Ip.parse("10.0.0.1").toIpSpace())))
        .apply(TransformationStep.assignSourceIp(Ip.parse("192.0.2.1")))
        .setOrElse(second)
        .build();

    assertThat(
        c.getAllInterfaces().get("Ethernet0/0").getOutgoingTransformation(),
        equalTo(expectedOutgoing));
  }

  @Test
  public void testNatOrderingAcrossInterfacesConversion() {
    String config = join(
        "interface Ethernet0/0",
        " nameif inside",
        "interface Ethernet0/2",
        " nameif dmz",
        "object network REAL_INSIDE",
        " host 10.0.0.10",
        "object network MAPPED_INSIDE",
        " host 192.0.2.10",
        "object network REAL_DMZ",
        " host 10.0.1.10",
        "object network MAPPED_DMZ",
        " host 192.0.2.20",
        "nat (inside,outside) source static REAL_INSIDE MAPPED_INSIDE",
        "nat (dmz,outside) source static REAL_DMZ MAPPED_DMZ");

    FtdConfiguration vc = parseVendorConfig(config);
    Configuration c = vc.toVendorIndependentConfigurations().get(0);

    Transformation expectedInside = Transformation.when(
        AclLineMatchExprs.and(
            AclLineMatchExprs.matchSrcInterface("Ethernet0/0"),
            AclLineMatchExprs.matchSrc(Ip.parse("10.0.0.10").toIpSpace())))
        .apply(TransformationStep.assignSourceIp(Ip.parse("192.0.2.10")))
        .build();

    Transformation expectedDmz = Transformation.when(
        AclLineMatchExprs.and(
            AclLineMatchExprs.matchSrcInterface("Ethernet0/2"),
            AclLineMatchExprs.matchSrc(Ip.parse("10.0.1.10").toIpSpace())))
        .apply(TransformationStep.assignSourceIp(Ip.parse("192.0.2.20")))
        .build();

    assertThat(
        c.getAllInterfaces().get("Ethernet0/0").getOutgoingTransformation(),
        equalTo(expectedInside));
    assertThat(
        c.getAllInterfaces().get("Ethernet0/2").getOutgoingTransformation(),
        equalTo(expectedDmz));
  }

  @Test
  public void testNatDestinationAcrossInterfacesConversion() {
    String config = join(
        "interface Ethernet0/1",
        " nameif outside",
        "interface Ethernet0/2",
        " nameif dmz",
        "object network REAL_OUTSIDE",
        " host 10.0.0.50",
        "object network MAPPED_OUTSIDE",
        " host 192.0.2.50",
        "object network REAL_DMZ",
        " host 10.0.1.50",
        "object network MAPPED_DMZ",
        " host 192.0.2.60",
        "nat (inside,outside) destination static REAL_OUTSIDE MAPPED_OUTSIDE",
        "nat (inside,dmz) destination static REAL_DMZ MAPPED_DMZ");

    FtdConfiguration vc = parseVendorConfig(config);
    Configuration c = vc.toVendorIndependentConfigurations().get(0);

    Transformation expectedOutside = Transformation.when(
        AclLineMatchExprs.and(
            AclLineMatchExprs.matchSrcInterface("Ethernet0/1"),
            AclLineMatchExprs.matchDst(Ip.parse("192.0.2.50").toIpSpace())))
        .apply(TransformationStep.assignDestinationIp(Ip.parse("10.0.0.50")))
        .build();

    Transformation expectedDmz = Transformation.when(
        AclLineMatchExprs.and(
            AclLineMatchExprs.matchSrcInterface("Ethernet0/2"),
            AclLineMatchExprs.matchDst(Ip.parse("192.0.2.60").toIpSpace())))
        .apply(TransformationStep.assignDestinationIp(Ip.parse("10.0.1.50")))
        .build();

    assertThat(
        c.getAllInterfaces().get("Ethernet0/1").getIncomingTransformation(),
        equalTo(expectedOutside));
    assertThat(
        c.getAllInterfaces().get("Ethernet0/2").getIncomingTransformation(),
        equalTo(expectedDmz));
  }

  @Test
  public void testNatDestinationOrderingConversion() {
    String config = join(
        "interface Ethernet0/1",
        " nameif outside",
        "object network REAL_FIRST",
        " host 10.0.0.70",
        "object network MAPPED_FIRST",
        " host 192.0.2.70",
        "object network REAL_SECOND",
        " host 10.0.0.71",
        "object network MAPPED_SECOND",
        " host 192.0.2.71",
        "nat (inside,outside) destination static REAL_FIRST MAPPED_FIRST",
        "nat (inside,outside) destination static REAL_SECOND MAPPED_SECOND");

    FtdConfiguration vc = parseVendorConfig(config);
    Configuration c = vc.toVendorIndependentConfigurations().get(0);

    Transformation second = Transformation.when(
        AclLineMatchExprs.and(
            AclLineMatchExprs.matchSrcInterface("Ethernet0/1"),
            AclLineMatchExprs.matchDst(Ip.parse("192.0.2.71").toIpSpace())))
        .apply(TransformationStep.assignDestinationIp(Ip.parse("10.0.0.71")))
        .build();

    Transformation expectedIncoming = Transformation.when(
        AclLineMatchExprs.and(
            AclLineMatchExprs.matchSrcInterface("Ethernet0/1"),
            AclLineMatchExprs.matchDst(Ip.parse("192.0.2.70").toIpSpace())))
        .apply(TransformationStep.assignDestinationIp(Ip.parse("10.0.0.70")))
        .setOrElse(second)
        .build();

    assertThat(
        c.getAllInterfaces().get("Ethernet0/1").getIncomingTransformation(),
        equalTo(expectedIncoming));
  }

  @Test
  public void testNatServiceSourceTranslationUdpConversion() {
    String config = join(
        "interface Ethernet0/0",
        " nameif inside",
        "object network REAL_UDP",
        " host 10.0.0.40",
        "object network MAPPED_UDP",
        " host 192.0.2.40",
        "nat (inside,outside) source static REAL_UDP MAPPED_UDP service udp 5555 6666");

    FtdConfiguration vc = parseVendorConfig(config);
    Configuration c = vc.toVendorIndependentConfigurations().get(0);

    Transformation expectedOutgoing = Transformation.when(
        AclLineMatchExprs.and(
            AclLineMatchExprs.matchSrcInterface("Ethernet0/0"),
            AclLineMatchExprs.matchSrc(Ip.parse("10.0.0.40").toIpSpace()),
            AclLineMatchExprs.matchIpProtocol(IpProtocol.UDP),
            AclLineMatchExprs.matchSrcPort(5555)))
        .apply(
            ImmutableList.of(
                TransformationStep.assignSourceIp(Ip.parse("192.0.2.40")),
                TransformationStep.assignSourcePort(6666)))
        .build();

    assertThat(
        c.getAllInterfaces().get("Ethernet0/0").getOutgoingTransformation(),
        equalTo(expectedOutgoing));
  }

  @Test
  public void testAccessGroupParsing() {
    String config = "access-group OUTSIDE_IN in interface outside\n" +
        "access-group INSIDE_OUT out interface inside\n" +
        "access-group GLOBAL_ACL global\n";

    FtdConfiguration vc = parseVendorConfig(config);

    assertThat(vc, notNullValue());
    assertThat(vc.getAccessGroups(), hasSize(3));

    boolean foundIn = false;
    boolean foundOut = false;
    boolean foundGlobal = false;

    for (FtdAccessGroup ag : vc.getAccessGroups()) {
      if (ag.getAclName().equals("OUTSIDE_IN")) {
        assertThat(ag.getDirection(), equalTo("in"));
        assertThat(ag.getInterfaceName(), equalTo("outside"));
        foundIn = true;
      } else if (ag.getAclName().equals("INSIDE_OUT")) {
        assertThat(ag.getDirection(), equalTo("out"));
        assertThat(ag.getInterfaceName(), equalTo("inside"));
        foundOut = true;
      } else if (ag.getAclName().equals("GLOBAL_ACL")) {
        assertThat(ag.getDirection(), equalTo("global"));
        assertThat(ag.getInterfaceName(), equalTo(null));
        foundGlobal = true;
      }
    }

    assertThat(foundIn, equalTo(true));
    assertThat(foundOut, equalTo(true));
    assertThat(foundGlobal, equalTo(true));
  }

  @Test
  public void testNetworkObjectParsing() {
    String config = "object network HOST_1\n" +
        " host 1.2.3.4\n" +
        "object network SUBNET_1\n" +
        " subnet 10.0.0.0 255.0.0.0\n" +
        "object network FQDN_1\n" +
        " fqdn example.com\n";
    FtdConfiguration vc = parseVendorConfig(config);

    assertThat(vc, notNullValue());
    assertThat(vc.getNetworkObjects().keySet(), hasSize(3));

    FtdNetworkObject hostObj = vc.getNetworkObjects().get("HOST_1");
    assertThat(hostObj, notNullValue());
    assertThat(hostObj.getHostIp(), equalTo(Ip.parse("1.2.3.4")));

    FtdNetworkObject subnetObj = vc.getNetworkObjects().get("SUBNET_1");
    assertThat(subnetObj, notNullValue());
    assertThat(subnetObj.getSubnetNetwork(), equalTo(Ip.parse("10.0.0.0")));
    assertThat(subnetObj.getSubnetMask(), equalTo(Ip.parse("255.0.0.0")));

    FtdNetworkObject fqdnObj = vc.getNetworkObjects().get("FQDN_1");
    assertThat(fqdnObj, notNullValue());
    assertThat(fqdnObj.getFqdn(), equalTo("example.com"));
  }

  @Test
  public void testNetworkObjectGroupParsing() {
    String config = "object-group network GROUP_1\n" +
        " network-object host 1.1.1.1\n" +
        " network-object object HOST_REF\n" +
        " group-object OTHER_GROUP\n";
    FtdConfiguration vc = parseVendorConfig(config);

    assertThat(vc, notNullValue());
    assertThat(vc.getNetworkObjectGroups().keySet(), hasSize(1));

    FtdNetworkObjectGroup group = vc.getNetworkObjectGroups().get("GROUP_1");
    assertThat(group, notNullValue());
    assertThat(group.getMembers(), hasSize(3));
  }

  @Test
  public void testServiceObjectGroupParsing() {
    String config = "object-group service tcp SVC_TCP\n" +
        " port-object eq 443\n" +
        "object-group service SVC_MIX\n" +
        " service-object tcp destination eq 80\n" +
        " service-object udp destination eq 53\n" +
        " group-object SVC_TCP\n";
    FtdConfiguration vc = parseVendorConfig(config);

    assertThat(vc, notNullValue());
    assertThat(vc.getServiceObjectGroups().keySet(), hasSize(2));

    FtdServiceObjectGroup group = vc.getServiceObjectGroups().get("SVC_MIX");
    assertThat(group, notNullValue());
    assertThat(group.getMembers(), hasSize(3));
  }

  @Test
  public void testServiceObjectGroupAclConversion() {
    String config = join(
        "object-group service tcp SVC_TCP",
        " port-object eq 443",
        "object-group service SVC_MIX",
        " service-object tcp destination eq 80",
        " service-object udp destination eq 53",
        " group-object SVC_TCP",
        "access-list ACL1 extended permit ip any any object-group SVC_MIX");
    FtdConfiguration vc = parseVendorConfig(config);
    Configuration c = vc.toVendorIndependentConfigurations().get(0);

    ExprAclLine line = (ExprAclLine) c.getIpAccessLists().get("ACL1").getLines().get(0);
    assertThat(line.getMatchCondition(), instanceOf(MatchHeaderSpace.class));

    MatchHeaderSpace matchHeaderSpace = (MatchHeaderSpace) line.getMatchCondition();
    HeaderSpace headerSpace = matchHeaderSpace.getHeaderspace();
    assertThat(headerSpace.getIpProtocols(), containsInAnyOrder(IpProtocol.TCP, IpProtocol.UDP));
    assertThat(
        headerSpace.getDstPorts(),
        containsInAnyOrder(
            new SubRange(53, 53),
            new SubRange(80, 80),
            new SubRange(443, 443)));
  }

  @Test
  public void testMpfParsing() {
    String config = join(
        "class-map type inspect CMAP1",
        " match default-inspection-traffic",
        "policy-map PMAP1",
        " class CMAP1",
        "service-policy PMAP1 global",
        "service-policy PMAP1 interface outside");
    FtdConfiguration vc = parseVendorConfig(config);

    assertThat(vc.getClassMaps().keySet(), containsInAnyOrder("CMAP1"));
    assertThat(vc.getClassMaps().get("CMAP1").getMatchLines(),
        containsInAnyOrder("match default-inspection-traffic"));
    assertThat(vc.getPolicyMaps().keySet(), containsInAnyOrder("PMAP1"));
    assertThat(vc.getPolicyMaps().get("PMAP1").getClassNames(), containsInAnyOrder("CMAP1"));
    assertThat(vc.getServicePolicies(), hasSize(2));
    assertThat(vc.getServicePolicies().get(0).getPolicyMapName(), equalTo("PMAP1"));
    assertThat(vc.getServicePolicies().get(0).getScope(), equalTo(FtdServicePolicy.Scope.GLOBAL));
    assertThat(vc.getServicePolicies().get(1).getPolicyMapName(), equalTo("PMAP1"));
    assertThat(vc.getServicePolicies().get(1).getScope(), equalTo(FtdServicePolicy.Scope.INTERFACE));
    assertThat(vc.getServicePolicies().get(1).getInterfaceName(), equalTo("outside"));
  }

  @Test
  public void testMpfClassActionsParsing() {
    String config = join(
        "policy-map PMAP1",
        " class CMAP1",
        "  inspect ftp",
        "  set connection timeout half-closed 0:10:00",
        " class CMAP2",
        "  parameters",
        "   timeout 1:00:00");
    FtdConfiguration vc = parseVendorConfig(config);

    FtdPolicyMap policyMap = vc.getPolicyMaps().get("PMAP1");
    assertThat(policyMap, notNullValue());
    assertThat(
        policyMap.getClassActionLines().get("CMAP1"),
        containsInAnyOrder(
            "inspect ftp",
            "set connection timeout half-closed 0:10:00"));
    assertThat(
        policyMap.getClassActionLines().get("CMAP2"),
        containsInAnyOrder(
            "parameters",
            "timeout 1:00:00"));
  }

  @Test
  public void testMpfConversionToAcls() {
    String config = join(
        "class-map type inspect CMAP1",
        " match default-inspection-traffic",
        "policy-map PMAP1",
        " class CMAP1",
        "service-policy PMAP1 global");
    FtdConfiguration vc = parseVendorConfig(config);
    Configuration c = vc.toVendorIndependentConfigurations().get(0);

    assertThat(c.getIpAccessLists().keySet(), hasSize(2));
    assertThat(c.getIpAccessLists().containsKey("~FTD_CLASS_MAP_ACL~CMAP1~"), equalTo(true));
    assertThat(c.getIpAccessLists().containsKey("~FTD_POLICY_MAP_ACL~PMAP1~"), equalTo(true));
  }

  @Test
  public void testAccessListParsing() {
    String config = "access-list ACL_1 extended permit ip any any\n" +
        "access-list ACL_1 extended deny tcp host 1.1.1.1 any eq 80\n" +
        "access-list ACL_1 remark This is a remark\n";
    FtdConfiguration vc = parseVendorConfig(config);

    assertThat(vc, notNullValue());
    assertThat(vc.getAccessLists().keySet(), hasSize(1));

    FtdAccessList acl = vc.getAccessLists().get("ACL_1");
    assertThat(acl, notNullValue());
    assertThat(acl.getLines(), hasSize(3));

    FtdAccessListLine line1 = acl.getLines().get(0);
    assertThat(line1.getAclType(), equalTo(FtdAccessListLine.AclType.EXTENDED));
    assertThat(line1.getAction(), equalTo(LineAction.PERMIT));
    assertThat(line1.getProtocol(), equalTo("ip"));

    FtdAccessListLine line2 = acl.getLines().get(1);
    assertThat(line2.getAclType(), equalTo(FtdAccessListLine.AclType.EXTENDED));
    assertThat(line2.getAction(), equalTo(LineAction.DENY));
    assertThat(line2.getProtocol(), equalTo("tcp"));

    FtdAccessListLine line3 = acl.getLines().get(2);
    assertThat(line3.getAclType(), equalTo(FtdAccessListLine.AclType.REMARK));
    assertThat(line3.getRemark(), equalTo("This is a remark"));
  }

  @Test
  public void testPrefilterAdvancedTrustParsing() {
    String config = join(
        "access-list PREFILTER_ACL remark rule-id 100: PREFILTER POLICY: Prefilter-Policy",
        "access-list PREFILTER_ACL advanced trust udp ifc OUTSIDE any any eq domain rule-id 101 event-log flow-end");
    FtdConfiguration vc = parseVendorConfig(config);

    assertThat(vc.getAccessLists().keySet(), hasSize(1));
    FtdAccessList acl = vc.getAccessLists().get("PREFILTER_ACL");
    assertThat(acl.getLines(), hasSize(2));

    FtdAccessListLine remarkLine = acl.getLines().get(0);
    assertThat(remarkLine.getAclType(), equalTo(FtdAccessListLine.AclType.REMARK));
    assertThat(remarkLine.getRemark(), equalTo("rule-id 100: PREFILTER POLICY: Prefilter-Policy"));

    FtdAccessListLine trustLine = acl.getLines().get(1);
    assertThat(trustLine.getAclType(), equalTo(FtdAccessListLine.AclType.ADVANCED));
    assertThat(trustLine.getAction(), equalTo(LineAction.PERMIT));
    assertThat(trustLine.getProtocol(), equalTo("udp"));
    assertThat(trustLine.getDestinationPortSpecifier(), equalTo("eq domain"));
    assertThat(trustLine.getInterfaceName(), equalTo("OUTSIDE"));
  }

  @Test
  public void testSecurityLevelDefaults() throws IOException {
    String config = join(
        "interface Ethernet0/0",
        " nameif outside",
        " security-level 0",
        "interface Ethernet0/1",
        " nameif inside",
        " security-level 100");
    FtdConfiguration vc = parseVendorConfig(config);
    Configuration c = vc.toVendorIndependentConfigurations().get(0);

    org.batfish.datamodel.Interface outside = c.getAllInterfaces().get("Ethernet0/0");
    org.batfish.datamodel.Interface inside = c.getAllInterfaces().get("Ethernet0/1");

    IpAccessList outsideAcl = outside.getIncomingFilter();
    IpAccessList insideAcl = inside.getIncomingFilter();
    assertThat(outsideAcl, notNullValue());
    assertThat(insideAcl, notNullValue());

    ExprAclLine outsideLine = (ExprAclLine) outsideAcl.getLines().get(0);
    ExprAclLine insideLine = (ExprAclLine) insideAcl.getLines().get(0);
    assertThat(outsideLine.getAction(), equalTo(LineAction.DENY));
    assertThat(insideLine.getAction(), equalTo(LineAction.PERMIT));
  }

  @Test
  public void testCryptoMapParsing() throws IOException {
    String config = join(
        "access-list VPN_ACL extended permit ip 10.0.0.0 255.255.255.0 192.168.1.0 255.255.255.0",
        "crypto map MYMAP 10 match address VPN_ACL",
        "crypto map MYMAP 10 set peer 203.0.113.1",
        "crypto map MYMAP 10 set transform-set ESP-AES-SHA",
        "crypto map MYMAP 10 set pfs group14");
    FtdConfiguration vc = parseVendorConfig(config);

    assertThat(vc.getCryptoMaps().keySet(), contains("MYMAP"));
    FtdCryptoMapSet mapSet = vc.getCryptoMaps().get("MYMAP");
    assertThat(mapSet.getEntries().keySet(), contains(10));

    FtdCryptoMapEntry entry = mapSet.getEntries().get(10);
    assertThat(entry.getAccessList(), equalTo("VPN_ACL"));
    assertThat(entry.getPeer(), equalTo(Ip.parse("203.0.113.1")));
    assertThat(entry.getTransforms(), contains("ESP-AES-SHA"));
    assertThat(entry.getPfsKeyGroup(), equalTo(DiffieHellmanGroup.GROUP14));
  }

  @Test
  public void testIkev2PolicyParsing() {
    String config = join(
        "crypto ikev2 policy 10",
        " encryption aes-256",
        " integrity sha256",
        " group 14",
        " prf sha256",
        " lifetime seconds 86400");
    FtdConfiguration vc = parseVendorConfig(config);

    assertThat(vc.getIkev2Policies().keySet(), contains(10));
    FtdIkev2Policy policy = vc.getIkev2Policies().get(10);
    assertThat(policy.getEncryptionAlgorithms(), contains(EncryptionAlgorithm.AES_256_CBC));
    assertThat(policy.getIntegrityAlgorithms(), contains(IkeHashingAlgorithm.SHA_256));
    assertThat(policy.getDhGroups(), contains(DiffieHellmanGroup.GROUP14));
    assertThat(policy.getPrfAlgorithms(), contains(IkeHashingAlgorithm.SHA_256));
    assertThat(policy.getLifetimeSeconds(), equalTo(86400));
  }

  @Test
  public void testTunnelGroupParsing() {
    String config = join(
        "tunnel-group 203.0.113.1 type ipsec-l2l",
        "tunnel-group 203.0.113.1 ipsec-attributes",
        " ikev2 remote-authentication pre-shared-key secret123",
        " ikev2 local-authentication pre-shared-key secret456");
    FtdConfiguration vc = parseVendorConfig(config);

    assertThat(vc.getTunnelGroups().keySet(), contains("203.0.113.1"));
    FtdTunnelGroup tg = vc.getTunnelGroups().get("203.0.113.1");
    assertThat(tg.getType(), equalTo(FtdTunnelGroup.Type.IPSEC_L2L));
    assertThat(tg.getPresharedKey(), equalTo("secret123"));
    assertThat(tg.getPresharedKeyStandby(), equalTo("secret456"));
  }

  @Test
  public void testIpsecTransformSetParsing() throws IOException {
    String config = "crypto ipsec transform-set ESP-AES-SHA esp-aes esp-sha-hmac\n";
    FtdConfiguration vc = parseVendorConfig(config);

    assertThat(vc.getIpsecTransformSets().keySet(), contains("ESP-AES-SHA"));
    FtdIpsecTransformSet transformSet = vc.getIpsecTransformSets().get("ESP-AES-SHA");
    assertThat(transformSet.getName(), equalTo("ESP-AES-SHA"));
    assertThat(transformSet.getEspEncryption(), equalTo(EncryptionAlgorithm.AES_128_CBC));
    assertThat(transformSet.getEspAuthentication(), equalTo(IpsecAuthenticationAlgorithm.HMAC_SHA1_96));
  }

  @Test
  public void testIpsecProfileParsing() throws IOException {
    String config = join(
        "crypto ipsec profile IPSEC_PROF",
        " set transform-set ESP-AES-SHA ESP-3DES-SHA",
        " set pfs group14");
    FtdConfiguration vc = parseVendorConfig(config);

    assertThat(vc.getIpsecProfiles().keySet(), contains("IPSEC_PROF"));
    FtdIpsecProfile profile = vc.getIpsecProfiles().get("IPSEC_PROF");
    assertThat(profile.getName(), equalTo("IPSEC_PROF"));
    assertThat(profile.getTransformSets(), contains("ESP-AES-SHA", "ESP-3DES-SHA"));
    assertThat(profile.getPfsGroup(), equalTo(DiffieHellmanGroup.GROUP14));
  }

  @Test
  public void testVpnConversion() {
    String config = join(
        "interface Ethernet0/0",
        " nameif outside",
        " ip address 198.51.100.1 255.255.255.0",
        "access-list VPN_ACL extended permit ip 10.0.0.0 255.255.255.0 192.168.1.0 255.255.255.0",
        "crypto ipsec transform-set ESP-AES-SHA esp-aes esp-sha-hmac",
        "crypto ipsec profile IPSEC_PROF",
        " set transform-set ESP-AES-SHA",
        "crypto map MYMAP 10 match address VPN_ACL",
        "crypto map MYMAP 10 set peer 203.0.113.1",
        "crypto map MYMAP 10 set transform-set ESP-AES-SHA",
        "crypto map MYMAP interface outside",
        "tunnel-group 203.0.113.1 type ipsec-l2l",
        "tunnel-group 203.0.113.1 ipsec-attributes",
        " ikev2 remote-authentication pre-shared-key secret123");
    FtdConfiguration vc = parseVendorConfig(config);
    assertThat(vc.getCryptoMaps().keySet(), contains("MYMAP"));
    assertThat(vc.getCryptoMapInterfaceBindings().get("MYMAP"), contains("outside"));
    assertThat(vc.getInterfaces().keySet(), contains("Ethernet0/0"));
    Configuration c = vc.toVendorIndependentConfigurations().get(0);
    org.batfish.datamodel.Interface vpnIface = c.getAllInterfaces().get("Ethernet0/0");
    assertThat(vpnIface, notNullValue());
    assertThat(vpnIface.getCryptoMap(), equalTo("MYMAP"));
    assertThat(vpnIface.getConcreteAddress(), notNullValue());

    assertThat(c.getIpsecPhase2Proposals().keySet(), contains("ESP-AES-SHA"));
    assertThat(
        c.getIpsecPhase2Policies().keySet(),
        containsInAnyOrder("IPSEC_PROF", "~IPSEC_PHASE2_POLICY:MYMAP:10~"));
    assertThat(c.getIkePhase1Keys().keySet(), contains("203.0.113.1"));
    assertThat(
        c.getIpsecPeerConfigs().keySet(),
        contains("~IPSEC_PEER_CONFIG:MYMAP:10:Ethernet0/0~"));
  }

  @Test
  public void testAccessGroupVendorConversion() {
    String config = join(
        "interface Ethernet1/0",
        "  nameif outside",
        "access-list OUTSIDE_IN extended permit ip any any",
        "access-group OUTSIDE_IN in interface outside");

    FtdConfiguration vc = parseVendorConfig(config);
    Configuration c = vc.toVendorIndependentConfigurations().get(0);

    org.batfish.datamodel.Interface iface = c.getAllInterfaces().get("Ethernet1/0");
    assertThat(iface, notNullValue());
    assertThat(iface.getIncomingFilter().getName(), equalTo("OUTSIDE_IN"));
  }

  @Test
  public void testNatDestinationParsing() {
    String config = "nat (inside,outside) destination static real_dest mapped_dest\n";
    FtdConfiguration vc = parseVendorConfig(config);

    assertThat(vc, notNullValue());
    assertThat(vc.getNatRules(), hasSize(1));
    FtdNatRule rule = vc.getNatRules().get(0);
    assertThat(rule.getSourceInterface(), equalTo("inside"));
    assertThat(rule.getDestinationInterface(), equalTo("outside"));
    assertThat(rule.getDestinationTranslation(), notNullValue());
  }

  @Test
  public void testNatServiceParsing() {
    String config = "nat (inside,outside) source static real_src mapped_src service http https\n";
    FtdConfiguration vc = parseVendorConfig(config);

    assertThat(vc, notNullValue());
    assertThat(vc.getNatRules(), hasSize(1));
    FtdNatRule rule = vc.getNatRules().get(0);
    assertThat(rule.getSourceTranslation(), notNullValue());
    assertThat(rule.getServiceTranslation(), notNullValue());
    assertThat(rule.getServiceTranslation().getRealService(), equalTo("http"));
    assertThat(rule.getServiceTranslation().getMappedService(), equalTo("https"));
  }

  @Test
  public void testNatCombinedParsing() {
    String config = "nat (inside,outside) source static real_src mapped_src destination static real_dest mapped_dest service http https\n";
    FtdConfiguration vc = parseVendorConfig(config);

    assertThat(vc, notNullValue());
    assertThat(vc.getNatRules(), hasSize(1));
    FtdNatRule rule = vc.getNatRules().get(0);
    assertThat(rule.getSourceTranslation(), notNullValue());
    assertThat(rule.getDestinationTranslation(), notNullValue());
    assertThat(rule.getServiceTranslation(), notNullValue());
  }

  @Test
  public void testNewStanzaTypes() {
    String config = "enable password test pbkdf2\n"
        + "names\n"
        + "cts manual\n"
        + "snort preserve-connection\n"
        + "flow-offload enable\n"
        + "Cryptochecksum:test\n";
    FtdConfiguration vc = parseVendorConfig(config);
    assertThat(vc, notNullValue());
    // Test that no warnings were generated for unrecognized lines
    assertThat(vc.getWarnings().getParseWarnings(), hasSize(0));
  }

}
