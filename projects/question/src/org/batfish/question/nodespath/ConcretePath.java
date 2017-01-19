package org.batfish.question.nodespath;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public class ConcretePath implements Comparable<ConcretePath> {

   private final List<String> _parts;

   @JsonCreator
   private ConcretePath(List<String> parts) {
      _parts = parts;
   }

   public ConcretePath(String text) {
      String endsCut = text.substring(3, text.length() - 2);
      _parts = Arrays.asList(endsCut.split("'\\]\\['"));
   }

   @Override
   public int compareTo(ConcretePath o) {
      Iterator<String> iLhs = _parts.iterator();
      Iterator<String> iRhs = o._parts.iterator();
      do {
         boolean iLhsNext = iLhs.hasNext();
         boolean iRhsNext = iRhs.hasNext();
         if (iLhsNext && iRhsNext) {
            int c = iLhs.next().compareTo(iRhs.next());
            if (c != 0) {
               return c;
            }
         }
         else if (iLhsNext) {
            return 1;
         }
         else if (iRhsNext) {
            return -1;
         }
         else {
            return 0;
         }
      } while (true);
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
      return _parts.equals(((ConcretePath) obj)._parts);
   }

   @JsonValue
   public final List<String> getParts() {
      return _parts;
   }

   @Override
   public int hashCode() {
      return _parts.hashCode();
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("'" + _parts.get(0) + "'");
      for (int i = 1; i < _parts.size(); i++) {
         String currentPart = _parts.get(i);
         sb.append("->" + "'" + currentPart + "'");
      }
      String result = sb.toString();
      return result;
   }

}
