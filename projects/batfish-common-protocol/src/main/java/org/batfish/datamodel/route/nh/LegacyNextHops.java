package org.batfish.datamodel.route.nh;

import java.util.Optional;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;

/** Utilities for dealing with legacy {@link NextHop} objects. */
public final class LegacyNextHops {
  /**
   * Returns the next hop interface, if any, for the given {@link NextHop}.
   *
   * <p>NB: for legacy compatibility, returns {@link Interface#NULL_INTERFACE_NAME} for {@link
   * NextHopDiscard}.
   */
  public static @Nonnull Optional<String> getNextHopInterface(NextHop nextHop) {
    return NEXT_HOP_INTERFACE_EXTRACTOR.visit(nextHop);
  }

  /** Returns the next hop IP, if any, for the given {@link NextHop}. */
  public static @Nonnull Optional<Ip> getNextHopIp(NextHop nextHop) {
    return NEXT_HOP_IP_EXTRACTOR.visit(nextHop);
  }

  /** Returns the next VRF, if any, for the given {@link NextHop}. */
  public static @Nonnull Optional<String> getNextVrf(NextHop nextHop) {
    return NEXT_VRF_EXTRACTOR.visit(nextHop);
  }

  private static final NextHopVisitor<Optional<Ip>> NEXT_HOP_IP_EXTRACTOR =
      new NextHopVisitor<Optional<Ip>>() {

        @Override
        public Optional<Ip> visitNextHopIp(NextHopIp nextHopIp) {
          return Optional.of(nextHopIp.getIp());
        }

        @Override
        public Optional<Ip> visitNextHopInterface(NextHopInterface nextHopInterface) {
          return Optional.ofNullable(nextHopInterface.getIp());
        }

        @Override
        public Optional<Ip> visitNextHopDiscard(NextHopDiscard nextHopDiscard) {
          return Optional.empty();
        }

        @Override
        public Optional<Ip> visitNextHopVrf(NextHopVrf nextHopVrf) {
          return Optional.empty();
        }

        @Override
        public Optional<Ip> visitNextHopVtep(NextHopVtep nextHopVtep) {
          return Optional.empty();
        }
      };

  private static final NextHopVisitor<Optional<String>> NEXT_HOP_INTERFACE_EXTRACTOR =
      new NextHopVisitor<Optional<String>>() {
        @Override
        public Optional<String> visitNextHopIp(NextHopIp nextHopIp) {
          return Optional.empty();
        }

        @Override
        public Optional<String> visitNextHopInterface(NextHopInterface nextHopInterface) {
          return Optional.of(nextHopInterface.getInterfaceName());
        }

        @Override
        public Optional<String> visitNextHopDiscard(NextHopDiscard nextHopDiscard) {
          return Optional.of(Interface.NULL_INTERFACE_NAME);
        }

        @Override
        public Optional<String> visitNextHopVrf(NextHopVrf nextHopVrf) {
          return Optional.empty();
        }

        @Override
        public Optional<String> visitNextHopVtep(NextHopVtep nextHopVtep) {
          return Optional.empty();
        }
      };

  private static final NextHopVisitor<Optional<String>> NEXT_VRF_EXTRACTOR =
      new NextHopVisitor<Optional<String>>() {

        @Override
        public Optional<String> visitNextHopIp(NextHopIp nextHopIp) {
          return Optional.empty();
        }

        @Override
        public Optional<String> visitNextHopInterface(NextHopInterface nextHopInterface) {
          return Optional.empty();
        }

        @Override
        public Optional<String> visitNextHopDiscard(NextHopDiscard nextHopDiscard) {
          return Optional.empty();
        }

        @Override
        public Optional<String> visitNextHopVrf(NextHopVrf nextHopVrf) {
          return Optional.of(nextHopVrf.getVrfName());
        }

        @Override
        public Optional<String> visitNextHopVtep(NextHopVtep nextHopVtep) {
          return Optional.empty();
        }
      };

  private LegacyNextHops() {} // prevent instantiation
}
