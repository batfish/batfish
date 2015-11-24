package org.batfish.protocoldependency;

import java.util.HashSet;
import java.util.Set;

import org.batfish.representation.Prefix;
import org.batfish.representation.RoutingProtocol;

public class DependentRoute implements Comparable<DependentRoute> {

   private final Set<DependentRoute> _dependencies;

   private final String _node;

   private final Prefix _prefix;

   private final RoutingProtocol _protocol;

   public DependentRoute(String node, Prefix prefix, RoutingProtocol protocol) {
      _node = node;
      _prefix = prefix;
      _protocol = protocol;
      _dependencies = new HashSet<DependentRoute>();
   }

   @Override
   public int compareTo(DependentRoute rhs) {
      int ret = _prefix.compareTo(rhs._prefix);
      if (ret == 0) {
         ret = _node.compareTo(rhs._node);
         if (ret == 0) {
            ret = _protocol.compareTo(rhs._protocol);
         }
      }
      return ret;
   }

   public Set<DependentRoute> getDependencies() {
      return _dependencies;
   }

   public String getNode() {
      return _node;
   }

   public Prefix getPrefix() {
      return _prefix;
   }

   public RoutingProtocol getProtocol() {
      return _protocol;
   }

   @Override
   public String toString() {
      return DependentRoute.class.getSimpleName() + "<" + _node + ", "
            + _prefix.toString() + ", " + _protocol.toString() + ">";
   }

}
