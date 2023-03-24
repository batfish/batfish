package org.batfish.representation.cisco;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.batfish.datamodel.IsoAddress;
import org.batfish.datamodel.isis.IsisLevel;

public class IsisProcess implements Serializable {

  private IsisLevel _level;

  private IsoAddress _netAddress;

  private Map<RoutingProtocolInstance, IsisRedistributionPolicy> _redistributionPolicies;

  public IsisProcess() {
    _redistributionPolicies = new HashMap<>();
  }

  public IsisLevel getLevel() {
    return _level;
  }

  public IsoAddress getNetAddress() {
    return _netAddress;
  }

  public Map<RoutingProtocolInstance, IsisRedistributionPolicy> getRedistributionPolicies() {
    return _redistributionPolicies;
  }

  public void setLevel(IsisLevel level) {
    _level = level;
  }

  public void setNetAddress(IsoAddress netAddress) {
    _netAddress = netAddress;
  }
}
