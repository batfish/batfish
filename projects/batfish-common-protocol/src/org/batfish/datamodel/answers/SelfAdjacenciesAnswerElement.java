package org.batfish.datamodel.answers;

import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.batfish.common.Pair;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SelfAdjacenciesAnswerElement implements AnswerElement {

   public static class InterfaceIpPair extends Pair<String, Ip> {

      private static final String INTERFACE_NAME_VAR = "interfaceName";

      private static final String IP_VAR = "ip";
      /**
       *
       */
      private static final long serialVersionUID = 1L;

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

   private SortedMap<String, SortedMap<Prefix, SortedSet<InterfaceIpPair>>> _selfAdjacencies;

   public SelfAdjacenciesAnswerElement() {
      _selfAdjacencies = new TreeMap<String, SortedMap<Prefix, SortedSet<InterfaceIpPair>>>();
   }

   public void add(String hostname, Prefix prefix, String interfaceName,
         Ip address) {
      SortedMap<Prefix, SortedSet<InterfaceIpPair>> prefixMap = _selfAdjacencies
            .get(hostname);
      if (prefixMap == null) {
         prefixMap = new TreeMap<Prefix, SortedSet<InterfaceIpPair>>();
         _selfAdjacencies.put(hostname, prefixMap);
      }
      SortedSet<InterfaceIpPair> interfaces = prefixMap.get(prefix);
      if (interfaces == null) {
         interfaces = new TreeSet<InterfaceIpPair>();
         prefixMap.put(prefix, interfaces);
      }
      interfaces.add(new InterfaceIpPair(interfaceName, address));
   }

   public SortedMap<String, SortedMap<Prefix, SortedSet<InterfaceIpPair>>> getSelfAdjacencies() {
      return _selfAdjacencies;
   }

   @Override
   public String prettyPrint() throws JsonProcessingException {
      // TODO: change this function to pretty print the answer
      ObjectMapper mapper = new BatfishObjectMapper();
      return mapper.writeValueAsString(this);
   }

   public void setSelfAdjacencies(
         SortedMap<String, SortedMap<Prefix, SortedSet<InterfaceIpPair>>> selfAdjacencies) {
      _selfAdjacencies = selfAdjacencies;
   }

}
