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
import org.batfish.representation.aws.LoadBalancer.Protocol;

/**
 * Represents a target group for an elastic load balancer v2
 * https://docs.aws.amazon.com/elasticloadbalancing/.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
public final class TargetGroup implements AwsVpcEntity, Serializable {

  public enum Type {
    INSTANCE,
    IP
  }

  @Nonnull private String _targetGroupArn;

  @Nonnull private final List<String> _loadBalancerArns;

  @Nonnull private final Protocol _protocol;

  @Nonnull private final Integer _port;

  @Nonnull private final String _targetGroupName;

  @Nonnull private final Type _targetType;

  @JsonCreator
  private static TargetGroup create(
      @Nullable @JsonProperty(JSON_KEY_TARGET_GROUP_ARN) String targetGroupArn,
      @Nullable @JsonProperty(JSON_KEY_LOAD_BALANCER_ARNS) List<String> loadBalancerArns,
      @Nullable @JsonProperty(JSON_KEY_PROTOCOL) String protocol,
      @Nullable @JsonProperty(JSON_KEY_PORT) Integer port,
      @Nullable @JsonProperty(JSON_KEY_TARGET_GROUP_NAME) String targetGroupName,
      @Nullable @JsonProperty(JSON_KEY_TARGET_TYPE) String targetType) {
    checkNonNull(targetGroupArn, JSON_KEY_TARGET_GROUP_ARN, "LoadBalancer target group");
    checkNonNull(loadBalancerArns, JSON_KEY_LOAD_BALANCER_ARNS, "LoadBalancer target group");
    checkNonNull(protocol, JSON_KEY_PROTOCOL, "LoadBalancer target group");
    checkNonNull(port, JSON_KEY_PORT, "LoadBalancer target group");
    checkNonNull(targetGroupName, JSON_KEY_TARGET_GROUP_NAME, "LoadBalancer target group");
    checkNonNull(targetType, JSON_KEY_TARGET_TYPE, "LoadBalancer target group");

    return new TargetGroup(
        targetGroupArn,
        loadBalancerArns,
        Protocol.valueOf(protocol.toUpperCase()),
        port,
        targetGroupName,
        Type.valueOf(targetType.toUpperCase()));
  }

  public TargetGroup(
      String targetGroupArn,
      List<String> loadBalancerArns,
      Protocol protocol,
      Integer port,
      String targetGroupName,
      Type targetType) {
    _targetGroupArn = targetGroupArn;
    _loadBalancerArns = loadBalancerArns;
    _protocol = protocol;
    _port = port;
    _targetGroupName = targetGroupName;
    _targetType = targetType;
  }

  @Override
  public String getId() {
    return _targetGroupArn;
  }

  @Nonnull
  public List<String> getLoadBalancerArns() {
    return _loadBalancerArns;
  }

  @Nonnull
  public Protocol getProtocol() {
    return _protocol;
  }

  @Nonnull
  public Integer getPort() {
    return _port;
  }

  @Nonnull
  public String getTargetGroupName() {
    return _targetGroupName;
  }

  @Nonnull
  public Type getTargetType() {
    return _targetType;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof TargetGroup)) {
      return false;
    }
    TargetGroup that = (TargetGroup) o;
    return _targetGroupArn.equals(that._targetGroupArn)
        && _loadBalancerArns.equals(that._loadBalancerArns)
        && _protocol == that._protocol
        && _port.equals(that._port)
        && _targetGroupName.equals(that._targetGroupName)
        && _targetType == that._targetType;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _targetGroupArn, _loadBalancerArns, _protocol, _port, _targetGroupName, _targetType);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("_targetGroupArn", _targetGroupArn)
        .add("_loadBalancerArns", _loadBalancerArns)
        .add("_protocol", _protocol)
        .add("_port", _port)
        .add("_targetGroupName", _targetGroupName)
        .add("_targetType", _targetType)
        .toString();
  }
}
