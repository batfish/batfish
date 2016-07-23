package org.batfish.datamodel.answers;

import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.batfish.common.util.BatfishObjectMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OspfLoopbacksAnswerElement implements AnswerElement {

   private SortedMap<String, SortedSet<String>> _active;

   private SortedMap<String, SortedSet<String>> _inactive;

   private SortedMap<String, SortedSet<String>> _passive;

   private SortedMap<String, SortedSet<String>> _running;

   public OspfLoopbacksAnswerElement() {
      _inactive = new TreeMap<String, SortedSet<String>>();
      _active = new TreeMap<String, SortedSet<String>>();
      _passive = new TreeMap<String, SortedSet<String>>();
      _running = new TreeMap<String, SortedSet<String>>();
   }

   public void add(SortedMap<String, SortedSet<String>> map, String hostname,
         String interfaceName) {
      SortedSet<String> interfacesByHostname = map.get(hostname);
      if (interfacesByHostname == null) {
         interfacesByHostname = new TreeSet<String>();
         map.put(hostname, interfacesByHostname);
      }
      interfacesByHostname.add(interfaceName);
   }

   public SortedMap<String, SortedSet<String>> getActive() {
      return _active;
   }

   public SortedMap<String, SortedSet<String>> getInactive() {
      return _inactive;
   }

   public SortedMap<String, SortedSet<String>> getPassive() {
      return _passive;
   }

   public SortedMap<String, SortedSet<String>> getRunning() {
      return _running;
   }

   public void setActive(SortedMap<String, SortedSet<String>> active) {
      _active = active;
   }

   public void setInactive(SortedMap<String, SortedSet<String>> inactive) {
      _inactive = inactive;
   }

   public void setPassive(SortedMap<String, SortedSet<String>> passive) {
      _passive = passive;
   }

   public void setRunning(SortedMap<String, SortedSet<String>> running) {
      _running = running;
   }

   @Override
   public String prettyPrint() throws JsonProcessingException {
      //TODO: change this function to pretty print the answer
      ObjectMapper mapper = new BatfishObjectMapper();
      return mapper.writeValueAsString(this);
   }
}
