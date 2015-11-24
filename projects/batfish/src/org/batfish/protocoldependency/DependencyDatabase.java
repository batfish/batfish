package org.batfish.protocoldependency;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.batfish.common.BatfishException;
import org.batfish.common.Pair;
import org.batfish.representation.Configuration;
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

   private class PrefixProtocol extends Pair<Prefix, RoutingProtocol> {

      /**
       *
       */
      private static final long serialVersionUID = 1L;

      public PrefixProtocol(Prefix t1, RoutingProtocol t2) {
         super(t1, t2);
      }

   }

   private final Map<String, Set<DependentRoute>> _dependentRoutesByNode;

   private final Map<NodePrefixProtocol, Set<DependentRoute>> _dependentRoutesByNodePrefixProtocol;

   private final Map<Prefix, Set<DependentRoute>> _dependentRoutesByPrefix;

   private final Map<RoutingProtocol, Set<DependentRoute>> _dependentRoutesByProtocol;

   private final Map<RoutingProtocol, Set<PotentialExport>> _potentialExportsByProtocol;

   private final Map<RoutingProtocol, Set<PotentialImport>> _potentialImportsByProtocol;

   public DependencyDatabase(Map<String, Configuration> configurations) {
      _dependentRoutesByNode = new TreeMap<String, Set<DependentRoute>>();
      _dependentRoutesByNodePrefixProtocol = new TreeMap<NodePrefixProtocol, Set<DependentRoute>>();
      _dependentRoutesByPrefix = new TreeMap<Prefix, Set<DependentRoute>>();
      _dependentRoutesByProtocol = new TreeMap<RoutingProtocol, Set<DependentRoute>>();
      _potentialExportsByProtocol = new TreeMap<RoutingProtocol, Set<PotentialExport>>();
      _potentialImportsByProtocol = new TreeMap<RoutingProtocol, Set<PotentialImport>>();
      for (Configuration c : configurations.values()) {
         String name = c.getHostname();
         _dependentRoutesByNode.put(name, new TreeSet<DependentRoute>());
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
      addDependentRouteByNode(dependentRoute);
      addDependentRouteByPrefix(dependentRoute);
      addDependentRouteByProtocol(dependentRoute);
      addDependentRouteByNodePrefixProtocol(dependentRoute);
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

   public void addPotentialExport(PotentialExport potentialExport) {
      getPotentialExports(potentialExport.getProtocol()).add(potentialExport);
   }

   public void addPotentialImport(PotentialImport potentialImport) {
      _potentialImportsByProtocol.get(potentialImport.getProtocol()).add(potentialImport);
   }

   public void clearPotentialExports() {
      _potentialExportsByProtocol.clear();
   }

   public void clearPotentialImports() {
      _potentialImportsByProtocol.clear();
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

   public Set<DependentRoute> getDependentRoutes(String node) {
      return _dependentRoutesByNode.get(node);
   }

   public Set<DependentRoute> getDependentRoutes(String node, Prefix prefix,
         RoutingProtocol protocol) {
      return _dependentRoutesByNodePrefixProtocol.get(new NodePrefixProtocol(
            node, prefix, protocol));
   }

   public Set<PotentialExport> getPotentialExports(RoutingProtocol protocol) {
      return _potentialExportsByProtocol.get(protocol);
   }

   public Set<PotentialExport> getPotentialExports(String node, RoutingProtocol protocol) {
      Set<PotentialExport> potentialExports = new TreeSet<PotentialExport>();
      Set<PotentialExport> potentialExportsByProtocol = _potentialExportsByProtocol.get(protocol);
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

}
