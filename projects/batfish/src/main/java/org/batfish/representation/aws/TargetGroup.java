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

  private @Nonnull String _targetGroupArn;

  private final @Nonnull List<String> _loadBalancerArns;

  private final @Nonnull Protocol _protocol;

  private final @Nonnull Integer _port;

  private final @Nonnull String _targetGroupName;

  private final @Nonnull Type _targetType;

  @JsonCreator
  private static TargetGroup create(
      @JsonProperty(JSON_KEY_TARGET_GROUP_ARN) @Nullable String targetGroupArn,
      @JsonProperty(JSON_KEY_LOAD_BALANCER_ARNS) @Nullable List<String> loadBalancerArns,
      @JsonProperty(JSON_KEY_PROTOCOL) @Nullable String protocol,
      @JsonProperty(JSON_KEY_PORT) @Nullable Integer port,
      @JsonProperty(JSON_KEY_TARGET_GROUP_NAME) @Nullable String targetGroupName,
      @JsonProperty(JSON_KEY_TARGET_TYPE) @Nullable String targetType) {
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

  public @Nonnull List<String> getLoadBalancerArns() {
    return _loadBalancerArns;
  }

  public @Nonnull Protocol getProtocol() {
    return _protocol;
  }

  public @Nonnull Integer getPort() {
    return _port;
  }

  public @Nonnull String getTargetGroupName() {
    return _targetGroupName;
  }

  public @Nonnull Type getTargetType() {
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
