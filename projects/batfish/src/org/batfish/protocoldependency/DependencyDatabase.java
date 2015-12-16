package org.batfish.protocoldependency;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.batfish.common.BatfishException;
import org.batfish.common.Pair;
import org.batfish.representation.Configuration;
import org.batfish.representation.Ip;
import org.batfish.representation.Prefix;
import org.batfish.representation.RoutingProtocol;

public class DependencyDatabase {

   private class NodePrefixProtocol extends Pair<String, PrefixProtocol> {

      /**
       *
       */
      private static final long serialVersionUID = 1L;

      public NodePrefixProtocol(String node, Prefix prefix,
            RoutingProtocol protocol) {
         super(node, new PrefixProtocol(prefix, protocol));
      }

   }

   private class NodeProtocol extends Pair<String, RoutingProtocol> {

      /**
       *
       */
      private static final long serialVersionUID = 1L;

      public NodeProtocol(String t1, RoutingProtocol t2) {
         super(t1, t2);
      }

   }

   private class PrefixProtocol extends Pair<Prefix, RoutingProtocol> {

      /**
       *
       */
      private static final long serialVersionUID = 1L;

      public PrefixProtocol(Prefix t1, RoutingProtocol t2) {
         super(t1, t2);
      }

   }

   private final Set<DependentRoute> _dependentRoutes;

   private final Map<String, Set<DependentRoute>> _dependentRoutesByNode;

   private final Map<NodePrefixProtocol, Set<DependentRoute>> _dependentRoutesByNodePrefixProtocol;

   private final Map<NodeProtocol, Set<DependentRoute>> _dependentRoutesByNodeProtocol;

   private final Map<Prefix, Set<DependentRoute>> _dependentRoutesByPrefix;

   private final Map<RoutingProtocol, Set<DependentRoute>> _dependentRoutesByProtocol;

   private final Map<RoutingProtocol, Set<PotentialExport>> _potentialExportsByProtocol;

   private final Map<RoutingProtocol, Set<PotentialImport>> _potentialImportsByProtocol;

   private final Map<RoutingProtocol, Set<ProtocolDependency>> _protocolDependencies;

   private final Map<RoutingProtocol, Map<RoutingProtocol, Set<Integer>>> _protocolDependencyMap;

   private final Map<String, RoutingTable> _routingTables;

   public DependencyDatabase(Map<String, Configuration> configurations) {
      _dependentRoutes = new TreeSet<DependentRoute>();
      _dependentRoutesByNode = new TreeMap<String, Set<DependentRoute>>();
      _dependentRoutesByNodePrefixProtocol = new TreeMap<NodePrefixProtocol, Set<DependentRoute>>();
      _dependentRoutesByNodeProtocol = new TreeMap<NodeProtocol, Set<DependentRoute>>();
      _dependentRoutesByPrefix = new TreeMap<Prefix, Set<DependentRoute>>();
      _dependentRoutesByProtocol = new TreeMap<RoutingProtocol, Set<DependentRoute>>();
      _potentialExportsByProtocol = new TreeMap<RoutingProtocol, Set<PotentialExport>>();
      _potentialImportsByProtocol = new TreeMap<RoutingProtocol, Set<PotentialImport>>();
      _protocolDependencies = new TreeMap<RoutingProtocol, Set<ProtocolDependency>>();
      _protocolDependencyMap = new TreeMap<RoutingProtocol, Map<RoutingProtocol, Set<Integer>>>();
      _routingTables = new TreeMap<String, RoutingTable>();
      for (Configuration c : configurations.values()) {
         String node = c.getHostname();
         _dependentRoutesByNode.put(node, new TreeSet<DependentRoute>());
         _routingTables.put(node, new RoutingTable());
         for (RoutingProtocol protocol : RoutingProtocol.values()) {
            NodeProtocol np = new NodeProtocol(node, protocol);
            _dependentRoutesByNodeProtocol.put(np,
                  new TreeSet<DependentRoute>());
         }
      }
      for (RoutingProtocol protocol : RoutingProtocol.values()) {
         _dependentRoutesByProtocol
               .put(protocol, new TreeSet<DependentRoute>());
         _potentialExportsByProtocol.put(protocol,
               new TreeSet<PotentialExport>());
         _potentialImportsByProtocol.put(protocol,
               new TreeSet<PotentialImport>());
      }
   }

