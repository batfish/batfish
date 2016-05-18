package org.batfish.datamodel.answers;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class PairwiseVpnConnectivityAnswerElement implements AnswerElement {

   private Map<String, Set<String>> _connectedNeighbors;

   private Set<String> _ipsecVpnNodes;

   private Map<String, Set<String>> _missingNeighbors;

   public PairwiseVpnConnectivityAnswerElement() {
      _connectedNeighbors = new TreeMap<String, Set<String>>();
      _ipsecVpnNodes = new TreeSet<String>();
      _missingNeighbors = new TreeMap<String, Set<String>>();
   }

   public Map<String, Set<String>> getConnectedNeighbors() {
      return _connectedNeighbors;
   }

   public Set<String> getIpsecVpnNodes() {
      return _ipsecVpnNodes;
   }

   public Map<String, Set<String>> getMissingNeighbors() {
      return _missingNeighbors;
   }

   public void setConnectedNeighbors(Map<String, Set<String>> connectedNeighbors) {
      _connectedNeighbors = connectedNeighbors;
   }

   public void setIpsecVpnNodes(Set<String> ipsecVpnNodes) {
      _ipsecVpnNodes = ipsecVpnNodes;
   }

   public void setMissingNeighbors(Map<String, Set<String>> missingNeighbors) {
      _missingNeighbors = missingNeighbors;
   }

}
