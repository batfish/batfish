package org.batfish.representation.cisco;

import java.io.Serializable;
import org.batfish.datamodel.IpSpace;

public final class NetworkObject implements Serializable {
  /** */
  private static final long serialVersionUID = 1L;

  private String _description;

  private String _name;

  private IpSpace _ipSpace;

  public NetworkObject(String name) {
    _name = name;
  }

  public String getDescription() {
    return _description;
  }

  public String getName() {
    return _name;
  }

  public IpSpace getIpSpace() {
    return _ipSpace;
  }

  public void setDescription(String description) {
    _description = description;
  }

  public void setIpSpace(IpSpace ipSpace) {
    _ipSpace = ipSpace;
  }
}
