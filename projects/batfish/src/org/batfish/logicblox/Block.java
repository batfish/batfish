package org.batfish.logicblox;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class Block implements Comparable<Block> {

   public static final Map<String, Block> BLOCKS = initBlocks();

   private static Map<String, Block> initBlocks() {
      Map<String, Block> blocks = new HashMap<String, Block>();

      // DO NOT PUT BLOCKS EVERYTHING DEPENDS ON

      Block AsPath = new Block("AsPath");
      blocks.put("AsPath", AsPath);
      Block Bgp_ebgp_bgp_origination = new Block("Bgp_ebgp_bgp_origination");
      blocks.put("Bgp_ebgp_bgp_origination", Bgp_ebgp_bgp_origination);
      Block Bgp_ebgp_igp_origination = new Block("Bgp_ebgp_igp_origination");
      blocks.put("Bgp_ebgp_igp_origination", Bgp_ebgp_igp_origination);
      Block Bgp_ibgp_igp_origination = new Block("Bgp_ibgp_igp_origination");
      blocks.put("Bgp_ibgp_igp_origination", Bgp_ibgp_igp_origination);
      Block Bgp_igp_origination = new Block("Bgp_igp_origination");
      blocks.put("Bgp_igp_origination", Bgp_igp_origination);
      Block Bgp_generation = new Block("Bgp_generation");
      blocks.put("Bgp_generation", Bgp_generation);
      Block Bgp_ibgp_neighbors = new Block("Bgp_ibgp_neighbors");
      blocks.put("Bgp_ibgp_neighbors", Bgp_ibgp_neighbors);
      Block Bgp_ibgp_ebgp_origination = new Block("Bgp_ibgp_ebgp_origination");
      blocks.put("Bgp_ibgp_ebgp_origination", Bgp_ibgp_ebgp_origination);
      Block Bgp_incoming_transformation = new Block(
            "Bgp_incoming_transformation");
      blocks.put("Bgp_incoming_transformation", Bgp_incoming_transformation);
      Block Bgp_outgoing_transformation = new Block(
            "Bgp_outgoing_transformation");
      blocks.put("Bgp_outgoing_transformation", Bgp_outgoing_transformation);
      Block Bgp_ebgp_incoming_transformation = new Block(
            "Bgp_ebgp_incoming_transformation");
      blocks.put("Bgp_ebgp_incoming_transformation",
            Bgp_ebgp_incoming_transformation);
      Block Bgp_ebgp_outgoing_transformation = new Block(
            "Bgp_ebgp_outgoing_transformation");
      blocks.put("Bgp_ebgp_outgoing_transformation",
            Bgp_ebgp_outgoing_transformation);
      Block Bgp_ibgp_incoming_transformation = new Block(
            "Bgp_ibgp_incoming_transformation");
      blocks.put("Bgp_ibgp_incoming_transformation",
            Bgp_ibgp_incoming_transformation);
      Block Bgp_ibgp_outgoing_transformation = new Block(
            "Bgp_ibgp_outgoing_transformation");
      blocks.put("Bgp_ibgp_outgoing_transformation",
            Bgp_ibgp_outgoing_transformation);
      Block Bgp_route_reflection = new Block("Bgp_route_reflection");
      blocks.put("Bgp_route_reflection", Bgp_route_reflection);
      Block Bgp = new Block("Bgp");
      blocks.put("Bgp", Bgp);
      Block DataPlane = new Block("DataPlane");
      blocks.put("DataPlane", DataPlane);
      Block Flow = new Block("Flow");
      blocks.put("Flow", Flow);
      Block GeneratedRoute = new Block("GeneratedRoute");
      blocks.put("GeneratedRoute", GeneratedRoute);
      Block Interface = new Block("Interface");
      blocks.put("Interface", Interface);
      Block IpAccessList = new Block("IpAccessList");
      blocks.put("IpAccessList", IpAccessList);
      Block Isis_generation = new Block("Isis_generation");
      blocks.put("Isis_generation", Isis_generation);
      Block Isis_L1_redistribution = new Block("Isis_L1_redistribution");
      blocks.put("Isis_L1_redistribution", Isis_L1_redistribution);
      Block Isis_L1 = new Block("Isis_L1");
      blocks.put("Isis_L1", Isis_L1);
      Block Isis_L2_redistribution = new Block("Isis_L2_redistribution");
      blocks.put("Isis_L2_redistribution", Isis_L2_redistribution);
      Block Isis_L2 = new Block("Isis_L2");
      blocks.put("Isis_L2", Isis_L2);
      Block Isis_redistribution = new Block("Isis_redistribution");
      blocks.put("Isis_redistribution", Isis_redistribution);
      Block Isis = new Block("Isis");
      blocks.put("Isis", Isis);
      Block Ospf_e1 = new Block("Ospf_e1");
      blocks.put("Ospf_e1", Ospf_e1);
      Block Ospf_e2 = new Block("Ospf_e2");
      blocks.put("Ospf_e2", Ospf_e2);
      Block Ospf_external = new Block("Ospf_external");
      blocks.put("Ospf_external", Ospf_external);
      Block Ospf_generation = new Block("Ospf_generation");
      blocks.put("Ospf_generation", Ospf_generation);
      Block Ospf_inter_area = new Block("Ospf_inter_area");
      blocks.put("Ospf_inter_area", Ospf_inter_area);
      Block Ospf_intra_area = new Block("Ospf_intra_area");
      blocks.put("Ospf_intra_area", Ospf_intra_area);
      Block Ospf = new Block("Ospf");
      blocks.put("Ospf", Ospf);
      Block PolicyMap = new Block("PolicyMap");
      blocks.put("PolicyMap", PolicyMap);
      Block Precomputed = new Block("Precomputed");
      blocks.put("Precomputed", Precomputed);
      Block RouteFilter = new Block("RouteFilter");
      blocks.put("RouteFilter", RouteFilter);
      Block Route = new Block("Route");
      blocks.put("Route", Route);
      Block Static_interface = new Block("Static_interface");
      blocks.put("Static_interface", Static_interface);
      Block Static_recursive = new Block("Static_recursive");
      blocks.put("Static_recursive", Static_recursive);
      Block Traffic = new Block("Traffic");
      blocks.put("Traffic", Traffic);

      AsPath.addDependent(Bgp_ebgp_bgp_origination);
      AsPath.addDependent(Bgp_ibgp_ebgp_origination);
      AsPath.addDependent(Bgp_incoming_transformation);
      AsPath.addDependent(Bgp_outgoing_transformation);
      AsPath.addDependent(Bgp_route_reflection);

      Bgp.addDependent(Bgp_ebgp_bgp_origination);
      Bgp.addDependent(Bgp_igp_origination);
      Bgp.addDependent(Bgp_generation);
      Bgp.addDependent(Bgp_ibgp_neighbors);
      Bgp.addDependent(Bgp_ibgp_ebgp_origination);
      Bgp.addDependent(Bgp_incoming_transformation);
      Bgp.addDependent(Bgp_outgoing_transformation);
      Bgp.addDependent(Bgp_route_reflection);

      Bgp_igp_origination.addDependent(Bgp_ebgp_igp_origination);
      Bgp_igp_origination.addDependent(Bgp_ibgp_igp_origination);

      Bgp_incoming_transformation
            .addDependent(Bgp_ebgp_incoming_transformation);
      Bgp_incoming_transformation
            .addDependent(Bgp_ibgp_incoming_transformation);

      Bgp_outgoing_transformation
            .addDependent(Bgp_ebgp_outgoing_transformation);
      Bgp_outgoing_transformation
            .addDependent(Bgp_ibgp_outgoing_transformation);

      DataPlane.addDependent(Traffic);

      GeneratedRoute.addDependent(Bgp_generation);
      GeneratedRoute.addDependent(Isis_generation);
      GeneratedRoute.addDependent(Ospf_generation);

      Interface.addDependent(Route);
      Interface.addDependent(Static_interface);

      Isis.addDependent(Isis_generation);
      Isis.addDependent(Isis_L1);
      Isis.addDependent(Isis_L2);
      Isis.addDependent(Isis_redistribution);

      Isis_redistribution.addDependent(Isis_L1_redistribution);
      Isis_redistribution.addDependent(Isis_L2_redistribution);

      Ospf.addDependent(Ospf_external);
      Ospf.addDependent(Ospf_generation);
      Ospf.addDependent(Ospf_inter_area);
      Ospf.addDependent(Ospf_intra_area);

      Ospf_external.addDependent(Ospf_e1);
      Ospf_external.addDependent(Ospf_e2);

      PolicyMap.addDependent(Bgp);
      PolicyMap.addDependent(Isis_generation);
      PolicyMap.addDependent(Ospf_external);
      PolicyMap.addDependent(Ospf_generation);

      Route.addDependent(DataPlane);
      Route.addDependent(Static_recursive);

      RouteFilter.addDependent(PolicyMap);

      return blocks;
   }

   private final Set<Block> _dependencies;

   private final Set<Block> _dependents;

   private final String _name;

   public Block(String name) {
      _name = name;
      _dependencies = new LinkedHashSet<Block>();
      _dependents = new LinkedHashSet<Block>();
   }

   private void addDependent(Block dependent) {
      _dependents.add(dependent);
      dependent._dependencies.add(this);
   }

   @Override
   public int compareTo(Block block) {
      return _name.compareTo(block._name);
   }

   @Override
   public boolean equals(Object o) {
      return _name.equals(((Block) o)._name);
   }

   public Set<Block> getDependencies() {
      Set<Block> dependencies = new LinkedHashSet<Block>();
      for (Block dependency : _dependencies) {
         dependencies.addAll(dependency.getDependencies());
         dependencies.add(dependency);
      }
      return dependencies;
   }

   public Set<Block> getDependents() {
      Set<Block> dependents = new LinkedHashSet<Block>();
      for (Block dependent : _dependents) {
         dependents.addAll(dependent.getDependents());
         dependents.add(dependent);
      }
      return dependents;
   }

   public String getName() {
      return _name;
   }

   @Override
   public int hashCode() {
      return _name.hashCode();
   }

}
