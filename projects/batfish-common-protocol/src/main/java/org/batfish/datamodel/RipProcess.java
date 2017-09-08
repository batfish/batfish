package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import org.batfish.common.Pair;

public class RipProcess implements Serializable {

  public static final long DEFAULT_RIP_COST = 1;

  private static final String PROP_EXPORT_POLICY = "exportPolicy";

  private static final String PROP_IMPORT_POLICY = "importPolicy";

  /** */
  private static final long serialVersionUID = 1L;

  private String _exportPolicy;

  private SortedSet<GeneratedRoute> _generatedRoutes;

  private String _importPolicy;

  private SortedSet<String> _interfaces;

  private transient Map<Pair<Ip, Ip>, RipNeighbor> _ripNeighbors;

  public RipProcess() {
    _generatedRoutes = new TreeSet<>();
    _interfaces = new TreeSet<>();
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
  public Map<Pair<Ip, Ip>, RipNeighbor> getRipNeighbors() {
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

  public void setRipNeighbors(Map<Pair<Ip, Ip>, RipNeighbor> ripNeighbors) {
    _ripNeighbors = ripNeighbors;
  }
}
