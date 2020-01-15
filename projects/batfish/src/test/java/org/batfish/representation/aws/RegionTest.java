package org.batfish.representation.aws;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
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
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
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
            "security group 1",
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
            "security group 2",
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
    assertThat(
        c.getIpAccessLists().get("~INGRESS-security group 1-sg-001~").getLines(),
        equalTo(
            ImmutableList.of(
                ExprAclLine.accepting()
                    .setName("sg-001 - security group 1 [ingress] 0")
                    .setMatchCondition(
                        new MatchHeaderSpace(
                            HeaderSpace.builder()
                                .setDstPorts(SubRange.singleton(22))
                                .setIpProtocols(IpProtocol.TCP)
                                .setSrcIps(ImmutableSet.of(IpWildcard.parse("2.2.2.0/24")))
                                .build()))
                    .build())));
    assertThat(
        c.getIpAccessLists().get("~INGRESS-security group 2-sg-002~").getLines(),
        equalTo(
            ImmutableList.of(
                ExprAclLine.accepting()
                    .setName("sg-002 - security group 2 [ingress] 0")
                    .setMatchCondition(
                        new MatchHeaderSpace(
                            HeaderSpace.builder()
                                .setDstPorts(SubRange.singleton(25))
                                .setIpProtocols(IpProtocol.TCP)
                                .setSrcIps(ImmutableSet.of(IpWildcard.parse("2.2.2.0/24")))
                                .build()))
                    .build())));

    // incoming filter on the interface refers to the two ACLs using AclAclLines
    assertThat(
        c.getAllInterfaces().get("~Interface_0~").getIncomingFilter().getLines(),
        equalTo(
            ImmutableList.of(
                new AclAclLine(
                    "Permitted by security group 2", "~INGRESS-security group 2-sg-002~"),
                new AclAclLine(
                    "Permitted by security group 1", "~INGRESS-security group 1-sg-001~"))));
  }
}
