package org.batfish.specifier;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.IpSpace;
import org.batfish.role.NodeRole;

public interface SpecifierContext {
  @Nonnull
  Map<String, Configuration> getConfigs();

  @Nonnull
  Set<NodeRole> getNodeRolesByDimension(String dimension);

  Map<String, Map<String, IpSpace>> getInterfaceOwnedIps();

  default IpSpace getInterfaceOwnedIps(String hostname, String iface) {
    return getInterfaceOwnedIps()
        .getOrDefault(hostname, ImmutableMap.of())
        .getOrDefault(iface, EmptyIpSpace.INSTANCE);
  }

  Map<String, Map<String, IpSpace>> getVrfOwnedIps();

  default IpSpace getVrfOwnedIps(String hostname, String vrf) {
    return getVrfOwnedIps()
        .getOrDefault(hostname, ImmutableMap.of())
        .getOrDefault(vrf, EmptyIpSpace.INSTANCE);
  }
}
