package org.batfish.datamodel.tracking;

import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;

/** Utility class for constructing track methods. */
public final class TrackMethods {

  /** Always succeeds. */
  public static @Nonnull TrackMethod alwaysTrue() {
    return TrackTrue.instance();
  }

  /** Always fails. */
  public static @Nonnull TrackMethod alwaysFalse() {
    return negated(alwaysTrue());
  }

  /** Succeeds when interface is active. */
  public static @Nonnull TrackMethod interfaceActive(String interfaceName) {
    return TrackInterface.of(interfaceName);
  }

  /** Succeeds when interface is inactive. */
  public static @Nonnull TrackMethod interfaceInactive(String interfaceName) {
    return negated(interfaceActive(interfaceName));
  }

  /** Succeeds when {@code trackMethod} fails. */
  public static @Nonnull TrackMethod negated(TrackMethod trackMethod) {
    return NegatedTrackMethod.of(trackMethod);
  }

  /** Succeeds when {@link TrackMethod} with {@code trackMethodId} fails. */
  public static @Nonnull TrackMethod negatedReference(String trackMethodId) {
    return negated(reference(trackMethodId));
  }

  /**
   * Succeds when a trace to the given {@code destinationIp} resolved in {@code sourceVrf} is
   * bidirectionally accepted.
   */
  public static @Nonnull TrackMethod reachability(Ip destinationIp, String sourceVrf) {
    return TrackReachability.of(destinationIp, sourceVrf);
  }

  /** Succeeds when {@link TrackMethod} with {@code trackMethodId} succeeds. */
  public static @Nonnull TrackMethod reference(String trackMethodId) {
    return TrackMethodReference.of(trackMethodId);
  }

  /**
   * Succeeds when the main RIB of {@code vrf} contains a route for {@code prefix} from any of
   * {@code protocols}. If {@code protocols} is empty, there is no restriction on protocol.
   */
  public static @Nonnull TrackMethod route(
      Prefix prefix, Set<RoutingProtocol> protocols, String vrf) {
    return TrackRoute.of(prefix, protocols, vrf);
  }

  private TrackMethods() {}
}
