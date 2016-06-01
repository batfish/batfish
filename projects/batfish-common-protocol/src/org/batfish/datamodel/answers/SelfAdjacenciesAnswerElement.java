package org.batfish.datamodel.answers;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.batfish.common.Pair;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SelfAdjacenciesAnswerElement implements AnswerElement {

   public static class InterfaceIpPair extends Pair<String, Ip> {

      /**
       *
       */
      private static final long serialVersionUID = 1L;

      private static final String INTERFACE_NAME_VAR = "interfaceName";
      private static final String IP_VAR = "ip";
      
      @JsonCreator
      public InterfaceIpPair(@JsonProperty(INTERFACE_NAME_VAR) String t1, 
            @JsonProperty(IP_VAR) Ip t2) {
         super(t1, t2);
      }

      @JsonProperty(INTERFACE_NAME_VAR)
      public String getInterfaceName() {
         return _first;
      }

      @JsonProperty(IP_VAR)
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
