/**
 * (NOT INTENDED FOR END-USERS) Print certain details of various aspects of the control plane for the provided configurations 
 */
verify {
   foreach node {
      printf("Node: %s\n", node.name); 
      printf("\tProtocols present:\n");
      if (node.bgp.configured) {
         printf("\t\tBGP\n"); 
      }
      if (node.ospf.configured) {
         printf("\t\tOSPF\n"); 
      }
      if (node.isis.configured) {
         printf("\t\tIS-IS\n"); 
      }
      if (node.static.configured) {
         printf("\t\tStatic\n"); 
      }
      printf("\tInterfaces:\n");
      foreach interface {
         printf("\t\tInterface: %s\n", interface.name);
         if (interface.has_ip) {
            printf("\t\t\tPrefix: %s\n", interface.prefix);
         }
      }
      if (node.bgp.configured) {
         printf("\tBGP peerings:\n"); 
      }
      foreach bgp_neighbor {
         printf("\t\tNeighbor IP: %s\n", bgp_neighbor.remote_ip);
         printf("\t\t\tRemote AS: %s\n", bgp_neighbor.remote_as);
         printf("\t\t\tLocal IP: %s\n", bgp_neighbor.local_ip);
         printf("\t\t\tLocal AS: %s\n", bgp_neighbor.local_as);
         if (bgp_neighbor.has_generated_route) {
            printf("\t\t\tAggregate/Generated routes:\n");
         }
         foreach generated_route {
            printf("\t\t\t\tPrefix: %s\n", generated_route.prefix);
         }
      }
      if (node.has_generated_route) {
         printf("\tGlobal Aggregate/Generated routes:\n");
      }
      foreach generated_route {
         printf("\t\tPrefix: %s\n", generated_route.prefix);
      }
      if (node.bgp.has_generated_route) {
         printf("\tBGP Aggregate/Generated routes:\n");
      }
      foreach node_bgp_generated_route {
         printf("\t\tPrefix: %s\n", generated_route.prefix);
      }
      if (node.static.configured) {
         printf("\tStatic Routes:\n");
      }
      foreach static_route {
         $static_route_prefix := static_route.prefix;
         printf("\t\t%s:\n", $static_route_prefix);
         if (static_route.has_next_hop_ip) {
            $static_route_next_hop_ip := static_route.next_hop_ip;
            printf("\t\t\tNext hop IP: %s\n", $static_route_next_hop_ip);
         }
         if (static_route.has_next_hop_interface) {
            printf("\t\t\tNext hop interface: %s\n", static_route.next_hop_interface);
         }
         $static_route_administrative_cost := static_route.administrative_cost;
         printf("\t\t\tAdministrative Cost: %s\n", $static_route_administrative_cost);
      }
   }
}
