package org.batfish.bdp;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.batfish.common.util.ComparableStructure;
import org.batfish.datamodel.Configuration;

public final class Node extends ComparableStructure<String> {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   final Configuration _c;

   private Map<String, Node> _nodes;

   SortedMap<String, VirtualRouter> _virtualRouters;

   public Node(Configuration c, Map<String, Node> nodes) {
      super(c.getHostname());
      _c = c;
      _nodes = nodes;
      _virtualRouters = new TreeMap<>();
      for (String vrfName : _c.getVrfs().keySet()) {
         _virtualRouters.put(vrfName, new VirtualRouter(vrfName, _c, _nodes));
      }
   }

   public Configuration getConfiguration() {
      return _c;
   }

}
