package org.batfish.representation.azure;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class AzureConfigurationTest {

    @Test
    public void testAzureConfiguration_noData() {
        AzureConfiguration azureConfiguration = new AzureConfiguration();
        List<Configuration> c = azureConfiguration.toVendorIndependentConfigurations();
        assertThat(c, equalTo(ImmutableList.of()));
    }

    @Test
    public void testAzureConfiguration_withData() {

    }

    @Test
    public void testToVendorConfigurations_ispConfiguration() throws IOException {


        String text = readResource("org/batfish/representation/azure/PublicIpAddressTest.json", UTF_8);
        JsonNode node = BatfishObjectMapper.mapper().readTree(text);

        AzureConfiguration azureConfiguration = new AzureConfiguration();
        azureConfiguration.addConfigElement(node, "testIp", new ParseVendorConfigurationAnswerElement());

        azureConfiguration.getIspConfiguration().g;
    }
}
