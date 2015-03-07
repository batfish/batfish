package org.batfish.collections;

import java.util.Map;
import java.util.Set;

public abstract class AbstractMultiSet<E> implements MultiSet<E> {

   private final Map<E, Integer> _map;

   public AbstractMultiSet() {
      _map = initializeMap();
   }

   @Override
   public final void add(E e) {
      Integer oldCount = _map.get(e);
      if (oldCount == null) {
         _map.put(e, 1);
      }
      else {
         _map.put(e, oldCount + 1);
      }
   }

   @Override
   public final void add(Set<E> set) {
      for (E e : set) {
         add(e);
      }
   }

   @Override
   public final int count(E e) {
      Integer count = _map.get(e);
      if (count == null) {
         return 0;
      }
      else {
         return count;
      }
   }

   @Override
   public final Set<E> elements() {
      return _map.keySet();
   }

   protected abstract Map<E, Integer> initializeMap();

}
