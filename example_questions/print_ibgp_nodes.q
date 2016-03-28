/**
 * Print the list of nodes containing configuration for at least one iBGP neighbor
 */
verify {
   $ibgpnodes:set<string>;
   foreach node {
      foreach bgp_neighbor {
         if (bgp_neighbor.remote_as == bgp_neighbor.local_as) then {
            $prevsize := $ibgpnodes.size;
            $ibgpnodes.add(node.name);
            if ($prevsize != $ibgpnodes.size) then {
               // we just added a new ibgpnode
               printf("%s\n", node.name);
            }
         }
      }
   }
}
