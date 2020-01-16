package org.batfish.representation.aws;

import static org.batfish.datamodel.IpProtocol.TCP;
import static org.batfish.datamodel.acl.TraceElements.defaultDeniedByIpAccessList;
import static org.batfish.datamodel.acl.TraceElements.permittedByAclLine;
import static org.batfish.datamodel.acl.TraceNodeMatchers.hasTraceElement;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasEvents;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import java.io.IOException;
import java.util.Map;
import org.batfish.common.Warning;
import org.batfish.common.Warnings;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.AclAclLine;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.acl.AclTrace;
import org.batfish.datamodel.acl.AclTracer;
import org.batfish.datamodel.acl.TraceEvent;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
import org.batfish.datamodel.trace.TraceNode;
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
                    ImmutableList.of(Prefix.parse("2.2.2.0/24")),
                    ImmutableList.of(),
                    ImmutableList.of()))));
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
                    ImmutableList.of(Prefix.parse("2.2.2.0/24")),
                    ImmutableList.of(),
                    ImmutableList.of()))));

    return region;
  }

  @Test
  public void testApplySecurityGroupAcls() {
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
    region.applySecurityGroupsAcls(configurationMap, new Warnings());

    // security groups sg-001 and sg-002 converted to ExprAclLines
    assertThat(c.getIpAccessLists(), hasKey("~INGRESS~sg-1~sg-001~"));
    assertThat(c.getIpAccessLists(), hasKey("~INGRESS~sg-2~sg-002~"));

    assertThat(c.getIpAccessLists(), hasKey("~EGRESS~sg-1~sg-001~"));
    assertThat(c.getIpAccessLists(), hasKey("~EGRESS~sg-2~sg-002~"));

    // incoming and outgoing filter on the interface refers to the two ACLs using AclAclLines
    assertThat(
        c.getAllInterfaces().get("~Interface_0~").getIncomingFilter().getLines(),
        equalTo(
            ImmutableList.of(
                new AclAclLine("sg-2", "~INGRESS~sg-2~sg-002~"),
                new AclAclLine("sg-1", "~INGRESS~sg-1~sg-001~"))));

    assertThat(
        c.getAllInterfaces().get("~Interface_0~").getOutgoingFilter().getLines(),
        equalTo(
            ImmutableList.of(
                new AclAclLine("sg-2", "~EGRESS~sg-2~sg-002~"),
                new AclAclLine("sg-1", "~EGRESS~sg-1~sg-001~"))));
  }

  @Test
  public void testSecurityGroupAclTracer() {
    String ingressAclName = "~SECURITY_GROUP_INGRESS_ACL~";
    String ingressSg2AclName = "~INGRESS-sg-2-sg-002~";
    String ingressSg1AclName = "~INGRESS-sg-1-sg-001~";

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
    region.applySecurityGroupsAcls(configurationMap, new Warnings());
    IpAccessList ingressAcl = c.getIpAccessLists().get("~SECURITY_GROUP_INGRESS_ACL~");

    Map<String, IpAccessList> availableAcls =
        ImmutableMap.of(
            ingressAclName,
            ingressAcl,
            ingressSg1AclName,
            c.getIpAccessLists().get(ingressSg1AclName),
            ingressSg2AclName,
            c.getIpAccessLists().get(ingressSg2AclName));
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
    TraceNode root =
        AclTracer.trace(
            ingressAcl, permittedFlow, null, availableAcls, ImmutableMap.of(), ImmutableMap.of());
    assertThat(root, hasTraceElement(permittedByAclLine(ingressAcl, 1)));
    AclTrace trace = new AclTrace(root);
    assertThat(trace, hasEvents(contains(TraceEvent.of(permittedByAclLine(ingressAcl, 1)))));

    root =
        AclTracer.trace(
            ingressAcl, deniedFlow, null, availableAcls, ImmutableMap.of(), ImmutableMap.of());
    assertThat(root, hasTraceElement(defaultDeniedByIpAccessList(ingressAcl)));
  }
}
