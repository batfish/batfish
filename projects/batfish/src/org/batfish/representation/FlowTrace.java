package org.batfish.representation;

import java.util.ArrayList;
import java.util.List;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.collections.NodeInterfacePair;

public class FlowTrace implements Comparable<FlowTrace> {

   private final FlowDisposition _disposition;

   private final List<Edge> _hops;

   private final String _notes;

   public FlowTrace(String historyLine) {
      FlowDisposition disposition = null;
      String notes = "";
      _hops = new ArrayList<Edge>();
      String[] hops = historyLine.split("(\\];\\[)|(\\])|(\\[)");
      for (String hop : hops) {
         if (hop.length() == 0) {
            continue;
         }
         if (hop.contains("->")) {
            // ordinary hop
            String[] interfaceStrs = hop.split("->");
            String[] int1parts = interfaceStrs[0].split(":");
            String[] int2parts = interfaceStrs[1].split(":");
            String node1 = int1parts[0].replace("'", "").trim();
            String node2 = int2parts[0].replace("'", "").trim();
            String int1 = int1parts[1].replace("'", "").trim();
            String int2 = int2parts[1].replace("'", "").trim();
            NodeInterfacePair outgoingInterface = new NodeInterfacePair(node1,
                  int1);
            NodeInterfacePair incomingInterface = new NodeInterfacePair(node2,
                  int2);
            if (int1parts.length > 2) {
               if (int1parts[2].contains("deniedOut")) {
                  disposition = FlowDisposition.DENIED_OUT;
                  for (int i = 3; i < int1parts.length; i++) {
                     notes += "{" + int1parts[i].replace("'", "") + "}";
                  }
               }
            }
            if (int2parts.length > 2) {
               if (int2parts[2].contains("deniedIn")) {
                  disposition = FlowDisposition.DENIED_IN;
                  for (int i = 3; i < int2parts.length; i++) {
                     notes += "{" + int2parts[i].replace("'", "") + "}";
                  }
               }
            }
            _hops.add(new Edge(outgoingInterface, incomingInterface));
         }
         else if (hop.contains("accepted")) {
            disposition = FlowDisposition.ACCEPTED;
         }
         else if (hop.contains("nullRouted")) {
            disposition = FlowDisposition.NULL_ROUTED;
         }
         else if (hop.contains("noRoute")) {
            disposition = FlowDisposition.NO_ROUTE;
         }
         else if (hop.contains("neighborUnreachable")) {
            disposition = FlowDisposition.NEIGHBOR_UNREACHABLE;
         }
      }
      if (disposition == null) {
         throw new BatfishException(
               "Could not determine flow disposition for trace: " + historyLine);
      }
      _disposition = disposition;
      _notes = "Disposition: " + _disposition + notes;
   }

   @Override
   public int compareTo(FlowTrace rhs) {
      for (int i = 0; i < _hops.size(); i++) {
         if (rhs._hops.size() < i + 1) {
            return 1;
         }
         Edge leftHop = _hops.get(i);
         Edge rightHop = rhs._hops.get(i);
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

   public FlowDisposition getDisposition() {
      return _disposition;
   }

   public List<Edge> getHops() {
      return _hops;
   }

   public String getNotes() {
      return _notes;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + _disposition.hashCode();
      result = prime * result + _hops.hashCode();
      return result;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < _hops.size(); i++) {
         Edge hop = _hops.get(i);
         int num = i + 1;
         sb.append("Hop " + num + ": " + hop.getNode1() + ":" + hop.getInt1()
               + " -> " + hop.getNode2() + ":" + hop.getInt2() + "\n");
      }
      sb.append(_notes);
      sb.append("\n");
      return sb.toString();
   }

}
