package org.batfish.representation.aws;

import static org.batfish.specifier.LocationInfoUtils.configuredIps;
import static org.batfish.specifier.LocationInfoUtils.connectedHostSubnetHostIps;

import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.Interface;
import org.batfish.specifier.LocationInfo;

/** Helpers for defining AWS-specific {@link LocationInfo}. */
public final class AwsLocationInfoUtils {
  private AwsLocationInfoUtils() {}

  static LocationInfo instanceInterfaceLocationInfo(Interface iface) {
    return new LocationInfo(
        true, // use as a traffic source
        configuredIps(iface), // use configured IPs for source IP by default
        EmptyIpSpace.INSTANCE); // interface locations do not have external ARP IPs
  }

  static final LocationInfo INSTANCE_INTERFACE_LINK_LOCATION_INFO =
      new LocationInfo(
          false, // do not use as a traffic source
          EmptyIpSpace.INSTANCE,
          EmptyIpSpace.INSTANCE);

  static LocationInfo subnetInterfaceLocationInfo(Interface iface) {
    return new LocationInfo(
        // infrastructure interface; not a source
        false,
        // if user explicitly selects this location to be a source, use
        // its configured IPs for source IPs by default
        configuredIps(iface),
        // interface locations never have external ARP IPs
        EmptyIpSpace.INSTANCE);
  }

  static LocationInfo subnetInterfaceLinkLocationInfo(Interface iface) {
    return new LocationInfo(
        // not a source of traffic
        false,
        // if user explicitly selects this location to be a source, use
        // these source IPs by default
        connectedHostSubnetHostIps(iface),
        // no external ARP replies
        EmptyIpSpace.INSTANCE);
  }

  /**
   * A LocationInfo object for interfaces in the middle of the infrastructure. Most such interfaces
   * have link local IPs, so we do not need explicit location info for them, but some such as load
   * balancers and NAT gateways have concrete addresses. We configure those interfaces such that
   * they don't act as default sources or sinks.
   */
  static final LocationInfo INFRASTRUCTURE_LOCATION_INFO =
      new LocationInfo(false, EmptyIpSpace.INSTANCE, EmptyIpSpace.INSTANCE);
}
