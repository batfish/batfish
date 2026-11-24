package org.batfish.representation.host;

import static org.batfish.datamodel.matchers.MapMatchers.hasKeys;
import static org.batfish.representation.host.HostConfiguration.toHostInterfaces;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.Map;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Ip;
import org.junit.jupiter.api.Test;

/** Test of {@link HostConfiguration}. */
final class HostConfigurationTest {

  @Test
  void testToHostInterfacesDeserializationFromArray() throws IOException {
    String jsonText = "[{ \"name\": \"eth0\" }]";
    Map<String, HostInterface> hostInterfaces =
        toHostInterfaces(BatfishObjectMapper.mapper().readTree(jsonText));
    assertThat(hostInterfaces, hasKeys("eth0"));
  }

  @Test
  void testToHostInterfacesDeserializationFromArrayDuplicate() throws IOException {
    String jsonText =
        "[{ \"name\": \"eth0\", \"gateway\": \"10.0.0.1\" },{ \"name\": \"eth0\", \"gateway\":"
            + " \"10.0.0.2\" }]";
    Map<String, HostInterface> hostInterfaces =
        toHostInterfaces(BatfishObjectMapper.mapper().readTree(jsonText));
    assertThat(hostInterfaces, hasKeys("eth0"));
    assertThat(hostInterfaces.get("eth0").getGateway(), equalTo(Ip.parse("10.0.0.2")));
  }

  @Test
  void testHostInterfacesDeserializationFromObject() throws IOException {
    String jsonText = "{ \"eth0\": {\"name\": \"eth0\" } }";
    Map<String, HostInterface> hostInterfaces =
        toHostInterfaces(BatfishObjectMapper.mapper().readTree(jsonText));
    assertThat(hostInterfaces, hasKeys("eth0"));
  }

  @Test
  void testHostInterfacesDeserializationFromObjectWithMismatch() {
    String jsonText = "{ \"eth0\": {\"name\": \"eth1\" } }";

    assertThrows(
        IllegalArgumentException.class,
        () -> toHostInterfaces(BatfishObjectMapper.mapper().readTree(jsonText)));
  }
}
