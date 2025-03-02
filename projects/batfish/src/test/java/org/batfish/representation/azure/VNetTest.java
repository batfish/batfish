package org.batfish.representation.azure;

import com.fasterxml.jackson.databind.JsonNode;
import org.batfish.common.topology.Layer1Edge;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class VNetTest {
    @Test
    public void testDeserialization() throws IOException {
        String text = readResource("org/batfish/representation/azure/VNetTest.json", UTF_8);
        JsonNode node = BatfishObjectMapper.mapper().readTree(text);

        VNet vnet = BatfishObjectMapper.mapper().convertValue(node, VNet.class);
        assertNotNull(vnet);

        assertEquals("test-vnet",
                vnet.getName());

        assertEquals("resourceGroups/test/providers/Microsoft.Network/virtualNetworks/test-vnet",
                vnet.getId());

        assertEquals("Microsoft.Network/virtualNetworks",
                vnet.getType());

        VNet.Properties vNetProperties = vnet.getProperties();
        assertNotNull(vNetProperties);

        assertEquals(vNetProperties.getAddressSpace().getAddressPrefixes().get(0), Prefix.parse("10.0.0.0/16"));
    }

    @Test
    public void testToConfigurationNode(){

        ConvertedConfiguration convertedConfiguration = new ConvertedConfiguration();
        Region region = new Region("test");

        Subnet.Properties subnetProperties = new Subnet.Properties(
                Prefix.parse("10.0.0.0/24"),
                null,
                null,
                new HashSet<>(),
                false
        );

        Subnet.Properties subnetProperties2 = new Subnet.Properties(
                Prefix.parse("10.0.1.0/24"),
                null,
                null,
                new HashSet<>(),
                false
        );

        Set<Subnet> subnets = new HashSet<>();
        subnets.add(new Subnet("testSubnet1","testSubnet1","testSubnet1",subnetProperties));
        subnets.add(new Subnet("testSubnet2","testSubnet2","testSubnet2",subnetProperties2));

        for(Subnet subnet : subnets){
            region.getSubnets().put(subnet.getId(), subnet);
            convertedConfiguration.addNode(subnet.toConfigurationNode(region, convertedConfiguration));
        }

        List<Prefix> prefixes = new ArrayList<>();
        prefixes.add(Prefix.parse("10.0.0.0/16"));
        VNet.Properties vNetProperties = new VNet.Properties(
                new VNet.AddressSpace(prefixes),
                subnets
        );

        VNet vnet = new VNet("testId", "testName", "testType", vNetProperties);
        Configuration c = vnet.toConfigurationNode(region, convertedConfiguration);

        // hostname is set to lowercase when Configuration.setHostname()
        assertEquals("testid", c.getHostname());

        assertEquals(2, c.getAllInterfaces().size());
        assertTrue(c.getDefaultVrf().getStaticRoutes().size() >= 2);

        for(Subnet subnet : subnets){
            Interface iface = c.getAllInterfaces().get(subnet.getNodeName());
            assertNotNull(iface);
            assertEquals(subnet.getNodeName(), iface.getName());

            boolean foundRouteToSubnet = false;
            for(StaticRoute st : c.getDefaultVrf().getStaticRoutes()){
                if(st.getNetwork().equals(subnet.getProperties().getAddressPrefix())){
                    foundRouteToSubnet = true;
                    break;
                }
            }
            assertTrue(foundRouteToSubnet);

            boolean foundEdgeToSubnet = false;
            for(Layer1Edge edge : convertedConfiguration.getLayer1Edges()) {
                if(edge.getNode1().getHostname().equals(subnet.getNodeName())){
                    foundEdgeToSubnet = true;
                }
            }
            assertTrue(foundEdgeToSubnet);
        }

    }
}
