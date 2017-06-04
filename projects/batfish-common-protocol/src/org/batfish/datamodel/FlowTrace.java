package org.batfish.datamodel;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FlowTrace implements Comparable<FlowTrace> {

   private final static String DISPOSITION_VAR = "disposition";
   private final static String HOPS_VAR = "hops";
   private final static String NOTES_VAR = "notes";

   private final FlowDisposition _disposition;

   private final List<FlowTraceHop> _hops;

   private final String _notes;

   public FlowTrace(@JsonProperty(DISPOSITION_VAR) FlowDisposition disposition,
         @JsonProperty(HOPS_VAR) List<FlowTraceHop> hops,
         @JsonProperty(NOTES_VAR) String notes) {
      _disposition = disposition;
      _hops = hops != null ? hops : Collections.emptyList();
      _notes = notes;
   }

   @Override
   public int compareTo(FlowTrace rhs) {
      for (int i = 0; i < _hops.size(); i++) {
         if (rhs._hops.size() < i + 1) {
            return 1;
         }
         Edge leftHop = _hops.get(i).getEdge();
         Edge rightHop = rhs._hops.get(i).getEdge();
         int result = leftHop.compareTo(rightHop);
         if (result != 0) {
            return result;
         }
      }
      if (rhs._hops.size() == _hops.size()) {
         return _disposition.compareTo(rhs._disposition);
      }
      else {
         return -1;
      }
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      FlowTrace rhs = (FlowTrace) obj;
      if (_disposition != rhs._disposition) {
         return false;
      }
      if (!_hops.equals(rhs._hops)) {
         return false;
      }
      return true;
   }

   @JsonProperty(DISPOSITION_VAR)
   public FlowDisposition getDisposition() {
      return _disposition;
   }

   @JsonProperty(HOPS_VAR)
   public List<FlowTraceHop> getHops() {
      return _hops;
   }

   @JsonProperty(NOTES_VAR)
   public String getNotes() {
      return _notes;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + _disposition.ordinal();
      result = prime * result + _hops.hashCode();
      return result;
   }

   @Override
   public String toString() {
      return toString("");
   }

   public String toString(String prefixString) {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < _hops.size(); i++) {
         FlowTraceHop hop = _hops.get(i);
         Set<String> routes = hop.getRoutes();
         String transformedFlowString = "";
         Flow transformedFlow = hop.getTransformedFlow();
         if (transformedFlow != null) {
            transformedFlowString = " ***TRANSFORMED:"
                  + transformedFlow.prettyPrint("") + "***";
         }
         String routesStr = routes != null ? (" --- " + routes) : "";
         Edge edge = hop.getEdge();
         int num = i + 1;
         sb.append(prefixString + "Hop " + num + ": " + edge.getNode1() + ":"
               + edge.getInt1() + " -> " + edge.getNode2() + ":"
               + edge.getInt2() + transformedFlowString + routesStr + "\n");
      }
      sb.append(prefixString + _notes + "\n");
      return sb.toString();
   }

}
