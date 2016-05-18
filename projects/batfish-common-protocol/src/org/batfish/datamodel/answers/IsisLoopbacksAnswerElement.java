package org.batfish.datamodel.answers;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class IsisLoopbacksAnswerElement implements AnswerElement {

   private Map<String, Set<String>> _inactive;

   private Map<String, Set<String>> _l1;

   private Map<String, Set<String>> _l1Active;

   private Map<String, Set<String>> _l1Passive;

   private Map<String, Set<String>> _l2;

   private Map<String, Set<String>> _l2Active;

   private Map<String, Set<String>> _l2Passive;

   private Map<String, Set<String>> _running;

   public IsisLoopbacksAnswerElement() {
      _inactive = new TreeMap<String, Set<String>>();
      _l1 = new TreeMap<String, Set<String>>();
      _l1Active = new TreeMap<String, Set<String>>();
      _l1Passive = new TreeMap<String, Set<String>>();
      _l2 = new TreeMap<String, Set<String>>();
      _l2Active = new TreeMap<String, Set<String>>();
      _l2Passive = new TreeMap<String, Set<String>>();
      _running = new TreeMap<String, Set<String>>();
   }

   public void add(Map<String, Set<String>> map, String hostname,
         String interfaceName) {
      Set<String> interfacesByHostname = map.get(hostname);
      if (interfacesByHostname == null) {
         interfacesByHostname = new TreeSet<String>();
         map.put(hostname, interfacesByHostname);
      }
      interfacesByHostname.add(interfaceName);
   }

   public Map<String, Set<String>> getInactive() {
      return _inactive;
   }

   public Map<String, Set<String>> getL1() {
      return _l1;
   }

   public Map<String, Set<String>> getL1Active() {
      return _l1Active;
   }

   public Map<String, Set<String>> getL1Passive() {
      return _l1Passive;
   }

   public Map<String, Set<String>> getL2() {
      return _l2;
   }

   public Map<String, Set<String>> getL2Active() {
      return _l2Active;
   }

   public Map<String, Set<String>> getL2Passive() {
      return _l2Passive;
   }

   public Map<String, Set<String>> getRunning() {
      return _running;
   }

   public void setInactive(Map<String, Set<String>> inactive) {
      _inactive = inactive;
   }

   public void setL1(Map<String, Set<String>> l1) {
      _l1 = l1;
   }

   public void setL1Active(Map<String, Set<String>> l1Active) {
      _l1Active = l1Active;
   }

   public void setL1Passive(Map<String, Set<String>> l1Passive) {
      _l1Passive = l1Passive;
   }

   public void setL2(Map<String, Set<String>> l2) {
      _l2 = l2;
   }

   public void setL2Active(Map<String, Set<String>> l2Active) {
      _l2Active = l2Active;
   }

   public void setL2Passive(Map<String, Set<String>> l2Passive) {
      _l2Passive = l2Passive;
   }

   public void setRunning(Map<String, Set<String>> running) {
      _running = running;
   }

}
