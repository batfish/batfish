package org.batfish.collections;

import java.io.Serializable;

public abstract class Pair<T1 extends Comparable<T1>, T2 extends Comparable<T2>>
      implements Serializable, Comparable<Pair<T1, T2>> {

   private static final long serialVersionUID = 1L;

   protected final T1 _t1;

   protected final T2 _t2;

   public Pair(T1 t1, T2 t2) {
      _t1 = t1;
      _t2 = t2;
   }

   @Override
   public int compareTo(Pair<T1, T2> rhs) {
      int first = _t1.compareTo(rhs._t1);
      if (first == 0) {
         return _t2.compareTo(rhs._t2);
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
      if (_t1 == null) {
         if (other._t1 != null) {
            return false;
         }
      }
      else if (!_t1.equals(other._t1)) {
         return false;
      }
      if (_t2 == null) {
         if (other._t2 != null) {
            return false;
         }
      }
      else if (!_t2.equals(other._t2)) {
         return false;
      }
      return true;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((_t1 == null) ? 0 : _t1.hashCode());
      result = prime * result + ((_t2 == null) ? 0 : _t2.hashCode());
      return result;
   }

}
