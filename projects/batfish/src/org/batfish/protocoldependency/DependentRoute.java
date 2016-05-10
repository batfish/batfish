package org.batfish.protocoldependency;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;

public class DependentRoute implements Comparable<DependentRoute> {

   private final Set<DependentRoute> _dependencies;

   private transient String _dotNodeId;

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

   public boolean dependsOn(RoutingProtocol protocol) {
      for (DependentRoute dependentRoute : _dependencies) {
         if (dependentRoute.dependsOn(protocol)) {
            return true;
         }
      }
      return _protocol == protocol;
   }

   public Set<DependentRoute> getDependencies() {
      return _dependencies;
   }

   public Set<DependentRoute> getDependentClosure() {
      Set<DependentRoute> closure = new LinkedHashSet<DependentRoute>();
      for (DependentRoute dependency : _dependencies) {
         Set<DependentRoute> reflexiveClosure = dependency
               .getReflexiveClosure();
         closure.addAll(reflexiveClosure);
      }
      return closure;
   }

   public String getDotNodeId() {
      return _dotNodeId;
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

   public Set<ProtocolDependency> getProtocolDependencies() {
      Set<ProtocolDependency> protocolDependencies = new LinkedHashSet<ProtocolDependency>();
      for (DependentRoute dependency : _dependencies) {
         Set<ProtocolDependency> recursiveDependencies = dependency
               .getProtocolDependencies();
         for (ProtocolDependency recursiveDependency : recursiveDependencies) {
            RoutingProtocol protocol = recursiveDependency.getProtocol();
            int indirectionLevel = recursiveDependency.getIndirectionLevel() + 1;
            protocolDependencies.add(new ProtocolDependency(protocol,
                  indirectionLevel));
         }
      }
      protocolDependencies.add(new ProtocolDependency(_protocol, 0));
      return protocolDependencies;
   }

   private Set<DependentRoute> getReflexiveClosure() {
      Set<DependentRoute> closure = new LinkedHashSet<DependentRoute>();
      for (DependentRoute dependency : _dependencies) {
         Set<DependentRoute> reflexiveClosure = dependency
               .getReflexiveClosure();
         closure.addAll(reflexiveClosure);
      }
      closure.add(this);
      return closure;
   }

   public void setDotNodeId(String dotNodeId) {
      _dotNodeId = dotNodeId;
   }

   @Override
   public String toString() {
      return DependentRoute.class.getSimpleName() + "<" + _node + ", "
            + _prefix.toString() + ", " + _protocol.toString() + ">";
   }

}
