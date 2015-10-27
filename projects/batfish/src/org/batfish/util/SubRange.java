package org.batfish.util;

import java.io.Serializable;

public final class SubRange implements Serializable, Comparable<SubRange> {

   private static final long serialVersionUID = 1L;

   private final int _end;

   private final int _start;

   public SubRange(int start, int end) {
      _start = start;
      _end = end;
   }

   @Override
   public int compareTo(SubRange rhs) {
      int ret = Integer.compare(_start, rhs._start);
      if (ret == 0) {
         ret = Integer.compare(_end, rhs._end);
      }
      return ret;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      SubRange other = (SubRange) obj;
      if (_end != other._end) {
         return false;
      }
      if (_start != other._start) {
         return false;
      }
      return true;
   }

   public int getEnd() {
      return _end;
   }

   public int getStart() {
      return _start;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + _end;
      result = prime * result + _start;
      return result;
   }

   @Override
   public String toString() {
      return "[" + _start + "," + _end + "]";
   }

}
