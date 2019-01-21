package org.batfish.representation.cisco;

import java.io.Serializable;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.Prefix;

public final class NetworkObject implements Serializable {
  /** */
  private static final long serialVersionUID = 1L;

  private String _description;

  private Ip _host;

  private String _name;

  private Ip _rangeStart;

  private Ip _rangeEnd;

  private Prefix _subnet;

  private IpSpace _ipSpace;

  public NetworkObject(String name) {
    _name = name;
  }

  public String getDescription() {
    return _description;
  }

  public Ip getHost() {
    return _host;
  }

  public String getName() {
    return _name;
  }

  public Ip getRangeEnd() {
    return _rangeEnd;
  }

  public Ip getRangeStart() {
    return _rangeStart;
  }

  public Prefix getSubnet() {
    return _subnet;
  }

  public IpSpace getIpSpace() {
    return _ipSpace;
  }

  public void setDescription(String description) {
    _description = description;
  }

  public void setFqdn(String fqdn) {
    _ipSpace = EmptyIpSpace.INSTANCE;
  }

  public void setHost(Ip host) {
    _host = host;
    _ipSpace = host.toIpSpace();
  }

  public void setRange(Ip start, Ip end) {
    _rangeStart = start;
    _rangeEnd = end;
    _ipSpace = EmptyIpSpace.INSTANCE;
  }

  public void setSubnet(Prefix subnet) {
    _subnet = subnet;
    _ipSpace = subnet.toIpSpace();
  }
}
