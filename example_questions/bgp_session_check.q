/**
 * Check that each BGP neighbor definition for each configuration corresponds
 * uniquely to some other BGP neighbor definition on some other configuration.
 * Report eBGP(iBGP) peerings with local/remote IPs on (non-)loopback interfaces,
 * as well as unknown remote IPs for any BGP peerings. Check for half-open sessions
 * @param foreign_bgp_groups A set of strings signifying BGP groups for which no checks are performed. For instance, set this parameter to set&lt;string&gt;{"partners", "customers"} if you don't want to check your configurations for matching BGP neighbor definitions for BGP neighbors in the "partners" or "customers" groups because e.g. you don't own the configurations containing the matching definitions. 
 * @param no_check_ebgp_local_ip_on_loopback If set to true, don't check for eBGP local IPs on loopback interfaces
 * @param no_check_ebgp_remote_ip_on_loopback If set to true, don't check for eBGP remote IPs on loopback interfaces
 * @param no_check_ebgp_remote_ip_unknown If set to true, don't check for unknown eBGP remote IPs
 * @param no_check_half_open If set to true, don't check for half-open connections that are otherwise correctly configured
 * @param no_check_ibgp_local_ip_on_non_loopback If set to true, don't check for iBGP local IPs on non-loopback interfaces
 * @param no_check_ibgp_remote_ip_on_non_loopback If set to true, don't check for iBGP remote IPs on non-loopback interfaces
 * @param no_check_ibgp_remote_ip_unknown If set to true, don't check for unknown iBGP remote IPs
 * @param no_check_unique If set to true, don't check for UNIQUE matching BGP neighbor definitions
 * @param no_print_broken_total If set to true, don't print total broken sessions (including eBGP and iBGP)

 */
