/**
 * Check that each BGP neighbor definition for each configuration corresponds
 * uniquely to some other BGP neighbor definition on some other configuration.
 * @param no_check_endpoint If set to true, don't check for matching BGP neighbor definitions
 * @param no_check_unique If set to true, don't check for UNIQUE matching BGP neighbor definitions
 * @param foreign_bgp_groups A set of strings signifying BGP groups for which no checks are performed. For instance, set this parameter to set&lt;string&gt;{"partners", "customers"} if you don't want to check your configurations for matching BGP neighbor definitions for BGP neighbors in the "partners" or "customers" groups because e.g. you don't own the configurations containing the matching definitions. 
 */
defaults {
   $no_check_endpoint=false;
   $no_check_local=false;
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
   $num_missing_local_ips := 0;
   foreach node {
      foreach bgp_neighbor {
         $total_num_bgp_neighbors++;
         if ($foreign_bgp_groups.contains(bgp_neighbor.group)) {
            $num_ignored_foreign_bgp_neighbors++;
         }
         else {
            $num_bgp_neighbors++;
            unless ($no_check_local) {
               assert {
                  bgp_neighbor.has_local_ip
               }
               onfailure {
                  $num_missing_local_ips++;
                  $view_name := "MISSING_LOCAL_IP";
                  $base_msg := format("Could not determine local ip address for BGP neighbor '%s' group '%s'",
                     bgp_neighbor.name,
                     bgp_neighbor.group);
                  printf("%s: %s: %s\n",
                     $view_name,
                     node.name,
                     $base_msg);
                  $view := $views.get_map($view_name);
                  $view.set("name", $view_name);
                  $view.set("type", "view");
                  $n := $view.get_map("nodes").get_map(node.name);
                  $n.set("name", node.name);
                  $n.set("type", "node");
                  $description := format("%s%s<br>",
                     $n.get("description"),
                     $base_msg);
                  $n.set("description", $description);
               }
            }
            unless ($no_check_endpoint) {
               assert {
                  bgp_neighbor.has_remote_bgp_neighbor
               }
               onfailure {
                  $view_name := "MISSING_ENDPOINT";
                  $base_msg := format("Could not determine remote BGP neighbor for BGP neighbor '%s' group '%s'",
                     bgp_neighbor.name,
                     bgp_neighbor.group);
                  printf("%s: %s: %s\n",
                     $view_name,
                     node.name,
                     $base_msg);
                  $view := $views.get_map($view_name);
                  $view.set("name", $view_name);
                  $view.set("type", "view");
                  $n := $view.get_map("nodes").get_map(node.name);
                  $n.set("name", node.name);
                  $n.set("type", "node");
                  $description := format("%s%s<br>",
                     $n.get("description"),
                     $base_msg);
                  $n.set("description", $description);
               }
            }
            if (bgp_neighbor.has_remote_bgp_neighbor){
               $num_matched_bgp_neighbors++;
               unless ($no_check_unique){
                  assert {
                     bgp_neighbor.has_single_remote_bgp_neighbor
                  }
                  onfailure {
                     $num_bgp_neighbors_with_nondeterministic_endpoint++;
                     $view_name := "NON_UNIQUE_ENDPOINT";
                     $base_msg := format("Could not uniquely determine remote BGP neighbor for BGP neighbor '%s' from candidate remote BGP neighbors:",
                        bgp_neighbor.name);
                     printf("%s: %s: %s\n",
                        $view_name,
                        node.name,
                        $base_msg);
                     $view := $views.get_map($view_name);
                     $view.set("name", $view_name);
                     $view.set("type", "view");
                     $n := $view.get_map("nodes").get_map(node.name);
                     $n.set("name", node.name);
                     $n.set("type", "node");
                     $description := format("%s%s<br>",
                        $n.get("description"),
                        $base_msg);
                     $n.set("description", $description);
                     foreach remote_bgp_neighbor {
                        $base_msg := format("CANDIDATE REMOTE BGP NEIGHBOR: '%s' on node '%s'\n",
                           remote_bgp_neighbor.name,
                           remote_bgp_neighbor.owner.name);
                        printf("\t%s\n",
                           $base_msg);
                        $description := format("%s&nbsp;%s<br>",
                           $n.get("description"),
                           $base_msg);
                        $n.set("description", $description);
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
   unless ($no_check_local) {
      printf("MISSING_LOCAL_IP: %s/%s\n", $num_missing_local_ips, $num_bgp_neighbors);
   }
   unless ($no_check_endpoint) {
      printf("MISSING_ENDPOINT: %s/%s\n", $num_missing_endpoints, $num_bgp_neighbors);
   }
   unless ($no_check_unique) {
      printf("NON_UNIQUE_ENDPOINT: %s/%s\n", $num_bgp_neighbors_with_nondeterministic_endpoint, $num_matched_bgp_neighbors);
   }
}
