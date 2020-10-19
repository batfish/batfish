package org.batfish.representation.host;

import static org.batfish.datamodel.matchers.MapMatchers.hasKeys;
import static org.batfish.representation.host.HostConfiguration.toHostInterfaces;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Map;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Ip;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Test of {@link HostConfiguration}. */
public final class HostConfigurationTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testToHostInterfacesDeserializationFromArray() throws IOException {
    String jsonText = "[{ \"name\": \"eth0\" }]";
    Map<String, HostInterface> hostInterfaces =
        toHostInterfaces(BatfishObjectMapper.mapper().readTree(jsonText));
    assertThat(hostInterfaces, hasKeys("eth0"));
  }

  @Test
  public void testToHostInterfacesDeserializationFromArrayDuplicate() throws IOException {
    String jsonText =
        "[{ \"name\": \"eth0\", \"gateway\": \"10.0.0.1\" },{ \"name\": \"eth0\", \"gateway\":"
            + " \"10.0.0.2\" }]";
    Map<String, HostInterface> hostInterfaces =
        toHostInterfaces(BatfishObjectMapper.mapper().readTree(jsonText));
    assertThat(hostInterfaces, hasKeys("eth0"));
    assertThat(hostInterfaces.get("eth0").getGateway(), equalTo(Ip.parse("10.0.0.2")));
  }

  @Test
  public void testHostInterfacesDeserializationFromObject() throws IOException {
    String jsonText = "{ \"eth0\": {\"name\": \"eth0\" } }";
    Map<String, HostInterface> hostInterfaces =
        toHostInterfaces(BatfishObjectMapper.mapper().readTree(jsonText));
    assertThat(hostInterfaces, hasKeys("eth0"));
  }

  @Test
  public void testHostInterfacesDeserializationFromObjectWithMismatch() throws IOException {
    String jsonText = "{ \"eth0\": {\"name\": \"eth1\" } }";

    _thrown.expect(IllegalArgumentException.class);
    toHostInterfaces(BatfishObjectMapper.mapper().readTree(jsonText));
  }
}
