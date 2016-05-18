package org.batfish.datamodel.answers;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.batfish.common.Pair;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;

public class SelfAdjacenciesAnswerElement implements AnswerElement {

   public static class InterfaceIpPair extends Pair<String, Ip> {

      /**
       *
       */
      private static final long serialVersionUID = 1L;

      public InterfaceIpPair(String t1, Ip t2) {
         super(t1, t2);
      }

      public String getInterfaceName() {
         return _first;
      }

      public Ip getIp() {
         return _second;
      }

   }

   private Map<String, Map<Prefix, Set<InterfaceIpPair>>> _selfAdjacencies;

   public SelfAdjacenciesAnswerElement() {
      _selfAdjacencies = new TreeMap<String, Map<Prefix, Set<InterfaceIpPair>>>();
   }

   public void add(String hostname, Prefix prefix, String interfaceName,
         Ip address) {
      Map<Prefix, Set<InterfaceIpPair>> prefixMap = _selfAdjacencies
            .get(hostname);
      if (prefixMap == null) {
         prefixMap = new TreeMap<Prefix, Set<InterfaceIpPair>>();
         _selfAdjacencies.put(hostname, prefixMap);
      }
      Set<InterfaceIpPair> interfaces = prefixMap.get(prefix);
      if (interfaces == null) {
         interfaces = new TreeSet<InterfaceIpPair>();
         prefixMap.put(prefix, interfaces);
      }
      interfaces.add(new InterfaceIpPair(interfaceName, address));
   }

   public Map<String, Map<Prefix, Set<InterfaceIpPair>>> getSelfAdjacencies() {
      return _selfAdjacencies;
   }

   public void setSelfAdjacencies(
         Map<String, Map<Prefix, Set<InterfaceIpPair>>> selfAdjacencies) {
      _selfAdjacencies = selfAdjacencies;
   }

}