   public void addDependentRoute(DependentRoute dependentRoute) {
      _dependentRoutes.add(dependentRoute);
      addDependentRouteByNode(dependentRoute);
      addDependentRouteByNodePrefixProtocol(dependentRoute);
      addDependentRouteByNodeProtocol(dependentRoute);
      addDependentRouteByPrefix(dependentRoute);
      addDependentRouteByProtocol(dependentRoute);
      addDependentRouteToRoutingTableByNode(dependentRoute);
   }

   private void addDependentRouteByNode(DependentRoute dependentRoute) {
      _dependentRoutesByNode.get(dependentRoute.getNode()).add(dependentRoute);
   }

   private void addDependentRouteByNodePrefixProtocol(
         DependentRoute dependentRoute) {
      NodePrefixProtocol npr = new NodePrefixProtocol(dependentRoute.getNode(),
            dependentRoute.getPrefix(), dependentRoute.getProtocol());
      Set<DependentRoute> dependentRoutes = _dependentRoutesByNodePrefixProtocol
            .get(npr);
      if (dependentRoutes == null) {
         dependentRoutes = new TreeSet<DependentRoute>();
         _dependentRoutesByNodePrefixProtocol.put(npr, dependentRoutes);
      }
      dependentRoutes.add(dependentRoute);
   }

   private void addDependentRouteByNodeProtocol(DependentRoute dependentRoute) {
      NodeProtocol np = new NodeProtocol(dependentRoute.getNode(),
            dependentRoute.getProtocol());
      _dependentRoutesByNodeProtocol.get(np).add(dependentRoute);
   }

   private void addDependentRouteByPrefix(DependentRoute dependentRoute) {
      Prefix prefix = dependentRoute.getPrefix();
      Set<DependentRoute> dependentRoutes = _dependentRoutesByPrefix
            .get(prefix);
      if (dependentRoutes == null) {
         dependentRoutes = new TreeSet<DependentRoute>();
         _dependentRoutesByPrefix.put(prefix, dependentRoutes);
      }
      dependentRoutes.add(dependentRoute);
   }

   private void addDependentRouteByProtocol(DependentRoute dependentRoute) {
      _dependentRoutesByProtocol.get(dependentRoute.getProtocol()).add(
            dependentRoute);
   }

   private void addDependentRouteToRoutingTableByNode(
         DependentRoute dependentRoute) {
      String node = dependentRoute.getNode();
      _routingTables.get(node).addDependentRoute(dependentRoute);
   }

   public void addPotentialExport(PotentialExport potentialExport) {
      getPotentialExports(potentialExport.getProtocol()).add(potentialExport);
   }

   public void addPotentialImport(PotentialImport potentialImport) {
      _potentialImportsByProtocol.get(potentialImport.getProtocol()).add(
            potentialImport);
   }

   public void calculateDependencies() {
      _protocolDependencies.clear();
      for (Entry<RoutingProtocol, Set<DependentRoute>> e : _dependentRoutesByProtocol
            .entrySet()) {
         RoutingProtocol protocol = e.getKey();
         Set<DependentRoute> routes = e.getValue();
         if (!routes.isEmpty()) {
            Set<ProtocolDependency> protocolDependencies = new TreeSet<ProtocolDependency>();
            _protocolDependencies.put(protocol, protocolDependencies);
            for (DependentRoute route : routes) {
               Set<ProtocolDependency> routeProtocolDependencies = route
                     .getProtocolDependencies();
               protocolDependencies.addAll(routeProtocolDependencies);
            }
            Set<ProtocolDependency> trivialDependencies = new LinkedHashSet<ProtocolDependency>();
            for (ProtocolDependency protocolDependency : protocolDependencies) {
               if (protocolDependency.getIndirectionLevel().equals(0)) {
                  trivialDependencies.add(protocolDependency);
               }
            }
            protocolDependencies.removeAll(trivialDependencies);
         }
      }
      for (Entry<RoutingProtocol, Set<ProtocolDependency>> e : _protocolDependencies
            .entrySet()) {
         RoutingProtocol dependentProtocol = e.getKey();
         Set<ProtocolDependency> dependencies = e.getValue();
         Map<RoutingProtocol, Set<Integer>> compactDependencies = new TreeMap<RoutingProtocol, Set<Integer>>();
         _protocolDependencyMap.put(dependentProtocol, compactDependencies);
         for (ProtocolDependency dependency : dependencies) {
            RoutingProtocol dependencyProtocol = dependency.getProtocol();
            Set<Integer> indirectionLevels = compactDependencies
                  .get(dependencyProtocol);
            if (indirectionLevels == null) {
               indirectionLevels = new TreeSet<Integer>();
               compactDependencies.put(dependencyProtocol, indirectionLevels);
            }
            int indirectionLevel = dependency.getIndirectionLevel();
            indirectionLevels.add(indirectionLevel);
         }
      }
   }

