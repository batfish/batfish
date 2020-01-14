package org.batfish.representation.aws;

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
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
import org.junit.Test;

/** Tests for {@link Region} */
public class RegionTest {

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
        "config1",
        new SecurityGroup(
            "sg-001",
            "security group 1",
            ImmutableList.of(
                new IpPermissions(
                    "tcp",
                    22,
                    22,
                    ImmutableList.of(Prefix.parse("2.2.2.0/24")),
                    ImmutableList.of(),
                    ImmutableList.of())),
            ImmutableList.of()));
    region.updateConfigurationSecurityGroups(
        "config1",
        new SecurityGroup(
            "sg-002",
            "security group 2",
            ImmutableList.of(
                new IpPermissions(
                    "tcp",
                    25,
                    25,
                    ImmutableList.of(Prefix.parse("2.2.2.0/24")),
                    ImmutableList.of(),
                    ImmutableList.of())),
            ImmutableList.of()));
    return region;
  }

  @Test
  public void testApplySecurityGroupAcls() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder()
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setHostname("c")
            .build();
    nf.interfaceBuilder()
        .setOwner(c)
        .setAddress(ConcreteInterfaceAddress.parse("12.12.12.0/24"))
        .build();
    Map<String, Configuration> configurationMap = ImmutableMap.of(c.getHostname(), c);
    Region region = createTestRegion();
    region.applySecurityGroupsAcls(configurationMap, new Warnings());

    System.out.println("aha");
  }
}
