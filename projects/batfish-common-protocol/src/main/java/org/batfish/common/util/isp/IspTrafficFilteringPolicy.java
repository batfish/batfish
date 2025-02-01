package org.batfish.common.util.isp;

import javax.annotation.Nullable;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.isp_configuration.traffic_filtering.IspTrafficFiltering;

/** An implementation of a specific {@link IspTrafficFiltering}. */
public interface IspTrafficFilteringPolicy {
  /** Returns the filter that will be applied to traffic entering the ISP from the Internet. */
  @Nullable
  IpAccessList filterTrafficFromInternet();

  /** Returns the filter that will be applied to traffic leaving the ISP to the Internet. */
  @Nullable
  IpAccessList filterTrafficToInternet();

  /** Returns the filter that will be applied to traffic leaving the network to the ISP. */
  @Nullable
  IpAccessList filterTrafficFromNetwork();

  /** Returns the filter that will be applied to traffic entering the network from the ISP. */
  @Nullable
  IpAccessList filterTrafficToNetwork();

  static IspTrafficFilteringPolicy createFor(IspTrafficFiltering trafficFiltering) {
    return switch (trafficFiltering.getMode()) {
      case NONE -> NoTrafficFilteringPolicy.create();
      case BLOCK_RESERVED_ADDRESSES_AT_INTERNET -> BlockReservedAddressesAtInternet.create();
    };
  }
}
