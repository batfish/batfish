package org.batfish.representation.azure;

import com.fasterxml.jackson.databind.JsonNode;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class NetworkSecurityGroupTest {

    @Test
    public void testDeserialization() throws IOException {
        String text = readResource("org/batfish/representation/azure/NetworkSecurityGroupTest.json", UTF_8);
        JsonNode node = BatfishObjectMapper.mapper().readTree(text);

        NetworkSecurityGroup nsg = BatfishObjectMapper.mapper().convertValue(node, NetworkSecurityGroup.class);
        assertNotNull(nsg);

        assertEquals("testNsg", nsg.getName());
        assertEquals("Microsoft.Network/networkSecurityGroups", nsg.getType());
        assertEquals("resourceGroups/test/providers/Microsoft.Network/networkSecurityGroups/NSG-3",
                nsg.getId());

        NetworkSecurityGroup.Properties properties = nsg.getProperties();
        assertNotNull(properties);

        List<SecurityRule> securityRules = properties.getSecurityRules();
        assertNotNull(securityRules);



        {
            // best priority should be first (lowest)
            SecurityRule securityRule = securityRules.get(0);

            assertNotNull(securityRule);
            assertEquals("AllowCidrBlockSSHInbound", securityRule.getName());
            assertEquals("resourceGroups/test/providers/Microsoft.Network/networkSecurityGroups/" +
                            "NSG-3/securityRules/AllowCidrBlockSSHInbound",
                    securityRule.getId());
            assertEquals("Microsoft.Network/networkSecurityGroups/securityRules", securityRule.getType());

            SecurityRule.Properties securityRuleProperties = securityRule.getProperties();
            assertNotNull(securityRuleProperties);

            assertEquals(IpProtocol.fromString("TCP"), securityRuleProperties.getProtocol());
            assertEquals("Inbound", securityRuleProperties.getDirection());
            assertEquals("Allow", securityRuleProperties.getAccess());
            assertEquals(Prefix.parse("10.0.0.0/24"), securityRuleProperties.getSourceAddressPrefix());
            assertEquals(Prefix.ZERO, securityRuleProperties.getDestinationAddressPrefix()); // "*" destination
            assertEquals(100, securityRuleProperties.getPriority());

            // check prefixes ?
        }

        // check order by priority (lowest first)
        int last = 0;
        for(SecurityRule securityRule : securityRules) {
            assertTrue(last <= securityRule.getProperties().getPriority());
            last = securityRule.getProperties().getPriority();
        }

    }

    @Test
    public void testAcl(){
        {
            SecurityRule.Properties properties
                    = new SecurityRule.Properties(
                    "*",
                    "22-80",
                    new ArrayList<>(),
                    new ArrayList<>(),
                    "*",
                    "192.168.1.0/24",
                    new ArrayList<>(),
                    new ArrayList<>(),
                    "TCP",
                    "Deny",
                    100,
                    "Outbound"
            );

            SecurityRule securityRule =
                    new SecurityRule(
                            "test",
                            "testId",
                            "testType",
                            properties);



            HeaderSpace headerSpace = HeaderSpace.builder()
                    .setIpProtocols(IpProtocol.TCP)
                    .setSrcPorts(new SubRange(0,65535))
                    .setDstPorts(new SubRange(22,80))
                    .setSrcIps(Prefix.ZERO.toIpSpace())
                    .setDstIps(Prefix.parse("192.168.1.0/24").toIpSpace())
                    .build();

            ExprAclLine expectedAclLine = ExprAclLine.builder()
                    .setName("test")
                    .setAction(LineAction.DENY)
                    .setMatchCondition(new MatchHeaderSpace(headerSpace))
                    .build();

            assertEquals(expectedAclLine, securityRule.getAclLine());
        }
    }


}
