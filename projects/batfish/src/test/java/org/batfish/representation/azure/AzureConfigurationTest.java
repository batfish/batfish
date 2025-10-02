package org.batfish.representation.azure;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.List;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.isp_configuration.BorderInterfaceInfo;
import org.batfish.datamodel.isp_configuration.IspAnnouncement;
import org.batfish.datamodel.isp_configuration.IspConfiguration;
import org.batfish.datamodel.isp_configuration.IspNodeInfo;
import org.junit.Test;

public class AzureConfigurationTest {

  @Test
  public void testAzureConfiguration_noData() {
    AzureConfiguration azureConfiguration = new AzureConfiguration();
    List<Configuration> c = azureConfiguration.toVendorIndependentConfigurations();
    assertThat(c, equalTo(ImmutableList.of()));
  }

  @Test
  public void testAzureConfiguration_addConfigElement() throws IOException {
    AzureConfiguration azureConfiguration = new AzureConfiguration();

    String text = readResource("org/batfish/representation/azure/PublicIpAddressTest.json", UTF_8);
    JsonNode node = BatfishObjectMapper.mapper().readTree(text);

    azureConfiguration.addConfigElement(
        node, "publicIp", new ParseVendorConfigurationAnswerElement());

    assertNotNull(
        azureConfiguration.addOrGetRegion("westeurope").getPublicIpAddresses().get("testId"));
  }

  @Test
  public void testToVendorConfigurations_ispConfiguration() throws IOException {

    AzureConfiguration azureConfiguration = new AzureConfiguration();

    String text = readResource("org/batfish/representation/azure/PublicIpAddressTest.json", UTF_8);
    JsonNode node = BatfishObjectMapper.mapper().readTree(text);

    text = readResource("org/batfish/representation/azure/NatGatewayTest.json", UTF_8);
    JsonNode node2 = BatfishObjectMapper.mapper().readTree(text);

    azureConfiguration.addConfigElement(
        node, "testIp", new ParseVendorConfigurationAnswerElement());
    azureConfiguration.addConfigElement(
        node2, "testIp", new ParseVendorConfigurationAnswerElement());

    IspConfiguration ispConfiguration = azureConfiguration.getIspConfiguration();
    assertEquals(
        List.of(
            new BorderInterfaceInfo(
                NodeInterfacePair.of("testId", AzureConfiguration.BACKBONE_FACING_INTERFACE_NAME))),
        ispConfiguration.getBorderInterfaces());

    assertEquals(
        List.of(
            new IspNodeInfo(
                AzureConfiguration.AZURE_BACKBONE_ASN,
                AzureConfiguration.AZURE_BACKBONE_HUMAN_NAME,
                List.of(new IspAnnouncement(Ip.parse("40.91.194.198").toPrefix())))),
        ispConfiguration.getIspNodeInfos());
  }
}
