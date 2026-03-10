package org.batfish.common.topology;

import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.topology.IpOwnersBaseImpl.ElectionDetails;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.Prefix;

/** Provides a view of IP addresses owned by nodes, vrfs, and interfaces. */
public interface IpOwners {
  /**
   * Invert a mapping from {@link Ip} to owner interfaces (Ip -&gt; hostname -&gt; interface name)
   * to (hostname -&gt; interface name -&gt; Ip).
   */
  @Nonnull
  Map<String, Map<String, Set<Ip>>> getInterfaceOwners(boolean excludeInactive);

  /**
   * Returns a mapping of IP addresses to a set of hostnames that "own" this IP (e.g., as a network
   * interface address)
   *
   * @param excludeInactive Whether to exclude inactive interfaces
   * @return A map of {@link Ip}s to a set of hostnames that own this IP
   */
  @Nonnull
  Map<Ip, Set<String>> getNodeOwners(boolean excludeInactive);

  /**
   * Mapping from a IP to hostname to set of interfaces that own that IP (for active interfaces
   * only)
   */
  @Nonnull
  Map<Ip, Map<String, Set<String>>> getActiveDeviceOwnedIps();

  /**
   * Mapping from a IP to hostname to set of interfaces that own that IP (including inactive
   * interfaces)
   */
  @Nonnull
  Map<Ip, Map<String, Set<String>>> getAllDeviceOwnedIps();

  /**
   * Returns a mapping from hostname to interface name to the host {@link IpSpace} of that
   * interface, including inactive interfaces.
   *
   * @see Prefix#toHostIpSpace()
   */
  @Nonnull
  Map<String, Map<String, IpSpace>> getAllInterfaceHostIps();

  /**
   * Returns a mapping from hostname to interface name to IpSpace owned by that interface, for
   * active interfaces only
   */
  @Nonnull
  Map<String, Map<String, IpSpace>> getInterfaceOwnedIpSpaces();

  /** Returns a mapping from IP to hostname to set of VRFs that own that IP. */
  @Nonnull
  Map<Ip, Map<String, Set<String>>> getIpVrfOwners();

  /**
   * Returns a mapping from hostname to vrf name to interface name to a space of IPs owned by that
   * interface. Only considers interface IPs. Considers <em>only active</em> interfaces.
   */
  @Nonnull
  Map<String, Map<String, Map<String, IpSpace>>> getVrfIfaceOwnedIpSpaces();

  /** Returns election data for HSRP, if recorded. */
  @Nullable
  ElectionDetails getHsrpElectionDetails();

  /** Returns election data for VRRP, if recorded. */
  @Nullable
  ElectionDetails getVrrpElectionDetails();
}
