/**
   Print routing tables, computing data plane if necessary
*/
verify {
   foreach node {
      foreach route {
         $node := node.name;
         $network := route.network;
         $next_hop := route.next_hop;
         $next_hop_ip := route.next_hop_ip;
         $next_hop_interface := route.next_hop_interface;
         $administrative_cost := route.administrative_cost;
         $cost := route.cost;
         $protocol := route.protocol;
         $tag := route.tag;
         printf("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n",
            $node,
            $network,
            $next_hop,
            $next_hop_ip,
            $next_hop_interface,
            $administrative_cost,
            $cost,
            $protocol,
            $tag);
      }
   }
}