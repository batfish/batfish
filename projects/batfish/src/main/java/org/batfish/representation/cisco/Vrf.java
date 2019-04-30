package org.batfish.representation.cisco;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.batfish.representation.cisco.nx.CiscoNxBgpVrfConfiguration;

public final class Vrf implements Serializable {

  private static final long serialVersionUID = 1L;

  private final Map<Long, EigrpProcess> _eigrpProcesses;

  private CiscoNxBgpVrfConfiguration _bgpNxConfig;

  private BgpProcess _bgpProcess;

  private String _description;

  private IsisProcess _isisProcess;

  private final String _name;

  private Map<String, OspfProcess> _ospfProcesses;

  private RipProcess _ripProcess;

  private final Set<StaticRoute> _staticRoutes;

  public Vrf(String name) {
    _eigrpProcesses = new TreeMap<>();
    _name = name;
    // Ensure that processes are in insertion order.
    _ospfProcesses = new LinkedHashMap<>(0);
    _staticRoutes = new HashSet<>();
  }

  public CiscoNxBgpVrfConfiguration getBgpNxConfig() {
    return _bgpNxConfig;
  }

  public BgpProcess getBgpProcess() {
    return _bgpProcess;
  }

  public String getDescription() {
    return _description;
  }

  public Map<Long, EigrpProcess> getEigrpProcesses() {
    return _eigrpProcesses;
  }

  public IsisProcess getIsisProcess() {
    return _isisProcess;
  }

  public String getName() {
    return _name;
  }

  /** Return OSPF processes defined on this VRF. Guaranteed to be in insertion order */
  public Map<String, OspfProcess> getOspfProcesses() {
    return _ospfProcesses;
  }

  public RipProcess getRipProcess() {
    return _ripProcess;
  }

  public Set<StaticRoute> getStaticRoutes() {
    return _staticRoutes;
  }

  public void setBgpNxConfig(CiscoNxBgpVrfConfiguration bgpNxConfig) {
    _bgpNxConfig = bgpNxConfig;
  }

  public void setBgpProcess(BgpProcess bgpProcess) {
    _bgpProcess = bgpProcess;
  }

  public void setDescription(String description) {
    _description = description;
  }

  public void setIsisProcess(IsisProcess isisProcess) {
    _isisProcess = isisProcess;
  }

  public void setRipProcess(RipProcess ripProcess) {
    _ripProcess = ripProcess;
  }
}
