/**
   Print bgp advertisements, computing data plane if necessary
* @param show_ebgp Show EBGP advertisements
* @param show_ibgp Show IBGP advertisements
* @param show_received Show received advertisements
* @param show_sent Show sent advertisements
*/
defaults {
   $show_ebgp=true;
   $show_ibgp=true;
   $show_received=true;
   $show_sent=true;
}
verify {
   foreach node {
      printf("NODE: %s\n",
         node.name);
      if (and{$show_ebgp, $show_received}) {
         printf("\tRECEIVED EBGP ADVERTISEMENTS\n");
         foreach received_ebgp_advertisement {
            $src_node := received_ebgp_advertisement.src_node;
            $src_ip := received_ebgp_advertisement.src_ip;
            $dst_node := received_ebgp_advertisement.dst_node;
            $dst_ip := received_ebgp_advertisement.dst_ip;
            $network := received_ebgp_advertisement.network;
            $next_hop_ip  := received_ebgp_advertisement.next_hop_ip;
            $med  := received_ebgp_advertisement.med;
            $local_preference  := received_ebgp_advertisement.local_preference;
            $as_path  := received_ebgp_advertisement.as_path;
            $communities := received_ebgp_advertisement.communities;
            printf("\t\tSource Node: %s\n\t\tSource IP: %s\n\t\tDestination Node: %s\n\t\tDestination IP: %s\n\t\tNetwork: %s\n\t\tNext Hop IP: %s\n\t\tMED: %s\n\t\tLocal Preference: %s\n\t\tPath: %s\n\t\t",
               $src_node,
               $src_ip,
               $dst_node,
               $dst_ip,
               $network,
               $next_hop_ip,
               $med,
               $local_preference,
               $as_path);
            printf("Communities:\n");
            foreach $community : $communities {
               printf("\t\t\t%s\n",
                  $community);
            }
         }
      }
      if (and{$show_ibgp, $show_received}) {
         printf("\tRECEIVED IBGP ADVERTISEMENTS\n");
         foreach received_ibgp_advertisement {
            $src_node := received_ibgp_advertisement.src_node;
            $src_ip := received_ibgp_advertisement.src_ip;
            $dst_node := received_ibgp_advertisement.dst_node;
            $dst_ip := received_ibgp_advertisement.dst_ip;
            $network := received_ibgp_advertisement.network;
            $next_hop_ip  := received_ibgp_advertisement.next_hop_ip;
            $med  := received_ibgp_advertisement.med;
            $local_preference  := received_ibgp_advertisement.local_preference;
            $as_path  := received_ibgp_advertisement.as_path;
            $communities := received_ibgp_advertisement.communities;
            printf("\t\tSource Node: %s\n\t\tSource IP: %s\n\t\tDestination Node: %s\n\t\tDestination IP: %s\n\t\tNetwork: %s\n\t\tNext Hop IP: %s\n\t\tMED: %s\n\t\tLocal Preference: %s\n\t\tPath: %s\n\t\t",
               $src_node,
               $src_ip,
               $dst_node,
               $dst_ip,
               $network,
               $next_hop_ip,
               $med,
               $local_preference,
               $as_path);
            printf("Communities:\n");
            foreach $community : $communities {
               printf("\t\t\t%s\n",
                  $community);
            }
         }
      }
      if (and{$show_ebgp, $show_sent}) {
         printf("\tSENT EBGP ADVERTISEMENTS\n");
         foreach sent_ebgp_advertisement {
            $src_node := sent_ebgp_advertisement.src_node;
            $src_ip := sent_ebgp_advertisement.src_ip;
            $dst_node := sent_ebgp_advertisement.dst_node;
            $dst_ip := sent_ebgp_advertisement.dst_ip;
            $network := sent_ebgp_advertisement.network;
            $next_hop_ip  := sent_ebgp_advertisement.next_hop_ip;
            $med  := sent_ebgp_advertisement.med;
            $local_preference  := sent_ebgp_advertisement.local_preference;
            $as_path  := sent_ebgp_advertisement.as_path;
            $communities := sent_ebgp_advertisement.communities;
            printf("\t\tSource Node: %s\n\t\tSource IP: %s\n\t\tDestination Node: %s\n\t\tDestination IP: %s\n\t\tNetwork: %s\n\t\tNext Hop IP: %s\n\t\tMED: %s\n\t\tLocal Preference: %s\n\t\tPath: %s\n\t\t",
               $src_node,
               $src_ip,
               $dst_node,
               $dst_ip,
               $network,
               $next_hop_ip,
               $med,
               $local_preference,
               $as_path);
            printf("Communities:\n");
            foreach $community : $communities {
               printf("\t\t\t%s\n",
                  $community);
            }
         }
      }
      if (and{$show_ibgp, $show_sent}) {
         printf("\tSENT IBGP ADVERTISEMENTS\n");
         foreach sent_ibgp_advertisement {
            $src_node := sent_ibgp_advertisement.src_node;
            $src_ip := sent_ibgp_advertisement.src_ip;
            $dst_node := sent_ibgp_advertisement.dst_node;
            $dst_ip := sent_ibgp_advertisement.dst_ip;
            $network := sent_ibgp_advertisement.network;
            $next_hop_ip  := sent_ibgp_advertisement.next_hop_ip;
            $med  := sent_ibgp_advertisement.med;
            $local_preference  := sent_ibgp_advertisement.local_preference;
            $as_path  := sent_ibgp_advertisement.as_path;
            $communities := sent_ibgp_advertisement.communities;
            printf("\t\tSource Node: %s\n\t\tSource IP: %s\n\t\tDestination Node: %s\n\t\tDestination IP: %s\n\t\tNetwork: %s\n\t\tNext Hop IP: %s\n\t\tMED: %s\n\t\tLocal Preference: %s\n\t\tPath: %s\n\t\t",
               $src_node,
               $src_ip,
               $dst_node,
               $dst_ip,
               $network,
               $next_hop_ip,
               $med,
               $local_preference,
               $as_path);
            printf("Communities:\n");
            foreach $community : $communities {
               printf("\t\t\t%s\n",
                  $community);
            }
         }
      }
      printf("\n");
   }
}