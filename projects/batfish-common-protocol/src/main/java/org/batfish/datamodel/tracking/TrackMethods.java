package org.batfish.datamodel.tracking;

import javax.annotation.Nonnull;
import org.batfish.datamodel.Ip;

/** Utility class for constructing track methods. */
public final class TrackMethods {

  // TODO: make constructors private, static creators package-private, and move callers here

  public static @Nonnull TrackMethod alwaysTrue() {
    return TrackTrue.instance();
  }

  public static @Nonnull TrackMethod alwaysFalse() {
    return negated(alwaysTrue());
  }

  public static @Nonnull TrackMethod interfaceActive(String interfaceName) {
    return new TrackInterface(interfaceName);
  }

  public static @Nonnull TrackMethod negated(TrackMethod trackMethod) {
    return NegatedTrackMethod.of(trackMethod);
  }

  public static @Nonnull TrackMethod negatedReference(String trackMethodId) {
    return negated(reference(trackMethodId));
  }

  public static @Nonnull TrackMethod reachability(Ip destinationIp, String sourceVrf) {
    return TrackReachability.of(destinationIp, sourceVrf);
  }

  public static @Nonnull TrackMethod reference(String trackMethodId) {
    return TrackMethodReference.of(trackMethodId);
  }

  private TrackMethods() {}
}