defaults {
   $no_check_ebgp_local_ip_on_loopback=false;
   $no_check_ebgp_remote_ip_on_loopback=false;
   $no_check_ebgp_remote_ip_unknown=false;
   $no_check_half_open=false;
   $no_check_ibgp_local_ip_on_non_loopback=false;
   $no_check_ibgp_remote_ip_on_non_loopback=false;
   $no_check_ibgp_remote_ip_unknown=false;
   $no_check_local=false;
   $no_print_broken_total=false;
   $no_check_unique=false;
   $foreign_bgp_groups=set<string>{};
}
verify {
   /* JSON output initialization */
   query.set("name", "BGP Session Check");
   query.set("color", "error");
   query.set("type", "query");
   $views := query.get_map("views");
   $num_bgp_neighbors := 0;
   $num_bgp_neighbors_with_nondeterministic_endpoint := 0;
   $num_broken_ebgp := 0;
   $num_broken_ibgp := 0;
   $num_broken_total := 0;
   $num_ebgp_neighbors := 0;
   $num_ebgp_local_ip_on_loopback := 0;
   $num_ebgp_remote_ip_on_loopback := 0;
   $num_ebgp_remote_ip_unknown := 0;
   $num_half_open := 0;
   $num_half_open_candidates := 0;
   $num_ibgp_local_ip_on_non_loopback := 0;
   $num_ibgp_neighbors := 0;
   $num_ibgp_remote_ip_on_non_loopback := 0;
   $num_ibgp_remote_ip_unknown := 0;
   $num_ignored_foreign_bgp_neighbors := 0;
   $num_matched_bgp_neighbors := 0;
   $num_missing_local_ips := 0;
   $num_non_foreign_ebgp_neighbors := 0;
   $num_non_foreign_ibgp_neighbors := 0;
   $total_num_bgp_neighbors := 0;
   
   /* Collect interface IPs, and loopback IPs specifcally */
   $allinterfaceips:set<ip>;   
   $loopbackips:set<ip>;
   foreach node {
      foreach interface {
         if (interface.has_ip) {
            $interface_prefixes := interface.all_prefixes;
            foreach $interface_prefix : $interface_prefixes {
               $interface_ip := $interface_prefix.address;
               if (interface.is_loopback) {
                  $loopbackips.add($interface_ip);
               }
               $allinterfaceips.add($interface_ip);
            }
         }
      }
   }
   
   foreach node {
      foreach bgp_neighbor {
         /* Add to count of ALL BGP neighbors, including ignored ones */
         $total_num_bgp_neighbors++;

         /* Do (ebgp)ibgp (non-)loopback peering IP checks  of local ips regardless of whether bgp neighbor is foreign */
         /* eBGP checks */
         if (bgp_neighbor.remote_as != bgp_neighbor.local_as) {
            $num_ebgp_neighbors++;
            /* Assert that local address reported to eBGP neighbor is NOT that of a loopback interface */
            assert {
               not {
                  $loopbackips.contains(bgp_neighbor.local_ip)
               }
            }
            onfailure {
               $num_ebgp_local_ip_on_loopback++;
               $view_name := "EBGP_LOCAL_IP_ON_LOOPBACK";
               $base_msg := format("Local IP %s of eBGP session configured with remote IP %s, local AS %s, remote AS %s, group '%s', and description '%s' identified as the address of a loopback interface.",
                  bgp_neighbor.local_ip,
                  bgp_neighbor.remote_ip,
                  bgp_neighbor.local_as,
                  bgp_neighbor.remote_as,
                  bgp_neighbor.group,
                  bgp_neighbor.description);
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
         /* iBGP checks */
         else {
            $num_ibgp_neighbors++;
            /* Assert that local address reported to iBGP neighbor is NOT that of a loopback interface */
            assert {
               $loopbackips.contains(bgp_neighbor.local_ip)
            }
            onfailure {
               $num_ibgp_local_ip_on_non_loopback++;
               $view_name := "IBGP_LOCAL_IP_ON_NON_LOOPBACK";
               $base_msg := format("Local IP %s of iBGP session configured with remote IP %s, AS %s, group '%s', and description '%s' identified as the address of a non-loopback interface.",
                  bgp_neighbor.local_ip,
                  bgp_neighbor.remote_ip,
                  bgp_neighbor.local_as,
                  bgp_neighbor.group,
                  bgp_neighbor.description);
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
         
         /* For remaining checks, skip neighbors in foreign BGP groups, i.e. those we explicitly were asked to ignore */
         if ($foreign_bgp_groups.contains(bgp_neighbor.group)) {
            $num_ignored_foreign_bgp_neighbors++;
         }
         else {
            $broken := false;
            /* Begin checks */
            /* Add to count of non-ignored BGP neighbors */
            $num_bgp_neighbors++;
            if (bgp_neighbor.remote_as != bgp_neighbor.local_as) {
               /* Add to count of non-ignored eBGP neighbors */
               $num_non_foreign_ebgp_neighbors++;
               /* Assert that the remote address of this eBGP neighbor belongs to a known interface */            
               assert {
                  $allinterfaceips.contains(bgp_neighbor.remote_ip)
               }
               onfailure {
                  $broken := true;
                  $num_ebgp_remote_ip_unknown++;
                  $view_name := "BROKEN_EBGP_REMOTE_IP_UNKNOWN";
                  $base_msg := format("Remote IP %s of eBGP session configured with local AS %s, remote AS %s, group '%s', and description '%s' not identified as the address of a known interface.",
                     bgp_neighbor.remote_ip,
                     bgp_neighbor.local_as,
                     bgp_neighbor.remote_as,
                     bgp_neighbor.group,
                     bgp_neighbor.description);
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
               /* If in fact the remote address of the eBGP neighbor is known, do the included check */
               if ($allinterfaceips.contains(bgp_neighbor.remote_ip)) {
                  /* Assert that remote address of the eBGP neighbor is NOT that of a loopback interface */
                  assert {
                     not {
                        $loopbackips.contains(bgp_neighbor.remote_ip)
                     }
                  }
                  onfailure {
                     $num_ebgp_remote_ip_on_loopback++;
                     $view_name := "EBGP_REMOTE_IP_ON_LOOPBACK";
                     $base_msg := format("Remote IP %s of eBGP session configured with local AS %s, remote AS %s, group '%s', and description '%s' identified as the address of a loopback interface.",
                        bgp_neighbor.remote_ip,
                        bgp_neighbor.local_as,
                        bgp_neighbor.remote_as,
                        bgp_neighbor.group,
                        bgp_neighbor.description);
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
            }
            else {
               /* Add to count of non-ignored iBGP neighbors */
               $num_non_foreign_ibgp_neighbors++;
               /* Assert that the remote address of this iBGP neighbor belongs to a known interface */            
               assert {
                  $allinterfaceips.contains(bgp_neighbor.remote_ip)
               }
               onfailure {
                  $broken := true;
                  $num_ibgp_remote_ip_unknown++;
                  $view_name := "BROKEN_IBGP_REMOTE_IP_UNKNOWN";
                  $base_msg := format("Remote IP %s of iBGP session configured with AS %s, group '%s', and description '%s' not identified as the address of a known interface.",
                     bgp_neighbor.remote_ip,
                     bgp_neighbor.local_as,
                     bgp_neighbor.group,
                     bgp_neighbor.description);
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
               /* If in fact the remote address of the iBGP neighbor is known, do the included check */
               if ($allinterfaceips.contains(bgp_neighbor.remote_ip)) {
                  /* Assert that remote address of the iBGP neighbor is that of a loopback interface */
                  assert {
                     $loopbackips.contains(bgp_neighbor.remote_ip)
                  }
                  onfailure {
                     $num_ibgp_remote_ip_on_non_loopback++;
                     $view_name := "IBGP_REMOTE_IP_ON_NON_LOOPBACK";
                     $base_msg := format("Remote IP %s of iBGP session configured with AS %s, group '%s', and description '%s' identified as the address of a non-loopback interface.",
                        bgp_neighbor.remote_ip,
                        bgp_neighbor.local_as,
                        bgp_neighbor.group,
                        bgp_neighbor.description);
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
            }
            unless ($no_check_local) {
               /* Assert that we are able to determine the local IP reported to this BGP neighbor */
               assert {
                  bgp_neighbor.has_local_ip
               }
               onfailure {
                  $broken := true;
                  $num_missing_local_ips++;
                  $view_name := "BROKEN_MISSING_LOCAL_IP";
                  $base_msg := format("Could not determine local ip address for BGP neighbor '%s' configured with local AS %s, remote AS %s, group '%s', and description '%s'",
                     bgp_neighbor.name,
                     bgp_neighbor.local_as,
                     bgp_neighbor.remote_as,
                     bgp_neighbor.group,
                     bgp_neighbor.description);
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
            unless ($no_check_half_open) {
               if (and {bgp_neighbor.has_local_ip, $allinterfaceips.contains(bgp_neighbor.remote_ip)}) {
                  $num_half_open_candidates++;
                  /* Assert that this BGP neighbor connection is reciprocated at the other end and expected to be functional */
                  assert {
                     bgp_neighbor.has_remote_bgp_neighbor
                  }
                  onfailure {
                     $broken := true;
                     $num_half_open++;
                     $view_name := "BROKEN_HALF_OPEN";
                     $base_msg := format("Peering session with BGP neighbor '%s' configured with local IP %s, local AS %s, remote AS %s, group '%s', and description '%s' incorrectly configured on remote endpoint",
                        bgp_neighbor.name,
                        bgp_neighbor.local_ip,
                        bgp_neighbor.local_as,
                        bgp_neighbor.remote_as,
                        bgp_neighbor.group,
                        bgp_neighbor.description);
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
            }
            if (bgp_neighbor.has_remote_bgp_neighbor) {
               $num_matched_bgp_neighbors++;
               unless ($no_check_unique){
                  /* Assert that this BGP neighbor connection has a unique remote endpoint */
                  assert {
                     bgp_neighbor.has_single_remote_bgp_neighbor
                  }
                  onfailure {
                     $num_bgp_neighbors_with_nondeterministic_endpoint++;
                     $view_name := "NON_UNIQUE_ENDPOINT";
                     $base_msg := format("Could not uniquely determine remote BGP neighbor for BGP neighbor '%s' configured with local IP %s, local AS %s, remote AS %s, group '%s', and description '%s'from candidate remote BGP neighbors:",
                        bgp_neighbor.name,
                        bgp_neighbor.local_ip,
                        bgp_neighbor.local_as,
                        bgp_neighbor.remote_as,
                        bgp_neighbor.group,
                        bgp_neighbor.description);
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
            if ($broken) {
               $num_broken_total++;
               if (bgp_neighbor.remote_as != bgp_neighbor.local_as) {
                  $num_broken_ebgp++;
               }
               else {
                  $num_broken_ibgp++;
               }
            }
         }
      }
   }
   printf("****Summary****\n");
   printf("Failed assertions:\n");
   unless ($foreign_bgp_groups.size == 0) {
      printf("IGNORED_FOREIGN_BGP_GROUPS:\n");
      foreach $foreign_bgp_group : $foreign_bgp_groups {
         printf("\t%s\n", $foreign_bgp_group);
      }
      printf("IGNORED_FOREIGN_ENDPOINT: %s/%s\n", $num_ignored_foreign_bgp_neighbors, $total_num_bgp_neighbors);
   }
   unless ($no_check_ebgp_local_ip_on_loopback){
      printf("EBGP_LOCAL_IP_ON_LOOPBACK: %s/%s\n", $num_ebgp_local_ip_on_loopback, $num_ebgp_neighbors);
   }
   unless ($no_check_ebgp_remote_ip_on_loopback){
      printf("EBGP_REMOTE_IP_ON_LOOPBACK: %s/%s\n", $num_ebgp_remote_ip_on_loopback, $num_ebgp_neighbors);
   }
   unless ($no_check_ebgp_remote_ip_unknown){
      printf("BROKEN_EBGP_REMOTE_IP_UNKNOWN: %s/%s\n", $num_ebgp_remote_ip_unknown, $num_non_foreign_ebgp_neighbors);
   }
   unless ($no_check_ibgp_local_ip_on_non_loopback){
      printf("IBGP_LOCAL_IP_ON_NON_LOOPBACK: %s/%s\n", $num_ibgp_local_ip_on_non_loopback, $num_ibgp_neighbors);
   }
   unless ($no_check_ibgp_remote_ip_on_non_loopback){
      printf("IBGP_REMOTE_IP_ON_NON_LOOPBACK: %s/%s\n", $num_ibgp_remote_ip_on_non_loopback, $num_ibgp_neighbors);
   }
   unless ($no_check_ibgp_remote_ip_unknown){
      printf("BROKEN_IBGP_REMOTE_IP_UNKNOWN: %s/%s\n", $num_ibgp_remote_ip_unknown, $num_non_foreign_ibgp_neighbors);
   }
   unless ($no_check_local) {
      printf("BROKEN_MISSING_LOCAL_IP: %s/%s\n", $num_missing_local_ips, $num_bgp_neighbors);
   }
   unless ($no_check_unique) {
      printf("NON_UNIQUE_ENDPOINT: %s/%s\n", $num_bgp_neighbors_with_nondeterministic_endpoint, $num_matched_bgp_neighbors);
   }
   unless ($no_check_half_open) {
      printf("BROKEN_HALF_OPEN: %s/%s\n", $num_half_open, $num_half_open_candidates);
   }
   unless ($no_print_broken_total) {
      printf("Totals:\n");
      printf("BROKEN_EBGP: %s/%s\n", $num_broken_ebgp, $num_non_foreign_ebgp_neighbors);
      printf("BROKEN_IBGP: %s/%s\n", $num_broken_ibgp, $num_non_foreign_ibgp_neighbors);
      printf("BROKEN_TOTAL: %s/%s\n", $num_broken_total, $num_bgp_neighbors);
   }
}
