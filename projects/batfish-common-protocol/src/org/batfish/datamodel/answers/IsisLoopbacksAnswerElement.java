package org.batfish.datamodel.answers;

import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.batfish.common.util.BatfishObjectMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class IsisLoopbacksAnswerElement implements AnswerElement {

   private SortedMap<String, SortedSet<String>> _inactive;

   private SortedMap<String, SortedSet<String>> _l1;

   private SortedMap<String, SortedSet<String>> _l1Active;

   private SortedMap<String, SortedSet<String>> _l1Passive;

   private SortedMap<String, SortedSet<String>> _l2;

   private SortedMap<String, SortedSet<String>> _l2Active;

   private SortedMap<String, SortedSet<String>> _l2Passive;

   private SortedMap<String, SortedSet<String>> _running;

   public IsisLoopbacksAnswerElement() {
      _inactive = new TreeMap<String, SortedSet<String>>();
      _l1 = new TreeMap<String, SortedSet<String>>();
      _l1Active = new TreeMap<String, SortedSet<String>>();
      _l1Passive = new TreeMap<String, SortedSet<String>>();
      _l2 = new TreeMap<String, SortedSet<String>>();
      _l2Active = new TreeMap<String, SortedSet<String>>();
      _l2Passive = new TreeMap<String, SortedSet<String>>();
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

   public SortedMap<String, SortedSet<String>> getInactive() {
      return _inactive;
   }

   public SortedMap<String, SortedSet<String>> getL1() {
      return _l1;
   }

   public SortedMap<String, SortedSet<String>> getL1Active() {
      return _l1Active;
   }

   public SortedMap<String, SortedSet<String>> getL1Passive() {
      return _l1Passive;
   }

   public SortedMap<String, SortedSet<String>> getL2() {
      return _l2;
   }

   public SortedMap<String, SortedSet<String>> getL2Active() {
      return _l2Active;
   }

   public SortedMap<String, SortedSet<String>> getL2Passive() {
      return _l2Passive;
   }

   public SortedMap<String, SortedSet<String>> getRunning() {
      return _running;
   }

   public void setInactive(SortedMap<String, SortedSet<String>> inactive) {
      _inactive = inactive;
   }

   public void setL1(SortedMap<String, SortedSet<String>> l1) {
      _l1 = l1;
   }

   public void setL1Active(SortedMap<String, SortedSet<String>> l1Active) {
      _l1Active = l1Active;
   }

   public void setL1Passive(SortedMap<String, SortedSet<String>> l1Passive) {
      _l1Passive = l1Passive;
   }

   public void setL2(SortedMap<String, SortedSet<String>> l2) {
      _l2 = l2;
   }

   public void setL2Active(SortedMap<String, SortedSet<String>> l2Active) {
      _l2Active = l2Active;
   }

   public void setL2Passive(SortedMap<String, SortedSet<String>> l2Passive) {
      _l2Passive = l2Passive;
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
