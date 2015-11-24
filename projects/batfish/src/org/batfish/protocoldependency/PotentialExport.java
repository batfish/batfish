package org.batfish.protocoldependency;

import org.batfish.representation.Prefix;
import org.batfish.representation.RoutingProtocol;

public abstract class PotentialExport
      implements Comparable<PotentialExport> {

   private final DependentRoute _dependency;

   protected final String _node;

   protected final Prefix _prefix;

   protected final RoutingProtocol _protocol;

   public PotentialExport(String node, Prefix prefix,
         RoutingProtocol protocol, DependentRoute dependency) {
      _node = node;
      _prefix = prefix;
      _protocol = protocol;
      _dependency = dependency;
   }

   @Override
   public int compareTo(PotentialExport rhs) {
      int ret = _prefix.compareTo(rhs._prefix);
      if (ret == 0) {
         ret = _node.compareTo(rhs._node);
         if (ret == 0) {
            ret = _protocol.compareTo(rhs._protocol);
         }
      }
      return ret;
   }

   public DependentRoute getDependency() {
      return _dependency;
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
      return PotentialExport.class.getSimpleName() + "<" + _node + ", "
            + _prefix.toString() + ", " + _protocol.toString() + ">";
   }

}
