package org.batfish.protocoldependency;

import java.util.Set;
import java.util.TreeSet;

import org.batfish.representation.RoutingProtocol;

public class DependentProtocol implements Comparable<DependentProtocol> {

   private Set<Dependent> _dependents;

   private RoutingProtocol _protocol;

   public DependentProtocol(RoutingProtocol protocol) {
      _protocol = protocol;
      _dependents = new TreeSet<Dependent>();
   }

   @Override
   public int compareTo(DependentProtocol rhs) {
      return _protocol.compareTo(rhs._protocol);
   }

   public Set<Dependent> getDependents() {
      return _dependents;
   }

   public RoutingProtocol getRoutingProtocol() {
      return _protocol;
   }

}
