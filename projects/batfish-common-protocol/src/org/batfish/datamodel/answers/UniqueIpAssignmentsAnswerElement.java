package org.batfish.datamodel.answers;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.batfish.datamodel.Ip;
import org.batfish.datamodel.collections.NodeInterfacePair;

public class UniqueIpAssignmentsAnswerElement implements AnswerElement {

   private Map<Ip, Set<NodeInterfacePair>> _allIps;

   private Map<Ip, Set<NodeInterfacePair>> _enabledIps;

   public UniqueIpAssignmentsAnswerElement() {
      _allIps = new TreeMap<Ip, Set<NodeInterfacePair>>();
      _enabledIps = new TreeMap<Ip, Set<NodeInterfacePair>>();
   }

   public void add(Map<Ip, Set<NodeInterfacePair>> map, Ip ip, String hostname,
         String interfaceName) {
      Set<NodeInterfacePair> interfaces = map.get(ip);
      if (interfaces == null) {
         interfaces = new TreeSet<NodeInterfacePair>();
         map.put(ip, interfaces);
      }
      interfaces.add(new NodeInterfacePair(hostname, interfaceName));
   }

   public Map<Ip, Set<NodeInterfacePair>> getAllIps() {
      return _allIps;
   }

   public Map<Ip, Set<NodeInterfacePair>> getEnabledIps() {
      return _enabledIps;
   }

   public void setAllIps(Map<Ip, Set<NodeInterfacePair>> allIps) {
      _allIps = allIps;
   }

   public void setEnabledIps(Map<Ip, Set<NodeInterfacePair>> enabledIps) {
      _enabledIps = enabledIps;
   }

}
