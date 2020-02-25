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
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.SubRange;
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
  static class DefaultAction implements Serializable {

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

  @JsonIgnoreProperties(ignoreUnknown = true)
  @ParametersAreNonnullByDefault
  static class Listener implements AwsVpcEntity, Serializable {

    @Nonnull private String _listenerArn;

    @Nonnull private final List<DefaultAction> _defaultActions;

    @Nonnull private final Protocol _protocol;

    private final int _port;

    @JsonCreator
    private static Listener create(
        @Nullable @JsonProperty(JSON_KEY_LISTENER_ARN) String listenerArn,
        @Nullable @JsonProperty(JSON_KEY_DEFAULT_ACTIONS) List<DefaultAction> defaultActions,
        @Nullable @JsonProperty(JSON_KEY_PROTOCOL) String protocol,
        @Nullable @JsonProperty(JSON_KEY_PORT) Integer port) {
      checkNonNull(listenerArn, JSON_KEY_LISTENER_ARN, "LoadBalancer listener");
      checkNonNull(defaultActions, JSON_KEY_DEFAULT_ACTIONS, "LoadBalancer listener");
      checkNonNull(protocol, JSON_KEY_PROTOCOL, "LoadBalancer listener");
      checkNonNull(port, JSON_KEY_PORT, "LoadBalancer listener");

      return new Listener(
          listenerArn, defaultActions, Protocol.valueOf(protocol.toUpperCase()), port);
    }

    Listener(String listenerArn, List<DefaultAction> defaultActions, Protocol protocol, int port) {
      _listenerArn = listenerArn;
      _defaultActions = defaultActions;
      _protocol = protocol;
      _port = port;
    }

    /**
     * Gets {@code HeaderSpace} that this listener will match on for incoming packets.
     *
     * @throws IllegalArgumentException if this mapping cannot be made.
     */
    HeaderSpace getMatchingHeaderSpace() {
      HeaderSpace.Builder matchHeaderSpace =
          HeaderSpace.builder().setDstPorts(SubRange.singleton(_port));
      switch (_protocol) {
        case TCP:
        case TLS:
          matchHeaderSpace.setIpProtocols(IpProtocol.TCP);
          break;
        case TCP_UDP:
          matchHeaderSpace.setIpProtocols(IpProtocol.TCP, IpProtocol.UDP);
          break;
        case UDP:
          matchHeaderSpace.setIpProtocols(IpProtocol.TCP);
          break;
        default:
          throw new IllegalArgumentException(
              String.format(
                  "Cannot get matching header space for listener protocol %s", _protocol));
      }
      return matchHeaderSpace.build();
    }

    @Override
    public String getId() {
      return _listenerArn;
    }

    @Nonnull
    public List<DefaultAction> getDefaultActions() {
      return _defaultActions;
    }

    @Nonnull
    public Protocol getProtocol() {
      return _protocol;
    }

    public int getPort() {
      return _port;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Listener)) {
        return false;
      }
      Listener that = (Listener) o;
      return _listenerArn.equals(that._listenerArn)
          && _defaultActions.equals(that._defaultActions)
          && _protocol == that._protocol
          && _port == that._port;
    }

    @Override
    public int hashCode() {
      return Objects.hash(_listenerArn, _defaultActions, _protocol, _port);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("_listenerArn", _listenerArn)
          .add("_defaultActions", _defaultActions)
          .add("_protocol", _protocol)
          .add("_port", _port)
          .toString();
    }
  }

  @Nonnull private String _loadBalancerArn;

  @Nonnull private final List<Listener> _listeners;

  @JsonCreator
  private static LoadBalancerListener create(
      @Nullable @JsonProperty(JSON_KEY_LOAD_BALANCER_ARN) String loadBalancerArn,
      @Nullable @JsonProperty(JSON_KEY_LISTENERS) List<Listener> listeners) {
    checkNonNull(listeners, JSON_KEY_LISTENERS, "LoadBalancer listeners");
    checkNonNull(loadBalancerArn, JSON_KEY_LOAD_BALANCER_ARN, "LoadBalancer listener");

    return new LoadBalancerListener(loadBalancerArn, listeners);
  }

  LoadBalancerListener(String loadBalancerArn, List<Listener> listeners) {
    _loadBalancerArn = loadBalancerArn;
    _listeners = listeners;
  }

  @Nonnull
  public String getId() {
    return _loadBalancerArn;
  }

  @Nonnull
  public List<Listener> getListeners() {
    return _listeners;
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
    return _loadBalancerArn.equals(that._loadBalancerArn) && _listeners.equals(that._listeners);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_loadBalancerArn, _listeners);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("_loadBalancerArn", _loadBalancerArn)
        .add("_listeners", _listeners)
        .toString();
  }
}
