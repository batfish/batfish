package org.batfish.representation.aws;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.representation.aws.Utils.checkNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Represents an elastic load balancer v2 https://docs.aws.amazon.com/elasticloadbalancing/.
 *
 * <p>We currently only support network load balancer (not application)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
final class LoadBalancer implements AwsVpcEntity, Serializable {

  enum Scheme {
    INTERNAL,
    INTERNET_FACING
  }

  enum Type {
    APPLICATION,
    NETWORK
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  @ParametersAreNonnullByDefault
  static class AvailabilityZone {

    @Nonnull private final String _subnetId;

    @Nonnull private final String _zoneName;

    @JsonCreator
    private static AvailabilityZone create(
        @Nullable @JsonProperty(JSON_KEY_SUBNET_ID) String subnetId,
        @Nullable @JsonProperty(JSON_KEY_ZONE_NAME) String zoneName) {
      // not parsing "LoadBalancerAddresses" -- assuming that network interfaces cover the info
      checkNonNull(subnetId, JSON_KEY_SUBNET_ID, "Load balancer availability zone");
      checkNonNull(zoneName, JSON_KEY_ZONE_NAME, "Load balancer availability zone");
      return new AvailabilityZone(subnetId, zoneName);
    }

    AvailabilityZone(String subnetId, String zoneName) {
      _subnetId = subnetId;
      _zoneName = zoneName;
    }

    @Nonnull
    public String getSubnetId() {
      return _subnetId;
    }

    @Nonnull
    public String getZoneName() {
      return _zoneName;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof AvailabilityZone)) {
        return false;
      }
      AvailabilityZone that = (AvailabilityZone) o;
      return _subnetId.equals(that._subnetId) && _zoneName.equals(that._zoneName);
    }

    @Override
    public int hashCode() {
      return Objects.hash(_subnetId, _zoneName);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("_subnetId", _subnetId)
          .add("_zoneName", _zoneName)
          .toString();
    }
  }

  @Nonnull private String _arn;

  @Nonnull private final List<AvailabilityZone> _availabilityZones;

  @Nullable private final String _name;

  @Nonnull private final Scheme _scheme;

  @Nonnull private final Type _type;

  @Nonnull private final String _vpcId;

  @JsonCreator
  private static LoadBalancer create(
      @Nullable @JsonProperty(JSON_KEY_LOAD_BALANCER_ARN) String arn,
      @Nullable @JsonProperty(JSON_KEY_AVAILABILITY_ZONES) List<AvailabilityZone> availabilityZones,
      @Nullable @JsonProperty(JSON_KEY_LOAD_BALANCER_NAME) String name,
      @Nullable @JsonProperty(JSON_KEY_SCHEME) String scheme,
      @Nullable @JsonProperty(JSON_KEY_TYPE) String type,
      @Nullable @JsonProperty(JSON_KEY_VPC_ID) String vpcId) {
    checkNonNull(arn, JSON_KEY_LOAD_BALANCER_ARN, "LoadBalancer");
    checkNonNull(availabilityZones, JSON_KEY_AVAILABILITY_ZONES, "LoadBalancer");
    checkNonNull(scheme, JSON_KEY_SCHEME, "LoadBalancer");
    checkNonNull(type, JSON_KEY_TYPE, "LoadBalancer");
    checkNonNull(vpcId, JSON_KEY_VPC_ID, "LoadBalancer");

    Scheme schemeEnum =
        scheme.equals("internal")
            ? Scheme.INTERNAL
            : scheme.equals("internet-facing") ? Scheme.INTERNET_FACING : null;
    checkArgument(schemeEnum != null, "Unknown scheme for LoadBalances %s", scheme);

    return new LoadBalancer(
        arn, availabilityZones, name, schemeEnum, Type.valueOf(type.toUpperCase()), vpcId);
  }

  public LoadBalancer(
      String arn,
      List<AvailabilityZone> availabilityZones,
      String name,
      Scheme scheme,
      Type type,
      String vpcId) {
    _arn = arn;
    _availabilityZones = availabilityZones;
    _name = name;
    _scheme = scheme;
    _type = type;
    _vpcId = vpcId;
  }

  @Override
  public String getId() {
    return _arn;
  }

  @Nonnull
  public List<AvailabilityZone> getAvailabilityZones() {
    return _availabilityZones;
  }

  @Nullable
  public String getName() {
    return _name;
  }

  @Nonnull
  public Scheme getScheme() {
    return _scheme;
  }

  @Nonnull
  public Type getType() {
    return _type;
  }

  @Nonnull
  public String getVpcId() {
    return _vpcId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof LoadBalancer)) {
      return false;
    }
    LoadBalancer that = (LoadBalancer) o;
    return _arn.equals(that._arn)
        && _availabilityZones.equals(that._availabilityZones)
        && Objects.equals(_name, that._name)
        && _scheme == that._scheme
        && _type == that._type
        && _vpcId.equals(that._vpcId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_arn, _availabilityZones, _name, _scheme, _type, _vpcId);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("_arn", _arn)
        .add("_availabilityZones", _availabilityZones)
        .add("_name", _name)
        .add("_scheme", _scheme)
        .add("_type", _type)
        .add("_vpcId", _vpcId)
        .toString();
  }
}
