package org.batfish.datamodel.answers;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.batfish.datamodel.Prefix;

public class BdpAnswerElement extends DataPlaneAnswerElement {

  private static final String MAIN_RIB_ROUTES_BY_ITERATION = "mainRibRoutesByIteration";

  private static final String PROP_BGP_BEST_PATH_RIB_ROUTES_BY_ITERATION =
      "bgpBestPathRibRoutesByIteration";

  private static final String PROP_BGP_MULTIPATH_RIB_ROUTES_BY_ITERATION =
      "bgpMultipathRibRoutesByIteration";

  private static final String PROP_DEPENDENT_ROUTES_ITERATIONS = "dependentRoutesIterations";

  private static final String PROP_OSCILLATING_PREFIXES = "oscillatingPrefixes";

  private static final String PROP_OSPF_INTERNAL_ITERATIONS = "ospfInternalIterations";

  /** */
  private static final long serialVersionUID = 1L;

  private SortedMap<Integer, Integer> _bgpBestPathRibRoutesByIteration;

  private SortedMap<Integer, Integer> _bgpMultipathRibRoutesByIteration;

  private int _completedOscillationRecoveryAttempts;

  private int _dependentRoutesIterations;

  private SortedMap<Integer, Integer> _mainRibRoutesByIteration;

  private SortedSet<Prefix> _oscillatingPrefixes;

  private int _ospfInternalIterations;

  private String _version;

  public BdpAnswerElement() {
    _bgpBestPathRibRoutesByIteration = new TreeMap<>();
    _bgpMultipathRibRoutesByIteration = new TreeMap<>();
    _mainRibRoutesByIteration = new TreeMap<>();
    _oscillatingPrefixes = new TreeSet<>();
  }

  @JsonProperty(PROP_BGP_BEST_PATH_RIB_ROUTES_BY_ITERATION)
  public SortedMap<Integer, Integer> getBgpBestPathRibRoutesByIteration() {
    return _bgpBestPathRibRoutesByIteration;
  }

  @JsonProperty(PROP_BGP_MULTIPATH_RIB_ROUTES_BY_ITERATION)
  public SortedMap<Integer, Integer> getBgpMultipathRibRoutesByIteration() {
    return _bgpMultipathRibRoutesByIteration;
  }

  public int getCompletedOscillationRecoveryAttempts() {
    return _completedOscillationRecoveryAttempts;
  }

  @JsonProperty(PROP_DEPENDENT_ROUTES_ITERATIONS)
  public int getDependentRoutesIterations() {
    return _dependentRoutesIterations;
  }

  @JsonProperty(MAIN_RIB_ROUTES_BY_ITERATION)
  public SortedMap<Integer, Integer> getMainRibRoutesByIteration() {
    return _mainRibRoutesByIteration;
  }

  @JsonProperty(PROP_OSCILLATING_PREFIXES)
  public SortedSet<Prefix> getOscillatingPrefixes() {
    return _oscillatingPrefixes;
  }

  @JsonProperty(PROP_OSPF_INTERNAL_ITERATIONS)
  public int getOspfInternalIterations() {
    return _ospfInternalIterations;
  }

  @Override
  @JsonProperty(PROP_VERSION)
  public String getVersion() {
    return _version;
  }

  @Override
  public String prettyPrint() {
    StringBuilder sb = new StringBuilder();
    sb.append("Computation summary:\n");
    sb.append("   OSPF-internal iterations: " + _dependentRoutesIterations + "\n");
    sb.append("   Dependent-routes iterations: " + _dependentRoutesIterations + "\n");
    sb.append(
        "   BGP best-path RIB routes by iteration: " + _bgpBestPathRibRoutesByIteration + "\n");
    sb.append(
        "   BGP multipath RIB routes by iteration: " + _bgpMultipathRibRoutesByIteration + "\n");
    sb.append("   Main RIB routes by iteration: " + _mainRibRoutesByIteration + "\n");
    return sb.toString();
  }

  @JsonProperty(PROP_BGP_BEST_PATH_RIB_ROUTES_BY_ITERATION)
  public void setBgpBestPathRibRoutesByIteration(
      SortedMap<Integer, Integer> bgpBestPathRibRoutesByIteration) {
    _bgpBestPathRibRoutesByIteration = bgpBestPathRibRoutesByIteration;
  }

  @JsonProperty(PROP_BGP_MULTIPATH_RIB_ROUTES_BY_ITERATION)
  public void setBgpMultipathRibRoutesByIteration(
      SortedMap<Integer, Integer> bgpMultipathRibRoutesByIteration) {
    _bgpMultipathRibRoutesByIteration = bgpMultipathRibRoutesByIteration;
  }

  public void setCompletedOscillationRecoveryAttempts(int completedOscillationRecoveryAttempts) {
    _completedOscillationRecoveryAttempts = completedOscillationRecoveryAttempts;
  }

  @JsonProperty(PROP_DEPENDENT_ROUTES_ITERATIONS)
  public void setDependentRoutesIterations(int dependentRoutesIterations) {
    _dependentRoutesIterations = dependentRoutesIterations;
  }

  @JsonProperty(MAIN_RIB_ROUTES_BY_ITERATION)
  public void setMainRibRoutesByIteration(SortedMap<Integer, Integer> mainRibRoutesByIteration) {
    _mainRibRoutesByIteration = mainRibRoutesByIteration;
  }

  @JsonProperty(PROP_OSCILLATING_PREFIXES)
  public void setOscillatingPrefixes(SortedSet<Prefix> oscillatingPrefixes) {
    _oscillatingPrefixes = oscillatingPrefixes;
  }

  @JsonProperty(PROP_OSPF_INTERNAL_ITERATIONS)
  public void setOspfInternalIterations(int ospfInternalIterations) {
    _ospfInternalIterations = ospfInternalIterations;
  }

  @JsonProperty(PROP_VERSION)
  public void setVersion(String version) {
    _version = version;
  }
}
