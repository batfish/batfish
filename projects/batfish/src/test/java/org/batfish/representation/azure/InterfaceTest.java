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
import static org.junit.Assert.assertTrue;

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

        NetworkInterface.Properties networkInterfaceProperties = networkInterface.getProperties();
        assertNotNull(networkInterfaceProperties);

        Set<IPConfiguration> ipConfigurations = networkInterfaceProperties.getIPConfigurations();

        boolean found1 = false;
        boolean found2 = false;
        for (IPConfiguration ipConfiguration : ipConfigurations) {
            assertNotNull(ipConfiguration);

            if(ipConfiguration.getName().equals("ipconfig1")) {
                found1 = true;
                IPConfiguration.Properties ipConfigurationProperties = ipConfiguration.getProperties();
                assertNotNull(ipConfigurationProperties);

                assertEquals(Ip.parse("10.0.1.4"),ipConfigurationProperties.getPrivateIpAddress());
                assertEquals("resourceGroups/test/providers/Microsoft.Network/virtualNetworks/VM1-vnet/subnets/private",
                        ipConfigurationProperties.getSubnetId());
            }

            if(ipConfiguration.getName().equals("ipconfig2")) {
                found2 = true;
                IPConfiguration.Properties ipConfigurationProperties = ipConfiguration.getProperties();
                assertNotNull(ipConfigurationProperties);

                assertEquals(Ip.parse("10.0.1.5"),ipConfigurationProperties.getPrivateIpAddress());
                assertEquals("resourceGroups/test/providers/Microsoft.Network/virtualNetworks/VM1-vnet/subnets/private",
                        ipConfigurationProperties.getSubnetId());
            }
        }

        assertTrue(found1);
        assertTrue(found2);
    }
}
