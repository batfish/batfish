verify {
   $ebgpnodes.clear_strings;
   foreach node {
      foreach bgp_neighbor {
         if (bgp_neighbor.remote_as != bgp_neighbor.local_as) then {
            $prevsize := $ebgpnodes.num_strings;
            $ebgpnodes.add_string(node.name);
            if ($prevsize != $ebgpnodes.num_strings) then {
               // we just added a new ebgpnode
               printf("%s\n", node.name);
            }
         }
      }
   }
}
