package org.batfish.nxtnet;

import java.util.Map;

import org.batfish.datamodel.Edge;
import org.batfish.datamodel.Topology;

public class TopologyFactExtractor {

   private Topology _topology;

   public TopologyFactExtractor(Topology topology) {
      _topology = topology;
   }

   public void writeFacts(Map<String, StringBuilder> factBins) {
      StringBuilder wSamePhysicalSegment = factBins.get("SamePhysicalSegment");
      for (Edge e : _topology.getEdges()) {
         wSamePhysicalSegment.append(e.getNode1() + "|" + e.getInt1() + "|"
               + e.getNode2() + "|" + e.getInt2() + "\n");
      }
   }
}
