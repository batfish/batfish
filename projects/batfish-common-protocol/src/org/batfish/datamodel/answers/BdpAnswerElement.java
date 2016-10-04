package org.batfish.datamodel.answers;

import java.util.SortedMap;

import com.fasterxml.jackson.core.JsonProcessingException;

public class BdpAnswerElement implements AnswerElement {

   private SortedMap<Integer, Integer> _bgpIterations;

   private int _dependentRoutesIterations;

   private int _ospfInternalIterations;

   private int _totalRoutes;

   public SortedMap<Integer, Integer> getBgpIterations() {
      return _bgpIterations;
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
   public String prettyPrint() throws JsonProcessingException {
      StringBuilder sb = new StringBuilder();
      sb.append("Computation summary:\n");
      sb.append("   OSPF-internal iterations: " + _dependentRoutesIterations
            + "\n");
      sb.append("   Dependent-routes iterations: " + _dependentRoutesIterations
            + "\n");
      sb.append("   BGP iterations: " + _dependentRoutesIterations + "\n");
      sb.append("   Total routes: " + _totalRoutes + "\n");
      return sb.toString();
   }

   public void setBgpIterations(SortedMap<Integer, Integer> bgpIterations) {
      _bgpIterations = bgpIterations;
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

}
