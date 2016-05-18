package org.batfish.datamodel.answers;

import java.util.Map;
import java.util.TreeMap;

import org.batfish.datamodel.PrefixSpace;

public class UniqueBgpPrefixOriginationAnswerElement implements AnswerElement {

   private Map<String, Map<String, PrefixSpace>> _intersections;

   private Map<String, PrefixSpace> _prefixSpaces;

   public UniqueBgpPrefixOriginationAnswerElement() {
      _intersections = new TreeMap<String, Map<String, PrefixSpace>>();
      _prefixSpaces = new TreeMap<String, PrefixSpace>();
   }

   public void addIntersection(String node1, String node2,
         PrefixSpace intersection) {
      Map<String, PrefixSpace> intersections = _intersections.get(node1);
      if (intersections == null) {
         intersections = new TreeMap<String, PrefixSpace>();
         _intersections.put(node1, intersections);
      }
      intersections.put(node2, intersection);
   }

   public Map<String, Map<String, PrefixSpace>> getIntersections() {
      return _intersections;
   }

   public Map<String, PrefixSpace> getPrefixSpaces() {
      return _prefixSpaces;
   }

   public void setIntersections(
         Map<String, Map<String, PrefixSpace>> intersections) {
      _intersections = intersections;
   }

   public void setPrefixSpaces(Map<String, PrefixSpace> prefixSpaces) {
      _prefixSpaces = prefixSpaces;
   }

}
