package org.batfish.datamodel.answers;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class OspfLoopbacksAnswerElement implements AnswerElement {

   private Map<String, Set<String>> _active;

   private Map<String, Set<String>> _inactive;

   private Map<String, Set<String>> _passive;

   private Map<String, Set<String>> _running;

   public OspfLoopbacksAnswerElement() {
      _inactive = new TreeMap<String, Set<String>>();
      _active = new TreeMap<String, Set<String>>();
      _passive = new TreeMap<String, Set<String>>();
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

   public Map<String, Set<String>> getActive() {
      return _active;
   }

   public Map<String, Set<String>> getInactive() {
      return _inactive;
   }

   public Map<String, Set<String>> getPassive() {
      return _passive;
   }

   public Map<String, Set<String>> getRunning() {
      return _running;
   }

   public void setActive(Map<String, Set<String>> active) {
      _active = active;
   }

   public void setInactive(Map<String, Set<String>> inactive) {
      _inactive = inactive;
   }

   public void setPassive(Map<String, Set<String>> passive) {
      _passive = passive;
   }

   public void setRunning(Map<String, Set<String>> running) {
      _running = running;
   }

}
