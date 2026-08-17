package org.batfish.representation.juniper;

import java.io.Serializable;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/**
 * Vendor-specific model of a Junos {@code system tacplus-server <address>} block, including its
 * sub-options. Keyed on the server address string.
 */
public class TacplusServer implements Serializable {

  private final String _name;

  private @Nullable String _secret;

  private @Nullable Ip _sourceAddress;

  private @Nullable Integer _port;

  private @Nullable Integer _timeout;

  private boolean _singleConnection;

  private @Nullable String _routingInstance;

  public TacplusServer(String name) {
    _name = name;
  }

  /** The server address, as written in the configuration. */
  public String getName() {
    return _name;
  }

  public @Nullable String getSecret() {
    return _secret;
  }

  public void setSecret(@Nullable String secret) {
    _secret = secret;
  }

  public @Nullable Ip getSourceAddress() {
    return _sourceAddress;
  }

  public void setSourceAddress(@Nullable Ip sourceAddress) {
    _sourceAddress = sourceAddress;
  }

  public @Nullable Integer getPort() {
    return _port;
  }

  public void setPort(@Nullable Integer port) {
    _port = port;
  }

  public @Nullable Integer getTimeout() {
    return _timeout;
  }

  public void setTimeout(@Nullable Integer timeout) {
    _timeout = timeout;
  }

  public boolean getSingleConnection() {
    return _singleConnection;
  }

  public void setSingleConnection(boolean singleConnection) {
    _singleConnection = singleConnection;
  }

  public @Nullable String getRoutingInstance() {
    return _routingInstance;
  }

  public void setRoutingInstance(@Nullable String routingInstance) {
    _routingInstance = routingInstance;
  }
}
