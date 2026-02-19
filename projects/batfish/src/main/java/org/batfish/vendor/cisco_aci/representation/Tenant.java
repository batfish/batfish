package org.batfish.vendor.cisco_aci.representation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * ACI Tenant (fvTenant) semantic model.
 *
 * <p>Tenants are the primary container for application policies in ACI. They contain application
 * profiles (fvAp), bridge domains (fvBD), VRF contexts (fvCtx), endpoint groups (fvAEPg), and
 * contracts (vzBrCP).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Tenant implements Serializable {
  private final String _name;
  private Map<String, BridgeDomain> _bridgeDomains;
  private Map<String, AciVrfModel> _vrfs;
  private Map<String, Epg> _epgs;
  private Map<String, Contract> _contracts;
  private Map<String, ContractInterface> _contractInterfaces;
  private Map<String, FilterModel> _filters;
  private Map<String, TabooContract> _tabooContracts;

  public Tenant(String name) {
    _name = name;
    _bridgeDomains = new TreeMap<>();
    _vrfs = new TreeMap<>();
    _epgs = new TreeMap<>();
    _contracts = new TreeMap<>();
    _contractInterfaces = new TreeMap<>();
    _filters = new TreeMap<>();
    _tabooContracts = new TreeMap<>();
  }

  public String getName() {
    return _name;
  }

  public Map<String, BridgeDomain> getBridgeDomains() {
    return _bridgeDomains;
  }

  public void setBridgeDomains(Map<String, BridgeDomain> bridgeDomains) {
    _bridgeDomains = new TreeMap<>(bridgeDomains);
  }

  public Map<String, AciVrfModel> getVrfs() {
    return _vrfs;
  }

  public void setVrfs(Map<String, AciVrfModel> vrfs) {
    _vrfs = new TreeMap<>(vrfs);
  }

  public Map<String, Epg> getEpgs() {
    return _epgs;
  }

  public void setEpgs(Map<String, Epg> epgs) {
    _epgs = new TreeMap<>(epgs);
  }

  public Map<String, Contract> getContracts() {
    return _contracts;
  }

  public void setContracts(Map<String, Contract> contracts) {
    _contracts = new TreeMap<>(contracts);
  }

  public Map<String, ContractInterface> getContractInterfaces() {
    return _contractInterfaces;
  }

  public void setContractInterfaces(Map<String, ContractInterface> contractInterfaces) {
    _contractInterfaces = new TreeMap<>(contractInterfaces);
  }

  public Map<String, FilterModel> getFilters() {
    return _filters;
  }

  public void setFilters(Map<String, FilterModel> filters) {
    _filters = new TreeMap<>(filters);
  }

  public Map<String, TabooContract> getTabooContracts() {
    return _tabooContracts;
  }

  public void setTabooContracts(Map<String, TabooContract> tabooContracts) {
    _tabooContracts = new TreeMap<>(tabooContracts);
  }

  /** Application profiles (fvAp) within this tenant. */
  private Map<String, ApplicationProfile> _applicationProfiles;

  public Map<String, ApplicationProfile> getApplicationProfiles() {
    if (_applicationProfiles == null) {
      _applicationProfiles = new TreeMap<>();
    }
    return _applicationProfiles;
  }

  public void setApplicationProfiles(Map<String, ApplicationProfile> applicationProfiles) {
    _applicationProfiles = new TreeMap<>(applicationProfiles);
  }

  /** L3Outs within this tenant. */
  private List<String> _l3OutNames;

  public List<String> getL3OutNames() {
    if (_l3OutNames == null) {
      _l3OutNames = new ArrayList<>();
    }
    return _l3OutNames;
  }
}
