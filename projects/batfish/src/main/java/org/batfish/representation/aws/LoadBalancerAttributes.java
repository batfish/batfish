package org.batfish.representation.aws;

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
 * Represents attributes for an elastic load balancer v2
 * https://docs.aws.amazon.com/elasticloadbalancing/.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
final class LoadBalancerAttributes implements AwsVpcEntity, Serializable {

  static final String CROSS_ZONE_KEY = "load_balancing.cross_zone.enabled";

  @JsonIgnoreProperties(ignoreUnknown = true)
  @ParametersAreNonnullByDefault
  static class Attribute implements Serializable {

    private final @Nonnull String _key;

    private final @Nonnull String _value;

    @JsonCreator
    private static Attribute create(
        @JsonProperty(JSON_KEY_KEY) @Nullable String key,
        @JsonProperty(JSON_KEY_VALUE) @Nullable String value) {
      checkNonNull(key, JSON_KEY_KEY, "Load balancer attribute");
      checkNonNull(value, JSON_KEY_VALUE, "Load balancer attribute");
      return new Attribute(key, value);
    }

    Attribute(String key, String value) {
      _key = key;
      _value = value;
    }

    public @Nonnull String getKey() {
      return _key;
    }

    public @Nonnull String getValue() {
      return _value;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Attribute)) {
        return false;
      }
      Attribute that = (Attribute) o;
      return _key.equals(that._key) && _value.equals(that._value);
    }

    @Override
    public int hashCode() {
      return Objects.hash(_key, _value);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this).add("_key", _key).add("_value", _value).toString();
    }
  }

  private @Nonnull String _loadBalancerArn;

  private final @Nonnull List<Attribute> _attributes;

  @JsonCreator
  private static LoadBalancerAttributes create(
      @JsonProperty(JSON_KEY_LOAD_BALANCER_ARN) @Nullable String arn,
      @JsonProperty(JSON_KEY_ATTRIBUTES) @Nullable List<Attribute> attributes) {
    checkNonNull(arn, JSON_KEY_LOAD_BALANCER_ARN, "LoadBalancer");
    checkNonNull(attributes, JSON_KEY_AVAILABILITY_ZONES, "LoadBalancer");

    return new LoadBalancerAttributes(arn, attributes);
  }

  public LoadBalancerAttributes(String arn, List<Attribute> attributes) {
    _loadBalancerArn = arn;
    _attributes = attributes;
  }

  /**
   * Returns whether cross zone load balancing is enabled. Assumes that it is NOT enabled if the key
   * is not found
   */
  boolean getCrossZoneLoadBalancing() {
    return _attributes.stream()
        .filter(attr -> attr.getKey().equals(CROSS_ZONE_KEY))
        .map(attr -> attr.getValue().equals("true"))
        .findFirst()
        .orElse(false);
  }

  @Override
  public String getId() {
    return _loadBalancerArn;
  }

  public @Nonnull List<Attribute> getAttributes() {
    return _attributes;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof LoadBalancerAttributes)) {
      return false;
    }
    LoadBalancerAttributes that = (LoadBalancerAttributes) o;
    return _loadBalancerArn.equals(that._loadBalancerArn) && _attributes.equals(that._attributes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_loadBalancerArn, _attributes);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("_loadBalancerArn", _loadBalancerArn)
        .add("_attributes", _attributes)
        .toString();
  }
}
