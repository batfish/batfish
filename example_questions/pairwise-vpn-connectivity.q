verify {
   $vpn_nodes:set<node>; 
   $current_vpn_neighbors:set<node>; 
   foreach node {
      foreach ipsec_vpn {
         $vpn_nodes.add(node);
      }
   }
   foreach node {
      $current_vpn_neighbors.clear;
      if ($vpn_nodes.contains(node)) {
         foreach ipsec_vpn {
            if (ipsec_vpn.has_remote_ipsec_vpn) {
               foreach remote_ipsec_vpn {
                  $current_vpn_neighbors.add(remote_ipsec_vpn.owner);
               }
            }
         }
         foreach $vpn_node : $vpn_nodes {
            if (node != $vpn_node) {
               assert {
                  $current_vpn_neighbors.contains($vpn_node)
               }
               onfailure {
                  printf("VPN node '%s' lacks connectivity with VPN node '%s'\n",
                     node.name,
                     $vpn_node.name);
               }
            }
         }
      }
   }
}
