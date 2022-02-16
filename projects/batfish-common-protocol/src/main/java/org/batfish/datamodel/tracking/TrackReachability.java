package org.batfish.datamodel.tracking;

import static com.google.common.base.MoreObjects.toStringHelper;
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

  /** The destination IP of the test flow(s). */
  @JsonProperty(PROP_DESTINATION_IP)
  public @Nonnull Ip getDestinationIp() {
    return _destinationIp;
  }

  /**
   * Optional source IP for the test flow.
   *
   * <p>If {@code null}, then test flows will be created for every primary interface address of
   * every layer-3 interface that is an output interface for {@link #getDestinationIp()} with
   * resolution starting in {@link #getSourceVrf()}. The track will succed if any test flow
   * succeeds. Else, there will only be a single flow for returned source IP.
   */
  public @Nullable Ip getSourceIp() {
    return _sourceIp;
  }

  /** The VRF from which to send the test flow(s). */
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
    return _destinationIp.equals(that._destinationIp)
        && _sourceVrf.equals(that._sourceVrf)
        && Objects.equals(_sourceIp, that._sourceIp);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_destinationIp, _sourceVrf, _sourceIp);
  }

  static @Nonnull TrackReachability of(Ip destinationIp, String sourceVrf, @Nullable Ip sourceIp) {
    return new TrackReachability(destinationIp, sourceVrf, sourceIp);
  }

  @JsonCreator
  private static @Nonnull TrackReachability create(
      @JsonProperty(PROP_DESTINATION_IP) @Nullable Ip destinationIp,
      @JsonProperty(PROP_SOURCE_VRF) @Nullable String sourceVrf,
      @JsonProperty(PROP_SOURCE_IP) @Nullable Ip sourceIp) {
    checkArgument(destinationIp != null, "Missing %s", PROP_DESTINATION_IP);
    checkArgument(sourceVrf != null, "Missing %s", PROP_SOURCE_VRF);
    return of(destinationIp, sourceVrf, sourceIp);
  }

  @Override
  public String toString() {
    return toStringHelper(this)
        .omitNullValues()
        .add(PROP_DESTINATION_IP, _destinationIp)
        .add(PROP_SOURCE_IP, _sourceIp)
        .add(PROP_SOURCE_VRF, _sourceVrf)
        .toString();
  }

  private static final String PROP_DESTINATION_IP = "destinationIp";
  private static final String PROP_SOURCE_IP = "sourceIp";
  private static final String PROP_SOURCE_VRF = "sourceVrf";

  private final @Nonnull Ip _destinationIp;
  private final @Nullable Ip _sourceIp;
  private final @Nonnull String _sourceVrf;

  private TrackReachability(Ip destinationIp, String sourceVrf, @Nullable Ip sourceIp) {
    _destinationIp = destinationIp;
    _sourceVrf = sourceVrf;
    _sourceIp = sourceIp;
  }
}
