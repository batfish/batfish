package org.batfish.representation.palo_alto.application_definitions;

import static org.batfish.common.util.Resources.readResource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Ints;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.IpProtocol;
import org.batfish.representation.palo_alto.Application;
import org.batfish.representation.palo_alto.Service;

/**
 * Helper class that reads, converts, and stores all built-in application definitions for a Palo
 * Alto device.
 */
public final class ApplicationDefinitions {
  public static ApplicationDefinitions INSTANCE = new ApplicationDefinitions();

  public Map<String, ApplicationDefinition> getApplications() {
    return _applications;
  }

  private ApplicationDefinitions() {
    List<ApplicationDefinition> apps;
    try {
      apps =
          BatfishObjectMapper.ignoreUnknownMapper()
              .readValue(
                  readResource(APPLICATION_DEFINITIONS_PATH, StandardCharsets.UTF_8),
                  new TypeReference<List<ApplicationDefinition>>() {});
    } catch (JsonProcessingException e) {
      apps = ImmutableList.of();
    }
    _applications =
        apps.stream().collect(ImmutableMap.toImmutableMap(ApplicationDefinition::getName, a -> a));
  }

  @VisibleForTesting
  private static @Nonnull Application toApplication(ApplicationDefinition appDef) {
    String appName = appDef.getName();
    return Application.builder(appName)
        .setDescription(String.format("built-in application %s", appName))
        .addServices(toServices(appDef))
        .build();
  }

  @VisibleForTesting
  private static @Nonnull List<Service> toServices(ApplicationDefinition appDef) {
    String appName = appDef.getName();
    Default defaultVal = appDef.getDefault();
    assert defaultVal != null;

    if (defaultVal.getPort() != null) {
      return portToServices(appName, defaultVal.getPort());
    } else if (defaultVal.getIdentByIpProtocol() != null) {
      return ImmutableList.of(protocolToService(appName, defaultVal.getIdentByIpProtocol()));
    }
    assert defaultVal.getIdentByIcmpType() != null;
    return ImmutableList.of(icmpTypeToService(appName, defaultVal.getIdentByIcmpType()));
  }

  @VisibleForTesting
  private static @Nonnull List<Service> portToServices(String appName, Port port) {
    // TODO
    return ImmutableList.of(Service.builder(appName).build());
  }

  @VisibleForTesting
  private static @Nonnull Service protocolToService(String appName, String protocol) {
    Integer protocolNumber = Ints.tryParse(protocol);
    assert protocolNumber != null;
    return Service.builder(appName).setIpProtocol(IpProtocol.fromNumber(protocolNumber)).build();
  }

  @VisibleForTesting
  private static @Nonnull Service icmpTypeToService(String appName, String icmpType) {
    return Service.builder(appName).setIpProtocol(IpProtocol.ICMP).build();
  }

  private final Map<String, ApplicationDefinition> _applications;

  private static final String APPLICATION_DEFINITIONS_PATH =
      "org/batfish/representation/palo_alto/application_definitions/application_definitions.json";
}
