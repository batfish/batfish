package org.batfish.representation.azure;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Ip;
import org.junit.Test;

public class PublicIpAddressTest {

  @Test
  public void testDeserialization() throws IOException {
    String text = readResource("org/batfish/representation/azure/PublicIpAddressTest.json", UTF_8);
    JsonNode node = BatfishObjectMapper.mapper().readTree(text);

    PublicIpAddress publicIpAddress =
        BatfishObjectMapper.mapper().convertValue(node, PublicIpAddress.class);
    assertNotNull(publicIpAddress);

    assertEquals("testId", publicIpAddress.getId());
    assertEquals("testName", publicIpAddress.getName());
    assertEquals("Microsoft.Network/publicIPAddresses", publicIpAddress.getType());

    PublicIpAddress.Properties properties = publicIpAddress.getProperties();
    assertNotNull(properties);

    assertEquals(Ip.parse("40.91.194.198"), properties.getIpAddress());
  }
}
