package org.batfish.representation.azure;

import com.fasterxml.jackson.databind.JsonNode;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Ip;
import org.junit.Test;

import java.io.IOException;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class InterfaceTest {

    @Test
    public void testDeserialization() throws IOException {
        String text = readResource("org/batfish/representation/azure/NetworkInterfaceTest.json", UTF_8);
        JsonNode node = BatfishObjectMapper.mapper().readTree(text);

        NetworkInterface networkInterface = BatfishObjectMapper.mapper().convertValue(node, NetworkInterface.class);
        assertNotNull(networkInterface);

        assertEquals("resourceGroups/test/providers/Microsoft.Network/networkInterfaces/vm235",
                networkInterface.getId());
        assertEquals("vm235", networkInterface.getName());
        assertEquals("Microsoft.Network/networkInterfaces", networkInterface.getType());

        NetworkInterface.NetworkInterfaceProperties networkInterfaceProperties = networkInterface.getProperties();
        assertNotNull(networkInterfaceProperties);

        Set<IPConfiguration> ipConfigurations = networkInterfaceProperties.getIPConfigurations();

        // only 1 ipConfiguration supported
        for (IPConfiguration ipConfiguration : ipConfigurations) {
            assertNotNull(ipConfiguration);
            IPConfiguration.IPConfigurationProperties ipConfigurationProperties = ipConfiguration.getProperties();
            assertEquals(Ip.parse("10.0.1.4"),ipConfigurationProperties.getPrivateIpAddress());
            assertEquals("resourceGroups/test/providers/Microsoft.Network/virtualNetworks/VM1-vnet/subnets/private",
                    ipConfigurationProperties.getSubnetId());
        }
    }
}
