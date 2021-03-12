package org.batfish.representation.cisco_asa;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;
import org.batfish.datamodel.IsoAddress;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.isis.IsisLevel;

public class IsisProcess implements Serializable {

  private IsisLevel _level;

  private IsoAddress _netAddress;

  private Map<RoutingProtocol, IsisRedistributionPolicy> _redistributionPolicies;

  public IsisProcess() {
    _redistributionPolicies = new TreeMap<>();
  }

  public IsisLevel getLevel() {
    return _level;
  }

  public IsoAddress getNetAddress() {
    return _netAddress;
  }

  public Map<RoutingProtocol, IsisRedistributionPolicy> getRedistributionPolicies() {
    return _redistributionPolicies;
  }

  public void setLevel(IsisLevel level) {
    _level = level;
  }

  public void setNetAddress(IsoAddress netAddress) {
    _netAddress = netAddress;
  }
}
