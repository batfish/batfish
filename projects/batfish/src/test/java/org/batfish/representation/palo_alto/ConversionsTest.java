package org.batfish.representation.palo_alto;

import static com.google.common.collect.Iterators.getOnlyElement;
import static java.util.Collections.emptySet;
import static org.batfish.representation.palo_alto.Conversions.definitionToApp;
import static org.batfish.representation.palo_alto.Conversions.portsStringToIntegerSpace;
import static org.batfish.representation.palo_alto.application_definitions.TestApplicationDefinitions.createApplicationDefinition;
import static org.batfish.representation.palo_alto.application_definitions.TestApplicationDefinitions.createDefault;
import static org.batfish.representation.palo_alto.application_definitions.TestApplicationDefinitions.createPort;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import java.util.Map;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.representation.palo_alto.application_definitions.ApplicationDefinition;
import org.batfish.representation.palo_alto.application_definitions.ApplicationDefinitions;
import org.batfish.representation.palo_alto.application_definitions.Default;
import org.batfish.representation.palo_alto.application_definitions.Port;
import org.junit.Test;

/** Test of {@link Conversions}. */
public class ConversionsTest {
  @Test
  public void testBuiltinAppConversion() {
    Map<String, ApplicationDefinition> appDefs = ApplicationDefinitions.INSTANCE.getApplications();
    // Should not crash / no asserts should fail
    Map<String, Application> apps =
        appDefs.values().stream()
            .map(Conversions::definitionToApp)
            .collect(ImmutableMap.toImmutableMap(Application::getName, a -> a));
    assertThat(apps, not(anEmptyMap()));
  }

  @Test
  public void testBuiltinAppContainers() {
    Multimap<String, String> appContainers =
        ApplicationDefinitions.INSTANCE.getApplicationContainers();
    // Should have some containers
    assertThat(appContainers.keySet(), not(emptySet()));
  }

  @Test
  public void testDefinitionToApp() {
    // IP protocol
    {
      Default def = createDefault(null, "19", null);
      Application app =
          definitionToApp(createApplicationDefinition("name", null, null, null, null, def));

      Service service = getOnlyElement(app.getServices().iterator());
      assertThat(service.getProtocol(), equalTo(IpProtocol.fromNumber(19)));
      assertNull(service.getIcmpType());
      assertThat(service.getPorts(), equalTo(IntegerSpace.EMPTY));
    }
    // ICMP type
    {
      Default def = createDefault(null, null, "8");
      Application app =
          definitionToApp(createApplicationDefinition("name", null, null, null, null, def));

      Service service = getOnlyElement(app.getServices().iterator());
      assertThat(service.getProtocol(), equalTo(IpProtocol.ICMP));
      assertThat(service.getIcmpType(), equalTo(8));
      assertThat(service.getPorts(), equalTo(IntegerSpace.EMPTY));
    }
    // TCP port
    {
      Port port = createPort(ImmutableList.of("tcp/443"));
      Default def = createDefault(port, null, null);
      Application app =
          definitionToApp(createApplicationDefinition("name", null, null, null, null, def));

      Service service = getOnlyElement(app.getServices().iterator());
      assertThat(service.getProtocol(), equalTo(IpProtocol.TCP));
      assertNull(service.getIcmpType());
      assertNull(service.getIcmpType());
      assertThat(service.getPorts(), equalTo(IntegerSpace.of(443)));
    }
  }

  @Test
  public void testPortsStringToIntegerSpace() {
    assertThat(portsStringToIntegerSpace("443"), equalTo(IntegerSpace.of(443)));
    assertThat(
        portsStringToIntegerSpace("443,445"),
        equalTo(IntegerSpace.builder().including(443, 445).build()));
    assertThat(
        portsStringToIntegerSpace("443-445"),
        equalTo(IntegerSpace.builder().including(443, 444, 445).build()));
    assertThat(
        portsStringToIntegerSpace("443-445, 4444"),
        equalTo(IntegerSpace.builder().including(443, 444, 445, 4444).build()));
    assertThat(portsStringToIntegerSpace("any"), equalTo(IntegerSpace.PORTS));
    assertThat(portsStringToIntegerSpace("dynamic"), equalTo(IntegerSpace.PORTS));
    assertThat(portsStringToIntegerSpace("80, dynamic"), equalTo(IntegerSpace.PORTS));
  }
}
