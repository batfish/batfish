package org.batfish.datamodel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public class AsPath implements Serializable {

   private static final long serialVersionUID = 1L;

   private final List<SortedSet<Integer>> _asSets;

   @JsonCreator
   public AsPath(List<SortedSet<Integer>> asSets) {
      _asSets = copyAsSets(asSets);
   }

   public boolean containsAs(int as) {
      for (Set<Integer> asSet : _asSets) {
         if (asSet.contains(as)) {
            return true;
         }
      }
      return false;
   }

   private List<SortedSet<Integer>> copyAsSets(
         List<SortedSet<Integer>> asSets) {
      List<SortedSet<Integer>> newAsSets = new ArrayList<>();
      for (SortedSet<Integer> asSet : asSets) {
         SortedSet<Integer> newAsSet = new TreeSet<>(asSet);
         newAsSets.add(newAsSet);
      }
      return newAsSets;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      if (obj == null) {
         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      AsPath other = (AsPath) obj;
      if (_asSets == null) {
         if (other._asSets != null) {
            return false;
         }
      }
      else if (!_asSets.equals(other._asSets)) {
         return false;
      }
      return true;
   }

   public String getAsPathString() {
      StringBuilder sb = new StringBuilder();
      for (Set<Integer> asSet : _asSets) {
         if (asSet.size() == 1) {
            int elem = asSet.iterator().next();
            sb.append(elem);
         }
         else {
            sb.append("{");
            Iterator<Integer> i = asSet.iterator();
            sb.append(i.next());
            while (i.hasNext()) {
               sb.append(",");
               sb.append(i.next());
            }
            sb.append("}");
         }
         sb.append(" ");
      }
      String result = sb.toString().trim();
      return result;
   }

   @JsonValue
   public List<SortedSet<Integer>> getAsSets() {
      return copyAsSets(_asSets);
   }

   @Override
   public int hashCode() {
      return _asSets.hashCode();
   }

   public int size() {
      return _asSets.size();
   }

   @Override
   public String toString() {
      return _asSets.toString();
   }

}
