package org.batfish.collections;

import java.util.Map;
import java.util.TreeMap;

public class TreeMultiSet<E> extends AbstractMultiSet<E> {

   @Override
   protected Map<E, Integer> initializeMap() {
      return new TreeMap<E, Integer>();
   }

}
