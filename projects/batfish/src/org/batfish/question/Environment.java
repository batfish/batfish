package org.batfish.question;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.batfish.representation.BgpNeighbor;
import org.batfish.representation.Configuration;
import org.batfish.representation.GeneratedRoute;
import org.batfish.representation.Interface;
import org.batfish.representation.Ip;
import org.batfish.representation.StaticRoute;

public class Environment {

   private boolean[] _assertions;

   private BgpNeighbor _bgpNeighbor;

   private Map<String, Configuration> _configurations;

   private GeneratedRoute _generatedRoute;

   private Map<String, Integer> _integers;

   private Interface _interface;

   private Map<String, Set<Ip>> _ipSets;

   private Configuration _node;

   private StaticRoute _staticRoute;

   private Map<String, Set<String>> _stringSets;

   private boolean[] _unsafe;

   public Environment() {
      _assertions = new boolean[1];
      _integers = new HashMap<String, Integer>();
      _ipSets = new HashMap<String, Set<Ip>>();
      _stringSets = new HashMap<String, Set<String>>();
      _unsafe = new boolean[1];
   }

   public Environment copy() {
      Environment copy = new Environment();
      copy._assertions = _assertions;
      copy._configurations = _configurations;
      copy._generatedRoute = _generatedRoute;
      copy._node = _node;
      copy._integers = _integers;
      copy._interface = _interface;
      copy._ipSets = _ipSets;
      copy._staticRoute = _staticRoute;
      copy._stringSets = _stringSets;
      copy._unsafe = _unsafe;
      return copy;
   }

   public boolean getAssertions() {
      return _assertions[0];
   }

   public BgpNeighbor getBgpNeighbor() {
      return _bgpNeighbor;
   }

   public GeneratedRoute getGeneratedRoute() {
      return _generatedRoute;
   }

   public Map<String, Integer> getIntegers() {
      return _integers;
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

   public StaticRoute getStaticRoute() {
      return _staticRoute;
   }

   public Map<String, Set<String>> getStringSets() {
      return _stringSets;
   }

   public boolean getUnsafe() {
      return _unsafe[0];
   }

   public void setAssertions(boolean b) {
      _assertions[0] = b;
   }

   public void setBgpNeighbor(BgpNeighbor bgpNeighbor) {
      _bgpNeighbor = bgpNeighbor;
   }

   public void setConfigurations(Map<String, Configuration> configurations) {
      _configurations = configurations;
   }

   public void setGeneratedRoute(GeneratedRoute generatedRoute) {
      _generatedRoute = generatedRoute;
   }

   public void setInterface(Interface iface) {
      _interface = iface;
   }

   public void setNode(Configuration node) {
      _node = node;
   }

   public void setStaticRoute(StaticRoute staticRoute) {
      _staticRoute = staticRoute;
   }

   public void setUnsafe(boolean b) {
      _unsafe[0] = b;
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
