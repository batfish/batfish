package org.batfish.datamodel.answers;

import java.util.SortedMap;
import java.util.TreeMap;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BdpAnswerElement implements DataPlaneAnswerElement {

   private static final String BGP_BEST_PATH_RIB_ROUTES_BY_ITERATION_VAR = "bgpBestPathRibRoutesByIteration";

   private static final String BGP_MULTIPATH_RIB_ROUTES_BY_ITERATION_VAR = "bgpMultipathRibRoutesByIteration";

   private static final String DEPENDENT_ROUTES_ITERATIONS_VAR = "dependentRoutesIterations";

   private static final String MAIN_RIB_ROUTES_BY_ITERATION = "mainRibRoutesByIteration";

   private static final String OSPF_INTERNAL_ITERATIONS_VAR = "ospfInternalIterations";

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private SortedMap<Integer, Integer> _bgpBestPathRibRoutesByIteration;

   private SortedMap<Integer, Integer> _bgpMultipathRibRoutesByIteration;

   private int _dependentRoutesIterations;

   private SortedMap<Integer, Integer> _mainRibRoutesByIteration;

   private int _ospfInternalIterations;

   private String _version;

   public BdpAnswerElement() {
      _bgpBestPathRibRoutesByIteration = new TreeMap<>();
      _bgpMultipathRibRoutesByIteration = new TreeMap<>();
      _mainRibRoutesByIteration = new TreeMap<>();
   }

   @JsonProperty(BGP_BEST_PATH_RIB_ROUTES_BY_ITERATION_VAR)
   public SortedMap<Integer, Integer> getBgpBestPathRibRoutesByIteration() {
      return _bgpBestPathRibRoutesByIteration;
   }

   @JsonProperty(BGP_MULTIPATH_RIB_ROUTES_BY_ITERATION_VAR)
   public SortedMap<Integer, Integer> getBgpMultipathRibRoutesByIteration() {
      return _bgpMultipathRibRoutesByIteration;
   }

   @JsonProperty(DEPENDENT_ROUTES_ITERATIONS_VAR)
   public int getDependentRoutesIterations() {
      return _dependentRoutesIterations;
   }

   @JsonProperty(MAIN_RIB_ROUTES_BY_ITERATION)
   public SortedMap<Integer, Integer> getMainRibRoutesByIteration() {
      return _mainRibRoutesByIteration;
   }

   @JsonProperty(OSPF_INTERNAL_ITERATIONS_VAR)
   public int getOspfInternalIterations() {
      return _ospfInternalIterations;
   }

   @Override
   @JsonProperty(VERSION_VAR)
   public String getVersion() {
      return _version;
   }

   @Override
   public String prettyPrint() {
      StringBuilder sb = new StringBuilder();
      sb.append("Computation summary:\n");
      sb.append("   OSPF-internal iterations: " + _dependentRoutesIterations
            + "\n");
      sb.append("   Dependent-routes iterations: " + _dependentRoutesIterations
            + "\n");
      sb.append("   BGP best-path RIB routes by iteration: "
            + _bgpBestPathRibRoutesByIteration.toString() + "\n");
      sb.append("   BGP multipath RIB routes by iteration: "
            + _bgpMultipathRibRoutesByIteration.toString() + "\n");
      sb.append("   Main RIB routes by iteration: "
            + _mainRibRoutesByIteration.toString() + "\n");
      return sb.toString();
   }

   @JsonProperty(BGP_BEST_PATH_RIB_ROUTES_BY_ITERATION_VAR)
   public void setBgpBestPathRibRoutesByIteration(
         SortedMap<Integer, Integer> bgpBestPathRibRoutesByIteration) {
      _bgpBestPathRibRoutesByIteration = bgpBestPathRibRoutesByIteration;
   }

   @JsonProperty(BGP_MULTIPATH_RIB_ROUTES_BY_ITERATION_VAR)
   public void setBgpMultipathRibRoutesByIteration(
         SortedMap<Integer, Integer> bgpMultipathRibRoutesByIteration) {
      _bgpMultipathRibRoutesByIteration = bgpMultipathRibRoutesByIteration;
   }

   @JsonProperty(DEPENDENT_ROUTES_ITERATIONS_VAR)
   public void setDependentRoutesIterations(int dependentRoutesIterations) {
      _dependentRoutesIterations = dependentRoutesIterations;
   }

   @JsonProperty(MAIN_RIB_ROUTES_BY_ITERATION)
   public void setMainRibRoutesByIteration(
         SortedMap<Integer, Integer> mainRibRoutesByIteration) {
      _mainRibRoutesByIteration = mainRibRoutesByIteration;
   }

   @JsonProperty(OSPF_INTERNAL_ITERATIONS_VAR)
   public void setOspfInternalIterations(int ospfInternalIterations) {
      _ospfInternalIterations = ospfInternalIterations;
   }

   @JsonProperty(VERSION_VAR)
   public void setVersion(String version) {
      _version = version;
   }

}
