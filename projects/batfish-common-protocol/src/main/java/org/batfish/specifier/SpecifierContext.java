package org.batfish.specifier;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.IpSpace;
import org.batfish.referencelibrary.ReferenceBook;
import org.batfish.role.NodeRole;

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

  /** @return the set of {@link NodeRole}s in the network with the input dimension. */
  @Nonnull
  Set<NodeRole> getNodeRolesByDimension(String dimension);

  /**
   * @return the {@link IpSpace}s owned by each interface in the network. Mapping: hostname ->
   *     interface name -> IpSpace.
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
   * @return the {@link IpSpace}s owned by each VRF in the network. Mapping: hostname -> VRF name ->
   *     IpSpace.
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
