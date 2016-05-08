package org.batfish.protocoldependency;

import org.batfish.common.datamodel.Prefix;
import org.batfish.common.datamodel.RoutingProtocol;

public class PotentialImport implements Comparable<PotentialImport> {

   protected final String _node;

   protected final PotentialExport _potentialExport;

   protected final Prefix _prefix;

   protected final RoutingProtocol _protocol;

   public PotentialImport(String node, Prefix prefix, RoutingProtocol protocol,
         PotentialExport potentialExport) {
      _node = node;
      _prefix = prefix;
      _protocol = protocol;
      _potentialExport = potentialExport;
   }

   @Override
   public int compareTo(PotentialImport rhs) {
      int ret = _prefix.compareTo(rhs._prefix);
      if (ret == 0) {
         ret = _node.compareTo(rhs._node);
         if (ret == 0) {
            ret = _protocol.compareTo(rhs._protocol);
         }
      }
      return ret;
   }

   public String getNode() {
      return _node;
   }

   public PotentialExport getPotentialExport() {
      return _potentialExport;
   }

   public Prefix getPrefix() {
      return _prefix;
   }

   public RoutingProtocol getProtocol() {
      return _protocol;
   }

   @Override
   public String toString() {
      return PotentialImport.class.getSimpleName() + "<" + _node + ", "
            + _prefix.toString() + ", " + _protocol.toString() + ">";
   }

}
