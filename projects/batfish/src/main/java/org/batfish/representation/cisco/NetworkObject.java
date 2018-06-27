package org.batfish.representation.cisco;

import org.batfish.common.util.ComparableStructure;
import org.batfish.datamodel.IpSpace;

public final class NetworkObject extends ComparableStructure<String> {
  /** */
  private static final long serialVersionUID = 1L;

  private String _description;

  private IpSpace _ipSpace;

  public NetworkObject(String name) {
    super(name);
  }

  public String getDescription() {
    return _description;
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
