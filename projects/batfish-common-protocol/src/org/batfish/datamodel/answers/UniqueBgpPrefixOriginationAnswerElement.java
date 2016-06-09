package org.batfish.datamodel.answers;

import java.util.SortedMap;
import java.util.TreeMap;

import org.batfish.datamodel.PrefixSpace;

public class UniqueBgpPrefixOriginationAnswerElement implements AnswerElement {

   private SortedMap<String, SortedMap<String, PrefixSpace>> _intersections;

   private SortedMap<String, PrefixSpace> _prefixSpaces;

   public UniqueBgpPrefixOriginationAnswerElement() {
      _intersections = new TreeMap<String, SortedMap<String, PrefixSpace>>();
      _prefixSpaces = new TreeMap<String, PrefixSpace>();
   }

   public void addIntersection(String node1, String node2,
         PrefixSpace intersection) {
      SortedMap<String, PrefixSpace> intersections = _intersections.get(node1);
      if (intersections == null) {
         intersections = new TreeMap<String, PrefixSpace>();
         _intersections.put(node1, intersections);
      }
      intersections.put(node2, intersection);
   }

   public SortedMap<String, SortedMap<String, PrefixSpace>> getIntersections() {
      return _intersections;
   }

   public SortedMap<String, PrefixSpace> getPrefixSpaces() {
      return _prefixSpaces;
   }

   public void setIntersections(
         SortedMap<String, SortedMap<String, PrefixSpace>> intersections) {
      _intersections = intersections;
   }

   public void setPrefixSpaces(SortedMap<String, PrefixSpace> prefixSpaces) {
      _prefixSpaces = prefixSpaces;
   }

}
