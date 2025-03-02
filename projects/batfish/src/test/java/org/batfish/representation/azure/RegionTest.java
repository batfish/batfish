package org.batfish.representation.azure;

import com.fasterxml.jackson.databind.JsonNode;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
import org.junit.Test;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.junit.Assert.assertNotNull;

public class RegionTest {

    @Test
    public void testAddConfigElement_PublicIp() throws IOException {
        Region region = new Region("test");
        String text = readResource("org/batfish/representation/azure/PublicIpAddressTest.json", UTF_8);
        JsonNode node = BatfishObjectMapper.mapper().readTree(text);

        region.addConfigElement(node, "file", new ParseVendorConfigurationAnswerElement());
        assertNotNull(region.getPublicIpAddresses().get("testId"));
    }

    @Test
    public void testAddConfigElement_VirtualMachine() throws IOException {
        Region region = new Region("test");
        String text = readResource("org/batfish/representation/azure/VirtualMachineTest.json", UTF_8);
        JsonNode node = BatfishObjectMapper.mapper().readTree(text);

        region.addConfigElement(node, "file", new ParseVendorConfigurationAnswerElement());
        assertNotNull(region.getInstances()
                .get("resourceGroups/test/providers/Microsoft.Compute/virtualMachines/VM2"));
    }

    @Test
    public void testAddConfigElement_Postgres() throws IOException {
        Region region = new Region("test");
        String text = readResource("org/batfish/representation/azure/PostgreTest.json", UTF_8);
        JsonNode node = BatfishObjectMapper.mapper().readTree(text);

        region.addConfigElement(node, "file", new ParseVendorConfigurationAnswerElement());
        assertNotNull(region.getInstances().get("testId"));
    }

    @Test
    public void testAddConfigElement_VirtualNetwork() throws IOException {
        Region region = new Region("test");
        String text = readResource("org/batfish/representation/azure/VNetTest.json", UTF_8);
        JsonNode node = BatfishObjectMapper.mapper().readTree(text);

        region.addConfigElement(node, "file", new ParseVendorConfigurationAnswerElement());
        assertNotNull(region.getVnets()
                .get("resourceGroups/test/providers/Microsoft.Network/virtualNetworks/test-vnet"));
    }

    @Test
    public void testAddConfigElement_NetworkSecurityGroup() throws IOException {
        Region region = new Region("test");
        String text = readResource("org/batfish/representation/azure/NetworkSecurityGroupTest.json", UTF_8);
        JsonNode node = BatfishObjectMapper.mapper().readTree(text);

        region.addConfigElement(node, "file", new ParseVendorConfigurationAnswerElement());
        assertNotNull(region.getNetworkSecurityGroups()
                .get("resourceGroups/test/providers/Microsoft.Network/networkSecurityGroups/NSG-3"));
    }

    @Test
    public void testAddConfigElement_NatGateways() throws IOException {
        Region region = new Region("test");
        String text = readResource("org/batfish/representation/azure/NatGatewayTest.json", UTF_8);
        JsonNode node = BatfishObjectMapper.mapper().readTree(text);

        region.addConfigElement(node, "file", new ParseVendorConfigurationAnswerElement());
        assertNotNull(region.getNatGateways().get("testId"));
    }

    @Test
    public void testAddConfigElement_ContainerGroups() throws IOException {
        Region region = new Region("test");
        String text = readResource("org/batfish/representation/azure/ContainerGroupTest.json", UTF_8);
        JsonNode node = BatfishObjectMapper.mapper().readTree(text);

        region.addConfigElement(node, "file", new ParseVendorConfigurationAnswerElement());
        assertNotNull(region.getInstances().get("testId"));
    }

    @Test
    public void testAddConfigElement_NetworkInterface() throws IOException {
        Region region = new Region("test");
        String text = readResource("org/batfish/representation/azure/NetworkInterfaceTest.json", UTF_8);
        JsonNode node = BatfishObjectMapper.mapper().readTree(text);

        region.addConfigElement(node, "file", new ParseVendorConfigurationAnswerElement());
        assertNotNull(region.getInterfaces().get(
                "resourceGroups/test/providers/Microsoft.Network/networkInterfaces/vm235"));
    }


}
