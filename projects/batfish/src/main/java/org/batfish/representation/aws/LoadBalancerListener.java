package org.batfish.representation.aws;

import static org.batfish.representation.aws.Utils.checkNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.representation.aws.LoadBalancer.Protocol;

/**
 * Represent a listener for an elastic load balancer v2
 * https://docs.aws.amazon.com/elasticloadbalancing/.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
final class LoadBalancerListener implements AwsVpcEntity, Serializable {

  enum ActionType {
    FORWARD,
    AUTHENTICATE_OIDC,
    AUTHENTICATE_COGNITO,
    REDIRECT,
    FIXED_RESPONSE,
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  @ParametersAreNonnullByDefault
  static class DefaultAction {

    private final int _order;

    @Nonnull private final String _targetGroupArn;

    @Nonnull private final ActionType _type;

    @JsonCreator
    private static DefaultAction create(
        @Nullable @JsonProperty(JSON_KEY_ORDER) Integer order,
        @Nullable @JsonProperty(JSON_KEY_TARGET_GROUP_ARN) String targetGroupArn,
        @Nullable @JsonProperty(JSON_KEY_TYPE) String type) {
      checkNonNull(order, JSON_KEY_ORDER, "Load balancer listener");
      checkNonNull(targetGroupArn, JSON_KEY_TARGET_GROUP_ARN, "Load balancer listener");
      checkNonNull(type, JSON_KEY_TARGET_TYPE, "Load balancer listener");

      return new DefaultAction(
          order, targetGroupArn, ActionType.valueOf(type.toUpperCase().replace('-', '_')));
    }

    DefaultAction(int order, String targetGroupArn, ActionType type) {
      _order = order;
      _targetGroupArn = targetGroupArn;
      _type = type;
    }

    public int getOrder() {
      return _order;
    }

    @Nonnull
    public String getTargetGroupArn() {
      return _targetGroupArn;
    }

    @Nonnull
    public ActionType getType() {
      return _type;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof DefaultAction)) {
        return false;
      }
      DefaultAction that = (DefaultAction) o;
      return _order == that._order
          && _targetGroupArn.equals(that._targetGroupArn)
          && _type == that._type;
    }

    @Override
    public int hashCode() {
      return Objects.hash(_order, _targetGroupArn, _type);
    }
  }

  @Nonnull private String _listenerArn;

  @Nonnull private String _loadBalancerArn;

  @Nonnull private final List<DefaultAction> _defaultActions;

  @Nonnull private final Protocol _protocol;

  @JsonCreator
  private static LoadBalancerListener create(
      @Nullable @JsonProperty(JSON_KEY_LISTENER_ARN) String listenerArn,
      @Nullable @JsonProperty(JSON_KEY_LOAD_BALANCER_ARN) String loadBalancerArn,
      @Nullable @JsonProperty(JSON_KEY_DEFAULT_ACTIONS) List<DefaultAction> defaultActions,
      @Nullable @JsonProperty(JSON_KEY_PROTOCOL) String protocol) {
    checkNonNull(listenerArn, JSON_KEY_LISTENER_ARN, "LoadBalancer listener");
    checkNonNull(loadBalancerArn, JSON_KEY_LOAD_BALANCER_ARN, "LoadBalancer listener");
    checkNonNull(defaultActions, JSON_KEY_DEFAULT_ACTIONS, "LoadBalancer listener");
    checkNonNull(protocol, JSON_KEY_PROTOCOL, "LoadBalancer listener");

    return new LoadBalancerListener(
        listenerArn, loadBalancerArn, defaultActions, Protocol.valueOf(protocol.toUpperCase()));
  }

  LoadBalancerListener(
      String listenerArn,
      String loadBalancerArn,
      List<DefaultAction> defaultActions,
      Protocol protocol) {
    _listenerArn = listenerArn;
    _loadBalancerArn = loadBalancerArn;
    _defaultActions = defaultActions;
    _protocol = protocol;
  }

  @Override
  public String getId() {
    return _listenerArn;
  }

  @Nonnull
  public String getLoadBalancerArn() {
    return _loadBalancerArn;
  }

  @Nonnull
  public List<DefaultAction> getDefaultActions() {
    return _defaultActions;
  }

  @Nonnull
  public Protocol getProtocol() {
    return _protocol;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof LoadBalancerListener)) {
      return false;
    }
    LoadBalancerListener that = (LoadBalancerListener) o;
    return _listenerArn.equals(that._listenerArn)
        && _loadBalancerArn.equals(that._loadBalancerArn)
        && _defaultActions.equals(that._defaultActions)
        && _protocol == that._protocol;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_listenerArn, _loadBalancerArn, _defaultActions, _protocol);
  }
}
