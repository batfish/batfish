defaults {
   $no_check_endpoint=false;
   $no_check_unique=false;
   $foreign_bgp_groups=set<string>{};
}
verify {
   query.set("name", "BGP Session Check");
   query.set("color", "error");
   query.set("type", "query");
   $views := query.get_map("views");
   $total_num_bgp_neighbors := 0;
   $num_bgp_neighbors := 0;
   $num_bgp_neighbors_with_nondeterministic_endpoint := 0;
   $num_ignored_foreign_bgp_neighbors := 0;
   $num_matched_bgp_neighbors := 0;
   foreach node {
      foreach bgp_neighbor {
         $total_num_bgp_neighbors++;
         if ($foreign_bgp_groups.contains(bgp_neighbor.group)) {
            $num_ignored_foreign_bgp_neighbors++;
         }
         else {
            $num_bgp_neighbors++;
            unless ($no_check_endpoint){
               assert {
                  bgp_neighbor.has_remote_bgp_neighbor
               }
               onfailure {
                  printf("MISSING_ENDPOINT: Could not determine remote BGP neighbor for BGP neighbor '%s' group '%s' on node '%s'\n",
                     bgp_neighbor.name,
                     bgp_neighbor.group,
                     node.name);
                  $view_name := "MISSING_ENDPOINT";
                  $view := $views.get_map($view_name);
                  $view.set("name", $view_name);
                  $view.set("type", "view");
                  $n := $view.get_map("nodes").get_map(node.name);
                  $n.set("name", node.name);
                  $n.set("type", "node");
                  $n.set("description", $n.get("description") + "Could not determine remote BGP neighbor for BGP neighbor '" + bgp_neighbor.name + "' group '" + bgp_neighbor.group + "'<br>");
               }
            }
            if (bgp_neighbor.has_remote_bgp_neighbor){
               $num_matched_bgp_neighbors++;
               unless ($no_check_unique){
                  assert {
                     bgp_neighbor.has_single_remote_bgp_neighbor
                  }
                  onfailure {
                     $num_bgp_neighbors_with_nondeterministic_endpoint := $num_bgp_neighbors_with_nondeterministic_endpoint + 1;
                     printf("NON_UNIQUE_ENDPOINT: Could not uniquely determine remote BGP neighbor for BGP neighbor '%s' on node '%s' from candidate remote BGP neighbors:\n",
                        bgp_neighbor.name,
                        node.name);
                     $view_name := "NON_UNIQUE_ENDPOINT";
                     $view := $views.get_map($view_name);
                     $view.set("name", $view_name);
                     $view.set("type", "view");
                     $n := $view.get_map("nodes").get_map(node.name);
                     $n.set("name", node.name);
                     $n.set("type", "node");
                     $n.set("description", $n.get("description") + "Could not uniquely determine remote BGP neighbor for BGP neighbor '" + bgp_neighbor.name + "' group '" + bgp_neighbor.group + "' among candidate remote BGP neighbors:<br>");
                     foreach remote_bgp_neighbor {
                        printf("\tCANDIDATE REMOTE BGP NEIGHBOR: '%s' on node '%s'\n",
                           remote_bgp_neighbor.name,
                           remote_bgp_neighbor.owner.name);
                        $n.set("description", $n.get("description") + "&nbsp;" + remote_bgp_neighbor.name + " on node '" + remote_bgp_neighbor.owner.name + "<br>");
                     }
                  }
               }
            }
         }
      }
   }
   $num_missing_endpoints := $num_bgp_neighbors - $num_matched_bgp_neighbors;
   printf("****Summary****\n");
   unless ($foreign_bgp_groups.size == 0) {
      printf("IGNORED_FOREIGN_BGP_GROUPS:\n");
      foreach $foreign_bgp_group : $foreign_bgp_groups {
         printf("\t%s\n", $foreign_bgp_group);
      }
      printf("IGNORED_FOREIGN_ENDPOINT: %s/%s\n", $num_ignored_foreign_bgp_neighbors, $total_num_bgp_neighbors);
   }
   unless ($no_check_endpoint){
      printf("MISSING_ENDPOINT: %s/%s\n", $num_missing_endpoints, $num_bgp_neighbors);
   }
   unless ($no_check_unique){
      printf("NON_UNIQUE_ENDPOINT: %s/%s\n", $num_bgp_neighbors_with_nondeterministic_endpoint, $num_matched_bgp_neighbors);
   }
}
