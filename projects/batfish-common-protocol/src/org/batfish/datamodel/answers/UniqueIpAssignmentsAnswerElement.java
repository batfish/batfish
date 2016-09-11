package org.batfish.datamodel.answers;

import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.collections.NodeInterfacePair;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UniqueIpAssignmentsAnswerElement implements AnswerElement {

   private SortedMap<Ip, SortedSet<NodeInterfacePair>> _allIps;

   private SortedMap<Ip, SortedSet<NodeInterfacePair>> _enabledIps;

   public UniqueIpAssignmentsAnswerElement() {
      _allIps = new TreeMap<>();
      _enabledIps = new TreeMap<>();
   }

   public void add(SortedMap<Ip, SortedSet<NodeInterfacePair>> map, Ip ip,
         String hostname, String interfaceName) {
      SortedSet<NodeInterfacePair> interfaces = map.get(ip);
      if (interfaces == null) {
         interfaces = new TreeSet<>();
         map.put(ip, interfaces);
      }
      interfaces.add(new NodeInterfacePair(hostname, interfaceName));
   }

   public SortedMap<Ip, SortedSet<NodeInterfacePair>> getAllIps() {
      return _allIps;
   }

   public SortedMap<Ip, SortedSet<NodeInterfacePair>> getEnabledIps() {
      return _enabledIps;
   }

   @Override
   public String prettyPrint() throws JsonProcessingException {
      // TODO: change this function to pretty print the answer
      ObjectMapper mapper = new BatfishObjectMapper();
      return mapper.writeValueAsString(this);
   }

   public void setAllIps(SortedMap<Ip, SortedSet<NodeInterfacePair>> allIps) {
      _allIps = allIps;
   }

   public void setEnabledIps(
         SortedMap<Ip, SortedSet<NodeInterfacePair>> enabledIps) {
      _enabledIps = enabledIps;
   }

}
