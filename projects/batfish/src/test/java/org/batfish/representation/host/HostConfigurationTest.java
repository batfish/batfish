package org.batfish.representation.host;

import static org.batfish.datamodel.matchers.MapMatchers.hasKeys;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Test of {@link HostConfiguration}. */
public final class HostConfigurationTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testHostInterfacesDeserializationFromArray() throws IOException {
    String abc =
        "{\n"
            + "  \"hostname\" : \"h1\",\n"
            + "  \"hostInterfaces\" : [\n"
            + "    {\n"
            + "      \"name\": \"eth0\"\n"
            + "    }\n"
            + "  ]\n"
            + "}";
    HostConfiguration hc = BatfishObjectMapper.mapper().readValue(abc, HostConfiguration.class);
    assertThat(hc.getHostInterfaces(), hasKeys("eth0"));
  }

  @Test
  public void testHostInterfacesDeserializationFromObject() throws IOException {
    String abc =
        "{\n"
            + "  \"hostname\" : \"h1\",\n"
            + "  \"hostInterfaces\" : {\n"
            + "    \"eth0\" : {\n"
            + "      \"name\": \"eth0\"\n"
            + "    }\n"
            + "  }\n"
            + "}";
    HostConfiguration hc = BatfishObjectMapper.mapper().readValue(abc, HostConfiguration.class);
    assertThat(hc.getHostInterfaces(), hasKeys("eth0"));
  }

  @Test
  public void testHostInterfacesDeserializationFromObjectWithMismatch() throws IOException {
    String abc =
        "{\n"
            + "  \"hostname\" : \"h1\",\n"
            + "  \"hostInterfaces\" : {\n"
            + "    \"eth0\" : {\n"
            + "      \"name\": \"eth1\"\n"
            + "    }\n"
            + "  }\n"
            + "}";

    // IllegalArgumentException should be wrapped in a jackson exception
    _thrown.expectCause(instanceOf(IllegalArgumentException.class));
    BatfishObjectMapper.mapper().readValue(abc, HostConfiguration.class);
  }
}
