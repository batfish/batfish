verify {
   $ibgpnodes.clear_strings;
   foreach node {
      foreach bgp_neighbor {
         if (bgp_neighbor.remote_as == bgp_neighbor.local_as) then {
            $prevsize := $ibgpnodes.num_strings;
            $ibgpnodes.add_string(node.name);
            if ($prevsize != $ibgpnodes.num_strings) then {
               // we just added a new ibgpnode
               printf("%s\n", node.name);
            }
         }
      }
   }
}
