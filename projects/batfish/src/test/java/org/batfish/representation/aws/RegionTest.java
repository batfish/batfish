package org.batfish.representation.aws;

import static org.batfish.datamodel.IpProtocol.TCP;
import static org.batfish.datamodel.matchers.TraceTreeMatchers.hasChildren;
import static org.batfish.datamodel.matchers.TraceTreeMatchers.hasTraceElement;
import static org.batfish.representation.aws.Region.computeAntiSpoofingFilter;
import static org.batfish.representation.aws.Utils.getTraceElementForRule;
import static org.batfish.representation.aws.Utils.getTraceElementForSecurityGroup;
import static org.batfish.representation.aws.Utils.traceElementForAddress;
import static org.batfish.representation.aws.Utils.traceElementForDstPorts;
import static org.batfish.representation.aws.Utils.traceElementForProtocol;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.batfish.common.Warning;
import org.batfish.common.Warnings;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.AclAclLine;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.acl.AclLineEvaluator;
import org.batfish.datamodel.acl.AclTrace;
import org.batfish.datamodel.acl.AclTracer;
import org.batfish.datamodel.acl.TraceEvent;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
import org.batfish.datamodel.matchers.DataModelMatchers;
import org.batfish.datamodel.trace.TraceTree;
import org.batfish.representation.aws.IpPermissions.AddressType;
import org.batfish.representation.aws.IpPermissions.IpRange;
import org.junit.Test;

/** Tests for {@link Region} */
public class RegionTest {

  public static final String CONFIGURATION_NAME = "c";

  /** Test that we don't warn the user upon encountering an empty list with an unknown key type */
  @Test
  public void testAddConfigElementUnknownKeyEmptyList() throws IOException {

    JsonNode json = BatfishObjectMapper.mapper().readTree("{ \"stranger\" :  [] }");

    ParseVendorConfigurationAnswerElement pvcae = new ParseVendorConfigurationAnswerElement();
    Region region = new Region("r1");
    region.addConfigElement(json, null, pvcae);

    assertTrue(pvcae.getWarnings().isEmpty());
  }

  /** Test that we warn the user upon encountering a non-empty list with an unknown key type */
  @Test
  public void testAddConfigElementUnknownKeyNonEmptyList() throws IOException {

    JsonNode json = BatfishObjectMapper.mapper().readTree("{ \"stranger\" :  [1] }");

    ParseVendorConfigurationAnswerElement pvcae = new ParseVendorConfigurationAnswerElement();
    Region region = new Region("r1");
    region.addConfigElement(json, null, pvcae);

    Warning warning =
        Iterables.getOnlyElement(
            Iterables.getOnlyElement(pvcae.getWarnings().values()).getUnimplementedWarnings());
    assertTrue(warning.getText().startsWith("Unrecognized element"));
  }

  /** Test that we warn the user upon encountering a non-list with an unknown key type */
  @Test
  public void testAddConfigElementUnknownKeyNonList() throws IOException {

    JsonNode json = BatfishObjectMapper.mapper().readTree("{ \"stranger\" :  {} }");

    ParseVendorConfigurationAnswerElement pvcae = new ParseVendorConfigurationAnswerElement();
    Region region = new Region("r1");
    region.addConfigElement(json, null, pvcae);

    Warning warning =
        Iterables.getOnlyElement(
            Iterables.getOnlyElement(pvcae.getWarnings().values()).getUnimplementedWarnings());
    assertTrue(warning.getText().startsWith("Unrecognized element"));
  }

  private static Region createTestRegion() {
    Region region = new Region("test");

    // add two security groups
    region.updateConfigurationSecurityGroups(
        CONFIGURATION_NAME,
        new SecurityGroup(
            "sg-001",
            "sg-1",
            ImmutableList.of(),
            ImmutableList.of(
                new IpPermissions(
                    "tcp",
                    22,
                    22,
                    ImmutableList.of(new IpRange(Prefix.parse("2.2.2.0/24"))),
                    ImmutableList.of(),
                    ImmutableList.of())),
            "vpc"));
    region.updateConfigurationSecurityGroups(
        CONFIGURATION_NAME,
        new SecurityGroup(
            "sg-002",
            "sg-2",
            ImmutableList.of(),
            ImmutableList.of(
                new IpPermissions(
                    "tcp",
                    25,
                    25,
                    ImmutableList.of(new IpRange(Prefix.parse("2.2.2.0/24"))),
                    ImmutableList.of(),
                    ImmutableList.of())),
            "vpc"));

    return region;
  }

  @Test
  public void testApplyInstanceInterfaceAcls() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder()
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setHostname(CONFIGURATION_NAME)
            .build();
    Interface iface =
        nf.interfaceBuilder()
            .setOwner(c)
            .setAddress(ConcreteInterfaceAddress.parse("12.12.12.0/24"))
            .build();
    Map<String, Configuration> configurationMap = ImmutableMap.of(c.getHostname(), c);
    Region region = createTestRegion();
    region.applyInstanceInterfaceAcls(configurationMap, new Warnings());

