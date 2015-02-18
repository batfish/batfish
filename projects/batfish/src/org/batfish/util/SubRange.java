package org.batfish.util;

import java.io.Serializable;

public class SubRange implements Serializable {

   private static final long serialVersionUID = 1L;

   private int _end;
   private int _start;

   public SubRange(int start, int end) {
      _start = start;
      _end = end;
   }

   public int getEnd() {
      return _end;
   }

   public int getStart() {
      return _start;
   }

   @Override
   public String toString() {
      return "[" + _start + "," + _end + "]";
   }
}
