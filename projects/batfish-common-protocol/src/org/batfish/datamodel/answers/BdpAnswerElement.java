package org.batfish.datamodel.answers;

import java.util.SortedMap;
import java.util.TreeMap;

public class BdpAnswerElement implements DataPlaneAnswerElement {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private SortedMap<Integer, Integer> _bgpRoutesByIteration;

   private int _dependentRoutesIterations;

   private int _ospfInternalIterations;

   private int _totalRoutes;

   private String _version;

   public BdpAnswerElement() {
      _bgpRoutesByIteration = new TreeMap<>();
   }

   public SortedMap<Integer, Integer> getBgpRoutesByIteration() {
      return _bgpRoutesByIteration;
   }

   public int getDependentRoutesIterations() {
      return _dependentRoutesIterations;
   }

   public int getOspfInternalIterations() {
      return _ospfInternalIterations;
   }

   public int getTotalRoutes() {
      return _totalRoutes;
   }

   @Override
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
      sb.append("   BGP routes in each iteration: "
            + _bgpRoutesByIteration.values().toString() + "\n");
      sb.append("   Total routes: " + _totalRoutes + "\n");
      return sb.toString();
   }

   public void setBgpRoutesByIteration(
         SortedMap<Integer, Integer> bgpRoutesByIteration) {
      _bgpRoutesByIteration = bgpRoutesByIteration;
   }

   public void setDependentRoutesIterations(int dependentRoutesIterations) {
      _dependentRoutesIterations = dependentRoutesIterations;
   }

   public void setOspfInternalIterations(int ospfInternalIterations) {
      _ospfInternalIterations = ospfInternalIterations;
   }

   public void setTotalRoutes(int totalRoutes) {
      _totalRoutes = totalRoutes;
   }

   public void setVersion(String version) {
      _version = version;
   }

}