    // security groups sg-001 and sg-002 converted to ExprAclLines
    assertThat(c.getIpAccessLists(), hasKey("~INGRESS~SECURITY-GROUP~sg-1~sg-001~"));
    assertThat(c.getIpAccessLists(), hasKey("~INGRESS~SECURITY-GROUP~sg-2~sg-002~"));

    assertThat(c.getIpAccessLists(), not(hasKey("~EGRESS~SECURITY-GROUP~sg-1~sg-001~")));
    assertThat(c.getIpAccessLists(), not(hasKey("~EGRESS~SECURITY-GROUP~sg-2~sg-002~")));

    // incoming and outgoing filter on the interface refers to the two ACLs using AclAclLines
    assertThat(
        c.getAllInterfaces().get("~Interface_0~").getIncomingFilter().getLines(),
        equalTo(
            ImmutableList.of(
                new AclAclLine(
                    "Security Group sg-1",
                    "~INGRESS~SECURITY-GROUP~sg-1~sg-001~",
                    getTraceElementForSecurityGroup("sg-1")),
                new AclAclLine(
                    "Security Group sg-2",
                    "~INGRESS~SECURITY-GROUP~sg-2~sg-002~",
                    getTraceElementForSecurityGroup("sg-2")))));

    assertThat(
        c.getAllInterfaces().get("~Interface_0~").getOutgoingFilter().getLines(),
        equalTo(ImmutableList.of(computeAntiSpoofingFilter(iface))));
  }

  @Test
  public void testSecurityGroupAclTracer() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder()
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setHostname(CONFIGURATION_NAME)
            .build();
    nf.interfaceBuilder()
        .setOwner(c)
        .setAddress(ConcreteInterfaceAddress.parse("12.12.12.0/24"))
        .build();
    Map<String, Configuration> configurationMap = ImmutableMap.of(c.getHostname(), c);
    Region region = createTestRegion();
    region.applyInstanceInterfaceAcls(configurationMap, new Warnings());
    IpAccessList ingressAcl = c.getIpAccessLists().get("~SECURITY_GROUP_INGRESS_ACL~");

    Flow permittedFlow =
        Flow.builder()
            .setIpProtocol(TCP)
            .setSrcPort(22)
            .setSrcIp(Ip.parse("2.2.2.2"))
            .setDstPort(22)
            .setIngressNode("c")
            .build();
    Flow deniedFlow =
        Flow.builder()
            .setIpProtocol(TCP)
            .setSrcPort(23)
            .setSrcIp(Ip.parse("2.2.2.2"))
            .setDstPort(23)
            .setIngressNode("c")
            .build();
    List<TraceTree> root =
        AclTracer.trace(
            ingressAcl,
            permittedFlow,
            null,
            c.getIpAccessLists(),
            ImmutableMap.of(),
            ImmutableMap.of());

    assertThat(
        root,
        contains(
            allOf(
                hasTraceElement(getTraceElementForSecurityGroup("sg-1")),
                hasChildren(
                    contains(
                        allOf(
                            hasTraceElement(getTraceElementForRule(null)),
                            hasChildren(
                                containsInAnyOrder(
                                    hasTraceElement(traceElementForProtocol(TCP)),
                                    hasTraceElement(traceElementForDstPorts(22, 22)),
                                    hasTraceElement(
                                        traceElementForAddress(
                                            "source", "2.2.2.0/24", AddressType.CIDR_IP))))))))));
    AclTrace trace = new AclTrace(root);
    assertThat(
        trace,
        DataModelMatchers.hasEvents(
            contains(
                TraceEvent.of(getTraceElementForSecurityGroup("sg-1")),
                TraceEvent.of(getTraceElementForRule(null)),
                TraceEvent.of(traceElementForProtocol(TCP)),
                TraceEvent.of(traceElementForDstPorts(22, 22)),
                TraceEvent.of(
                    traceElementForAddress("source", "2.2.2.0/24", AddressType.CIDR_IP)))));

    root =
        AclTracer.trace(
            ingressAcl,
            deniedFlow,
            null,
            c.getIpAccessLists(),
            ImmutableMap.of(),
            ImmutableMap.of());
    assertThat(root, empty());
  }

  @Test
  public void testComputeAntiSpoofingFilter() {
    Ip validSourceIp = Ip.parse("10.10.10.10");
    Interface iface =
        Interface.builder()
            .setName("test")
            .setAddresses(ConcreteInterfaceAddress.create(validSourceIp, Prefix.MAX_PREFIX_LENGTH))
            .build();
    AclLine antiSpoofingLine = computeAntiSpoofingFilter(iface);
    assertThat(
        new AclLineEvaluator(
                Flow.builder().setSrcIp(validSourceIp).setIngressNode("aa").build(),
                "blah",
                ImmutableMap.of(),
                ImmutableMap.of())
            .visit(antiSpoofingLine),
        nullValue()); // pass through
    assertThat(
        new AclLineEvaluator(
                Flow.builder().setSrcIp(Ip.parse("6.6.6.6")).setIngressNode("aa").build(),
                "blah",
                ImmutableMap.of(),
                ImmutableMap.of())
            .visit(antiSpoofingLine),
        equalTo(LineAction.DENY));
  }
}
