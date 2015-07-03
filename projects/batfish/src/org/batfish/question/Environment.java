package org.batfish.question;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.batfish.representation.BgpNeighbor;
import org.batfish.representation.Configuration;
import org.batfish.representation.Interface;
import org.batfish.representation.Ip;

public class Environment {

   private BgpNeighbor _bgpNeighbor;

   private Map<String, Configuration> _configurations;

   private Interface _interface;

   private Map<String, Set<Ip>> _ipSets;

   private Configuration _node;

   public Environment() {
      _ipSets = new HashMap<String, Set<Ip>>();
   }

   public Environment copy() {
      Environment copy = new Environment();
      copy._configurations = _configurations;
      copy._node = _node;
      copy._interface = _interface;
      copy._ipSets = _ipSets;
      return copy;
   }

   public BgpNeighbor getBgpNeighbor() {
      return _bgpNeighbor;
   }

   public Interface getInterface() {
      return _interface;
   }

   public Map<String, Set<Ip>> getIpSets() {
      return _ipSets;
   }

   public Configuration getNode() {
      return _node;
   }

   public Set<Configuration> getNodes() {
      Set<Configuration> nodes = new TreeSet<Configuration>();
      nodes.addAll(_configurations.values());
      return nodes;
   }

   public void setBgpNeighbor(BgpNeighbor bgpNeighbor) {
      _bgpNeighbor = bgpNeighbor;
   }

   public void setConfigurations(Map<String, Configuration> configurations) {
      _configurations = configurations;
   }

   public void setInterface(Interface iface) {
      _interface = iface;
   }

   public void setNode(Configuration node) {
      _node = node;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("{ ");
      sb.append("node:" + _node + " ");
      sb.append("interface:" + _interface + " ");
      sb.append("bgp_neighbor:" + _bgpNeighbor + " ");
      sb.append("}");
      return sb.toString();
   }

}
