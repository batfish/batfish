package org.batfish.datamodel.tracking;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/** Tracks whether a particular flow may be delivered bidirectionally.. */
public final class TrackReachability implements TrackMethod {

  @Override
  public <R> R accept(GenericTrackMethodVisitor<R> visitor) {
    return visitor.visitTrackReachability(this);
  }

  /** The destination IP of the test flow. */
  @JsonProperty(PROP_DESTINATION_IP)
  public @Nonnull Ip getDestinationIp() {
    return _destinationIp;
  }

  /** The VRF from which to send the test flow. */
  @JsonProperty(PROP_SOURCE_VRF)
  public @Nonnull String getSourceVrf() {
    return _sourceVrf;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof TrackReachability)) {
      return false;
    }
    TrackReachability that = (TrackReachability) o;
    return _destinationIp.equals(that._destinationIp) && _sourceVrf.equals(that._sourceVrf);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_destinationIp, _sourceVrf);
  }

  public static @Nonnull TrackReachability of(Ip destinationIp, String sourceVrf) {
    return new TrackReachability(destinationIp, sourceVrf);
  }

  @JsonCreator
  private static @Nonnull TrackReachability create(
      @JsonProperty(PROP_DESTINATION_IP) @Nullable Ip destinationIp,
      @JsonProperty(PROP_SOURCE_VRF) @Nullable String sourceVrf) {
    checkArgument(destinationIp != null, "Missing %s", PROP_DESTINATION_IP);
    checkArgument(sourceVrf != null, "Missing %s", PROP_SOURCE_VRF);
    return of(destinationIp, sourceVrf);
  }

  private static final String PROP_DESTINATION_IP = "destinationIp";
  private static final String PROP_SOURCE_VRF = "sourceVrf";

  private final @Nonnull Ip _destinationIp;
  private final @Nonnull String _sourceVrf;

  private TrackReachability(Ip destinationIp, String sourceVrf) {
    _destinationIp = destinationIp;
    _sourceVrf = sourceVrf;
  }
}
