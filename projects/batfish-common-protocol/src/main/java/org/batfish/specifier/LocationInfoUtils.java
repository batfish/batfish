package org.batfish.specifier;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.batfish.datamodel.Prefix.HOST_SUBNET_MAX_PREFIX_LENGTH;

import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.IpSpace;

/** Utility methods for constructing {@link LocationInfo}. */
public final class LocationInfoUtils {
  private LocationInfoUtils() {}

  @Nonnull
  public static IpSpace connectedSubnetIps(Interface iface) {

    return firstNonNull(
        AclIpSpace.union(
            iface.getAllConcreteAddresses().stream()
                /*
                 * Only include addresses on networks that might have hosts.
                 */
                .filter(address -> address.getNetworkBits() <= HOST_SUBNET_MAX_PREFIX_LENGTH)
                .map(address -> address.getPrefix().toHostIpSpace())
                .collect(Collectors.toList())),
        EmptyIpSpace.INSTANCE);
  }
}
