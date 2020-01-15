package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import java.io.Serializable;
import java.util.SortedSet;
import java.util.TreeSet;

public class RipProcess implements Serializable {

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private Vrf _vrf;

    public RipProcess build() {
      RipProcess ripProcess = new RipProcess();
      if (_vrf != null) {
        _vrf.setRipProcess(ripProcess);
      }
      return ripProcess;
    }

    public RipProcess.Builder setVrf(Vrf vrf) {
      _vrf = vrf;
      return this;
    }
  }

  public static final long DEFAULT_RIP_COST = 1;
  private static final String PROP_EXPORT_POLICY = "exportPolicy";
  private static final String PROP_IMPORT_POLICY = "importPolicy";

  private String _exportPolicy;

  private SortedSet<GeneratedRoute> _generatedRoutes;

  private String _importPolicy;

  private SortedSet<String> _interfaces;

  private transient Table<Ip, Ip, RipNeighbor> _ripNeighbors;

  public RipProcess() {
    _generatedRoutes = new TreeSet<>();
    _interfaces = new TreeSet<>();
    _ripNeighbors = HashBasedTable.create();
  }

  @JsonProperty(PROP_EXPORT_POLICY)
  public String getExportPolicy() {
    return _exportPolicy;
  }

  public SortedSet<GeneratedRoute> getGeneratedRoutes() {
    return _generatedRoutes;
  }

  @JsonProperty(PROP_IMPORT_POLICY)
  public String getImportPolicy() {
    return _importPolicy;
  }

  public SortedSet<String> getInterfaces() {
    return _interfaces;
  }

  @JsonIgnore
  public Table<Ip, Ip, RipNeighbor> getRipNeighbors() {
    return _ripNeighbors;
  }

  @JsonProperty(PROP_EXPORT_POLICY)
  public void setExportPolicy(String exportPolicy) {
    _exportPolicy = exportPolicy;
  }

  public void setGeneratedRoutes(SortedSet<GeneratedRoute> generatedRoutes) {
    _generatedRoutes = generatedRoutes;
  }

  @JsonProperty(PROP_IMPORT_POLICY)
  public void setImportPolicy(String importPolicy) {
    _importPolicy = importPolicy;
  }

  public void setInterfaces(SortedSet<String> interfaces) {
    _interfaces = interfaces;
  }

  @JsonIgnore
  public void setRipNeighbors(Table<Ip, Ip, RipNeighbor> ripNeighbors) {
    _ripNeighbors = ripNeighbors;
  }
}
