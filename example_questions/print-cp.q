verify {
   foreach node {
      printf("Node: %s\n", node.name); 
      printf("\tProtocols present:\n");
      if (node.bgp.configured) then {
         printf("\t\tBGP\n"); 
      }
      if (node.ospf.configured) then {
         printf("\t\tOSPF\n"); 
      }
      if (node.isis.configured) then {
         printf("\t\tIS-IS\n"); 
      }
      if (node.static.configured) then {
         printf("\t\tStatic\n"); 
      }
      printf("\tInterfaces:\n");
      foreach interface {
         printf("\t\tInterface: %s\n", interface.name);
         if (interface.has_ip) then {
            printf("\t\t\tPrefix: %s\n", interface.prefix);
         }
      }
      if (node.bgp.configured) then {
         printf("\tBGP peerings:\n"); 
      }
      foreach bgp_neighbor {
         printf("\t\tNeighbor IP: %s\n", bgp_neighbor.remote_ip);
         printf("\t\t\tRemote AS: %s\n", bgp_neighbor.remote_as);
         printf("\t\t\tLocal IP: %s\n", bgp_neighbor.local_ip);
         printf("\t\t\tLocal AS: %s\n", bgp_neighbor.local_as);
         if (bgp_neighbor.has_generated_route) then {
            printf("\t\t\tAggregate/Generated routes:\n");
         }
         foreach generated_route {
            printf("\t\t\t\tPrefix: %s\n", generated_route.prefix);
         }
      }
      if (node.has_generated_route) then {
         printf("\tGlobal Aggregate/Generated routes:\n");
      }
      foreach generated_route {
         printf("\t\tPrefix: %s\n", generated_route.prefix);
      }
      if (node.bgp.has_generated_route) then {
         printf("\tBGP Aggregate/Generated routes:\n");
      }
      foreach node.bgp.generated_route {
         printf("\t\tPrefix: %s\n", generated_route.prefix);
      }
      if (node.static.configured) then {
         printf("\tStatic Routes:\n");
      }
      foreach static_route {
         printf("\t\t%s:\n", static_route.prefix);
         if (static_route.has_next_hop_ip) then {
            printf("\t\t\tNext hop IP: %s\n", static_route.next_hop_ip);
         }
         if (static_route.has_next_hop_interface) then {
            printf("\t\t\tNext hop interface: %s\n", static_route.next_hop_interface);
         }
         printf("\t\t\tAdministrative Cost: %s\n", static_route.administrative_cost);
      }
   }
}
