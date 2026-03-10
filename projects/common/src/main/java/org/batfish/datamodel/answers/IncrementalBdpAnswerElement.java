package org.batfish.datamodel.answers;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.SortedMap;
import java.util.TreeMap;
import org.batfish.common.Warnings;

/** This answer contains summary information and warning about dataplane computation. */
public class IncrementalBdpAnswerElement extends DataPlaneAnswerElement {

  private static final String MAIN_RIB_ROUTES_BY_ITERATION = "mainRibRoutesByIteration";
  private static final String PROP_BGP_BEST_PATH_RIB_ROUTES_BY_ITERATION =
      "bgpBestPathRibRoutesByIteration";
  private static final String PROP_BGP_MULTIPATH_RIB_ROUTES_BY_ITERATION =
      "bgpMultipathRibRoutesByIteration";
  private static final String PROP_DEPENDENT_ROUTES_ITERATIONS = "dependentRoutesIterations";
  private static final String PROP_OSPF_INTERNAL_ITERATIONS = "ospfInternalIterations";
  private static final String PROP_WARNINGS = "warnings";

  private SortedMap<Integer, Integer> _bgpBestPathRibRoutesByIteration;
  private SortedMap<Integer, Integer> _bgpMultipathRibRoutesByIteration;
  private int _dependentRoutesIterations;
  private SortedMap<Integer, Integer> _mainRibRoutesByIteration;
  private int _ospfInternalIterations;
  private String _version;
  private Warnings _warnings;

  public IncrementalBdpAnswerElement() {
    _bgpBestPathRibRoutesByIteration = new TreeMap<>();
    _bgpMultipathRibRoutesByIteration = new TreeMap<>();
    _mainRibRoutesByIteration = new TreeMap<>();
    _warnings = new Warnings();
  }

  @JsonProperty(PROP_BGP_BEST_PATH_RIB_ROUTES_BY_ITERATION)
  public SortedMap<Integer, Integer> getBgpBestPathRibRoutesByIteration() {
    return _bgpBestPathRibRoutesByIteration;
  }

  @JsonProperty(PROP_BGP_MULTIPATH_RIB_ROUTES_BY_ITERATION)
  public SortedMap<Integer, Integer> getBgpMultipathRibRoutesByIteration() {
    return _bgpMultipathRibRoutesByIteration;
  }

  @JsonProperty(PROP_DEPENDENT_ROUTES_ITERATIONS)
  public int getDependentRoutesIterations() {
    return _dependentRoutesIterations;
  }

  @JsonProperty(MAIN_RIB_ROUTES_BY_ITERATION)
  public SortedMap<Integer, Integer> getMainRibRoutesByIteration() {
    return _mainRibRoutesByIteration;
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

  @JsonProperty(PROP_WARNINGS)
  public Warnings get_warnings() {
    return _warnings;
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

  @JsonProperty(PROP_DEPENDENT_ROUTES_ITERATIONS)
  public void setDependentRoutesIterations(int dependentRoutesIterations) {
    _dependentRoutesIterations = dependentRoutesIterations;
  }

  @JsonProperty(MAIN_RIB_ROUTES_BY_ITERATION)
  public void setMainRibRoutesByIteration(SortedMap<Integer, Integer> mainRibRoutesByIteration) {
    _mainRibRoutesByIteration = mainRibRoutesByIteration;
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
