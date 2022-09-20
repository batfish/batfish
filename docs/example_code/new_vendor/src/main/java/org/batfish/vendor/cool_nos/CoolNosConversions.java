package org.batfish.vendor.cool_nos;

import javax.annotation.Nonnull;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Prefix;

/**
 * Utility class for converting {@link CoolNosConfiguration} to vendor-independent {@link
 * Configuration}.
 */
public final class CoolNosConversions {

  static void convertStaticRoutes(CoolNosConfiguration vc, Configuration c) {
    vc.getStaticRoutes()
        .forEach(
            (prefix, route) ->
                c.getDefaultVrf().getStaticRoutes().add(toStaticRoute(prefix, route)));
  }

  private static @Nonnull org.batfish.datamodel.StaticRoute toStaticRoute(
      Prefix prefix, StaticRoute route) {
    return org.batfish.datamodel.StaticRoute.builder()
        .setAdministrativeCost(1)
        .setMetric(0L)
        .setNextHop(NEXT_HOP_CONVERTER.visit(route.getNextHop()))
        .setNetwork(prefix)
        .build();
  }

  private static final NextHopVisitor<org.batfish.datamodel.route.nh.NextHop> NEXT_HOP_CONVERTER =
      new NextHopVisitor<org.batfish.datamodel.route.nh.NextHop>() {
        @Override
        public org.batfish.datamodel.route.nh.NextHop visitNextHopDiscard(
            NextHopDiscard nextHopDiscard) {
          return org.batfish.datamodel.route.nh.NextHopDiscard.instance();
        }

        @Override
        public org.batfish.datamodel.route.nh.NextHop visitNextHopGateway(
            NextHopGateway nextHopGateway) {
          return org.batfish.datamodel.route.nh.NextHopIp.of(nextHopGateway.getIp());
        }

        @Override
        public org.batfish.datamodel.route.nh.NextHop visitNextHopInterface(
            NextHopInterface nextHopInterface) {
          return org.batfish.datamodel.route.nh.NextHopInterface.of(
              nextHopInterface.getInterface());
        }
      };

  // prevent instantiation
  private CoolNosConversions() {}
}
