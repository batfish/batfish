package org.batfish.datamodel.answers;

import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.batfish.datamodel.Ip;
import org.batfish.datamodel.collections.NodeInterfacePair;

public class UniqueIpAssignmentsAnswerElement implements AnswerElement {

   private SortedMap<Ip, SortedSet<NodeInterfacePair>> _allIps;

   private DiffLabel _diffLabel;

   private SortedMap<Ip, SortedSet<NodeInterfacePair>> _enabledIps;

   public UniqueIpAssignmentsAnswerElement() {
      _allIps = new TreeMap<Ip, SortedSet<NodeInterfacePair>>();
      _enabledIps = new TreeMap<Ip, SortedSet<NodeInterfacePair>>();
   }

   public void add(SortedMap<Ip, SortedSet<NodeInterfacePair>> map, Ip ip,
         String hostname, String interfaceName) {
      SortedSet<NodeInterfacePair> interfaces = map.get(ip);
      if (interfaces == null) {
         interfaces = new TreeSet<NodeInterfacePair>();
         map.put(ip, interfaces);
      }
      interfaces.add(new NodeInterfacePair(hostname, interfaceName));
   }

   public SortedMap<Ip, SortedSet<NodeInterfacePair>> getAllIps() {
      return _allIps;
   }

   public DiffLabel getDiffLabel() {
      return _diffLabel;
   }

   public SortedMap<Ip, SortedSet<NodeInterfacePair>> getEnabledIps() {
      return _enabledIps;
   }

   public void setAllIps(SortedMap<Ip, SortedSet<NodeInterfacePair>> allIps) {
      _allIps = allIps;
   }

   public void setDiffLabel(DiffLabel diffLabel) {
      _diffLabel = diffLabel;
   }

   public void setEnabledIps(
         SortedMap<Ip, SortedSet<NodeInterfacePair>> enabledIps) {
      _enabledIps = enabledIps;
   }

}
