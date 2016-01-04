package org.batfish.common;

import java.io.Serializable;

public class Pair<T1 extends Comparable<? super T1>, T2 extends Comparable<? super T2>>
      implements Serializable, Comparable<Pair<T1, T2>> {

   private static final long serialVersionUID = 1L;

   protected final T1 _first;

   protected final T2 _second;

   public Pair(T1 t1, T2 t2) {
      _first = t1;
      _second = t2;
   }

   @Override
   public int compareTo(Pair<T1, T2> rhs) {
      int first = _first.compareTo(rhs._first);
      if (first == 0) {
         return _second.compareTo(rhs._second);
      }
      else {
         return first;
      }
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      if (obj == null) {
         return false;
      }
      Pair<?, ?> other = (Pair<?, ?>) obj;
      if (_first == null) {
         if (other._first != null) {
            return false;
         }
      }
      else if (!_first.equals(other._first)) {
         return false;
      }
      if (_second == null) {
         if (other._second != null) {
            return false;
         }
      }
      else if (!_second.equals(other._second)) {
         return false;
      }
      return true;
   }

   public final T1 getFirst() {
      return _first;
   }

   public final T2 getSecond() {
      return _second;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((_first == null) ? 0 : _first.hashCode());
      result = prime * result + ((_second == null) ? 0 : _second.hashCode());
      return result;
   }

   @Override
   public String toString() {
      return "<" + _first.toString() + ":" + _second.toString() + ">";
   }

}
