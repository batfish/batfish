package org.batfish.specifier;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.IpSpace;
import org.batfish.referencelibrary.ReferenceBook;
import org.batfish.role.NodeRoleDimension;

/**
 * Collects all the information about the network that is needed by {@link NodeSpecifier}s, {@link
 * LocationSpecifier}s, and {@link IpSpaceSpecifier}s to resolve themselves.
 */
public interface SpecifierContext {
  /** @return The network configurations. */
  @Nonnull
  Map<String, Configuration> getConfigs();

  /** @return the set of {@link ReferenceBook} with name {@code bookName}. */
  Optional<ReferenceBook> getReferenceBook(String bookName);

  /** @return the {@link NodeRoleDimension} */
  @Nonnull
  Optional<NodeRoleDimension> getNodeRoleDimension(@Nullable String dimension);

  /**
   * @return the {@link IpSpace}s owned by each interface in the network. Mapping: hostname -&gt;
   *     interface name -&gt; IpSpace.
   */
  Map<String, Map<String, IpSpace>> getInterfaceOwnedIps();

  /**
   * Get the {@link IpSpace} owned by the input interface.
   *
   * @param hostname The node the interface belongs to.
   * @param iface The name of the interface.
   * @return The {@link IpSpace} owned by the interface.
   */
  default IpSpace getInterfaceOwnedIps(String hostname, String iface) {
    return getInterfaceOwnedIps()
        .getOrDefault(hostname, ImmutableMap.of())
        .getOrDefault(iface, EmptyIpSpace.INSTANCE);
  }

  /**
   * @return the {@link IpSpace}s owned by each VRF in the network. Mapping: hostname -&gt; VRF name
   *     -&gt; IpSpace.
   */
  Map<String, Map<String, IpSpace>> getVrfOwnedIps();

  /**
   * Get the {@link IpSpace} owned by the input VRF.
   *
   * @param hostname The node the VRF belongs to.
   * @param vrf The name of the VRF.
   * @return The combined space of IPs owned by the VRF's interfaces.
   */
  default IpSpace getVrfOwnedIps(String hostname, String vrf) {
    return getVrfOwnedIps()
        .getOrDefault(hostname, ImmutableMap.of())
        .getOrDefault(vrf, EmptyIpSpace.INSTANCE);
  }
}
