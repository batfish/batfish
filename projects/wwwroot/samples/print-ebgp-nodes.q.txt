verify {
   $ebgpnodes:set<string>;
   foreach node {
      foreach bgp_neighbor {
         if (bgp_neighbor.remote_as != bgp_neighbor.local_as) then {
            $prevsize := $ebgpnodes.size;
            $ebgpnodes.add(node.name);
            if ($prevsize != $ebgpnodes.size) then {
               // we just added a new ebgpnode
               printf("%s\n", node.name);
            }
         }
      }
   }
}
