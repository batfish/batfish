package org.batfish.representation.azure;

import com.fasterxml.jackson.databind.JsonNode;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.junit.Test;

import java.io.IOException;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class VirtualMachineTest {

    @Test
    public void testDeserialization() throws IOException {
        String text = readResource("org/batfish/representation/azure/VirtualMachineTest.json", UTF_8);
        JsonNode node = BatfishObjectMapper.mapper().readTree(text);

        VirtualMachine vm = BatfishObjectMapper.mapper().convertValue(node, VirtualMachine.class);

        assertNotNull(vm);

        assertEquals("resourceGroups/test/providers/Microsoft.Compute/virtualMachines/VM2",
                vm.getId());

        assertEquals("VM2", vm.getName());

        assertEquals("Microsoft.Compute/virtualMachines", vm.getType());

        assertNotNull(vm.getProperties());
        assertNotNull(vm.getProperties().getNetworkProfile());

        assertEquals(1, vm.getProperties().getNetworkProfile().getNetworkInterfaces().size());

        for(IdReference networkInterfaceId : vm.getProperties().getNetworkProfile().getNetworkInterfaces()) {
            assertNotNull(networkInterfaceId);
            assertEquals("resourceGroups/test/providers/Microsoft.Network/networkInterfaces/vm235",
                    networkInterfaceId.getId());
        }
    }

    @Test
    public void testToConfigurationNode() throws IOException {

        ConvertedConfiguration convertedConfiguration = new ConvertedConfiguration();
        Region region = new Region("test");

        Subnet subnet = new Subnet(
                "testSubnet", "testSubnet", "testType",
                new Subnet.Properties(
                        Prefix.parse("192.168.0.0/24"), null, null, Set.of(), false
                )
        );
        region.getSubnets().put(subnet.getId(), subnet);

        NetworkInterface networkInterface = new NetworkInterface(
                "vm235", "vm235", "testType",
                new NetworkInterface.Properties(
                        Set.of(
                                new IPConfiguration(
                                        "ipconfig1", "testType", "ipconfig1",
                                        new IPConfiguration.Properties(Ip.parse("192.168.0.4"),
                                                new IdReference(subnet.getId()), null, true)
                                ),
                                new IPConfiguration(
                                        "ipconfig2", "testType", "ipconfig2",
                                        new IPConfiguration.Properties(Ip.parse("192.168.0.5"),
                                                new IdReference(subnet.getId()), null, false)
                                )
                        ),
                        "00:00:00:00:00:00",
                        null
                )
        );
        region.getInterfaces().put(networkInterface.getId(), networkInterface);

        IdReference networkInterfaceId = new IdReference("vm235");
        VirtualMachine.NetworkProfile networkProfile = new VirtualMachine.NetworkProfile(Set.of(networkInterfaceId));

        VirtualMachine.Properties properties =
                new VirtualMachine.Properties(networkProfile);

        VirtualMachine vm = new VirtualMachine("test", "test", "test", properties);
        assertNotNull(vm);

        Configuration c = vm.toConfigurationNode(region, convertedConfiguration);
        assertNotNull(c);

        assertEquals(vm.getCleanId(), c.getHostname());
        assertEquals(1, c.getAllInterfaces().size());
        for(Interface iFace : c.getAllInterfaces().values()) {
            assertNotNull(iFace);

            assertEquals("vm235", iFace.getName());

            assertEquals(2, iFace.getAllAddresses().size());
            Set<InterfaceAddress> expectedAdresses = Set.of(
                    ConcreteInterfaceAddress.create(
                            Ip.parse("192.168.0.4"),
                            subnet.getProperties().getAddressPrefix().getPrefixLength()
                    ),
                    ConcreteInterfaceAddress.create(
                            Ip.parse("192.168.0.5"),
                            subnet.getProperties().getAddressPrefix().getPrefixLength()
                    )
            );
            assertEquals(expectedAdresses, iFace.getAllAddresses());
        }

        assertNotNull(c.getDefaultVrf());
        assertEquals(1, c.getDefaultVrf().getStaticRoutes().size());
        for(StaticRoute st : c.getDefaultVrf().getStaticRoutes()) {
            assertNotNull(st);
            assertEquals(0, st.getMetric());
            assertEquals(0, st.getAdministrativeCost());
            assertEquals(Prefix.ZERO, st.getNetwork());
            assertEquals(subnet.computeInstancesIfaceIp(), st.getNextHopIp());
        }
    }
}
