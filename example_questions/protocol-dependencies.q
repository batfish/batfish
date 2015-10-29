verify {
   foreach  node {
      $ospf_route_filters:set<route_filter>;
      if (node.ospf.configured) then {
         foreach ospf_outbound_policy {
            foreach clause {
               foreach match_protocol {
                  foreach protocol {
                     printf("External OSPF on %s depends on protocol: %s\n", node.name, protocol.name);
                  }
               }
               foreach match_route_filter {
                  foreach route_filter {
                     $ospf_route_filters.add(route_filter);
                     printf("External OSPF on %s depends on routefilter %s\n", node.name, route_filter.name);
                  }
               }
            }
         }
      }
      if ($ospf_route_filters.size > 0) then {
         printf("\nOSPF route-filters on %s:\n", node.name);
         foreach route_filter:$ospf_route_filters {
            printf("\n  %s:\n", route_filter.name);
            foreach line {
               printf("    %s\n", line);   
            }
         }
      }
   }
}