package org.batfish.representation.vyos;

import java.util.Map;
import java.util.TreeMap;

import org.batfish.common.datamodel.Ip;
import org.batfish.common.util.ComparableStructure;

public class BgpProcess extends ComparableStructure<Integer> {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final Map<Ip, BgpNeighbor> _neighbors;

   public BgpProcess(int localAs) {
      super(localAs);
      _neighbors = new TreeMap<Ip, BgpNeighbor>();
   }

   public int getLocalAs() {
      return _key;
   }

   public Map<Ip, BgpNeighbor> getNeighbors() {
      return _neighbors;
   }

}