   public void clearPotentialExports() {
      for (Set<PotentialExport> potentialExports : _potentialExportsByProtocol
            .values()) {
         potentialExports.clear();
      }
   }

   public void clearPotentialImports() {
      for (Set<PotentialImport> potentialImports : _potentialImportsByProtocol
            .values()) {
         potentialImports.clear();
      }
   }

   public Map<RoutingProtocol, Map<RoutingProtocol, Set<Integer>>> getCompactProtocolDependencies() {
      return _protocolDependencyMap;
   }

   public DependentRoute getConnectedRoute(String node, Prefix prefix) {
      Set<DependentRoute> dependentRoutes = _dependentRoutesByNodePrefixProtocol
            .get(new NodePrefixProtocol(node, prefix, RoutingProtocol.CONNECTED));
      if (dependentRoutes.size() != 1) {
         throw new BatfishException("expected only 1 connected route");
      }
      for (DependentRoute dependentRoute : dependentRoutes) {
         return dependentRoute;
      }
      return null;
   }

   public Set<DependentRoute> getDependentRoutes() {
      return _dependentRoutes;
   }

   public Set<DependentRoute> getDependentRoutes(String node) {
      return _dependentRoutesByNode.get(node);
   }

   public Set<DependentRoute> getDependentRoutes(String node, Prefix prefix,
         RoutingProtocol protocol) {
      return _dependentRoutesByNodePrefixProtocol.get(new NodePrefixProtocol(
            node, prefix, protocol));
   }

   public Set<DependentRoute> getDependentRoutes(String node,
         RoutingProtocol protocol) {
      return _dependentRoutesByNodeProtocol
            .get(new NodeProtocol(node, protocol));
   }

   public Set<DependentRoute> getLongestPrefixMatch(String node, Ip address) {
      return _routingTables.get(node).longestPrefixMatch(address);
   }

   public Set<PotentialExport> getPotentialExports(RoutingProtocol protocol) {
      return _potentialExportsByProtocol.get(protocol);
   }

   public Set<PotentialExport> getPotentialExports(String node,
         RoutingProtocol protocol) {
      Set<PotentialExport> potentialExports = new TreeSet<PotentialExport>();
      Set<PotentialExport> potentialExportsByProtocol = _potentialExportsByProtocol
            .get(protocol);
      for (PotentialExport potentialExport : potentialExportsByProtocol) {
         if (potentialExport.getNode().equals(node)) {
            potentialExports.add(potentialExport);
         }
      }
      return potentialExports;
   }

   public Set<PotentialImport> getPotentialImports(RoutingProtocol protocol) {
      return _potentialImportsByProtocol.get(protocol);
   }

   public Set<PotentialImport> getPotentialImports(String node,
         RoutingProtocol protocol) {
      Set<PotentialImport> potentialImports = new TreeSet<PotentialImport>();
      Set<PotentialImport> potentialImportsByProtocol = _potentialImportsByProtocol
            .get(protocol);
      for (PotentialImport potentialImport : potentialImportsByProtocol) {
         if (potentialImport.getNode().equals(node)) {
            potentialImports.add(potentialImport);
         }
      }
      return potentialImports;
   }

   public Map<RoutingProtocol, Set<ProtocolDependency>> getProtocolDependencies() {
      return _protocolDependencies;
   }

   public void removeCycles() {
      for (DependentRoute dependentRoute : _dependentRoutes) {
         Set<DependentRoute> dependentClosure = dependentRoute
               .getDependentClosure();
         if (dependentClosure.contains(dependentRoute)) {
            assert Boolean.TRUE;
         }
      }
   }

}
