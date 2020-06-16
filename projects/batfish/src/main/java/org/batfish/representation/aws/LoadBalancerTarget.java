package org.batfish.representation.aws;

import static org.batfish.representation.aws.Utils.checkNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
public final class LoadBalancerTarget implements AwsVpcEntity, Serializable {

  @Nullable private final String _availabilityZone;

  @Nonnull private final String _id;

  private final int _port;

  @JsonCreator
  private static LoadBalancerTarget create(
      @Nullable @JsonProperty(JSON_KEY_AVAILABILITY_ZONE) String availabilityZone,
      @Nullable @JsonProperty(JSON_KEY_ID) String id,
      @Nullable @JsonProperty(JSON_KEY_PORT) Integer port) {
    // availability zone is null when the target is an instance
    checkNonNull(id, JSON_KEY_ID, "Load balancer target");
    checkNonNull(port, JSON_KEY_PORT, "Load balancer target");

    return new LoadBalancerTarget(availabilityZone, id, port);
  }

  LoadBalancerTarget(@Nullable String availabilityZone, String id, int port) {
    _availabilityZone = availabilityZone;
    _id = id;
    _port = port;
  }

  @Nullable
  public String getAvailabilityZone() {
    return _availabilityZone;
  }

  @Override
  @Nonnull
  public String getId() {
    return _id;
  }

  public int getPort() {
    return _port;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof LoadBalancerTarget)) {
      return false;
    }
    LoadBalancerTarget that = (LoadBalancerTarget) o;
    return Objects.equals(_availabilityZone, that._availabilityZone)
        && _id.equals(that._id)
        && _port == that._port;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_availabilityZone, _id, _port);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("availabilityZone", _availabilityZone)
        .add("id", _id)
        .add("port", _port)
        .toString();
  }
}
