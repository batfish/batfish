/**
 * Report eBGP(iBGP) peerings with local/remote IPs on (non-)loopback interfaces, as well as unknown remote IPs for any BGP peerings
 * @param no_check_ebgp_local_ip_on_loopback If set to true, don't check for eBGP local IPs on loopback interfaces
 * @param no_check_ebgp_remote_ip_on_loopback If set to true, don't check for eBGP remote IPs on loopback interfaces
 * @param no_check_ebgp_remote_ip_unknown If set to true, don't check for unknown eBGP remote IPs
 * @param no_check_ibgp_local_ip_on_non_loopback If set to true, don't check for iBGP local IPs on non-loopback interfaces
 * @param no_check_ibgp_remote_ip_on_non_loopback If set to true, don't check for iBGP remote IPs on non-loopback interfaces
 * @param no_check_ibgp_remote_ip_unknown If set to true, don't check for unknown iBGP remote IPs
 */
defaults {
   $no_check_ebgp_local_ip_on_loopback=false;
   $no_check_ebgp_remote_ip_on_loopback=false;
   $no_check_ebgp_remote_ip_unknown=false;
   $no_check_ibgp_local_ip_on_non_loopback=false;
   $no_check_ibgp_remote_ip_on_non_loopback=false;
   $no_check_ibgp_remote_ip_unknown=false;
}
verify {
   $num_ebgp_neighbors := 0;
   $num_ibgp_neighbors := 0;
   $num_ebgp_local_ip_on_loopback := 0;
   $num_ebgp_remote_ip_on_loopback := 0;
   $num_ebgp_remote_ip_unknown := 0;
   $num_ibgp_local_ip_on_non_loopback := 0;
   $num_ibgp_remote_ip_on_non_loopback := 0;
   $num_ibgp_remote_ip_unknown := 0;
   query.set("name", "BGP Peering IPs Check");
   query.set("color", "error");
   query.set("type", "query");
   $views := query.get_map("views");
   $loopbackips:set<ip>;
   $allinterfaceips:set<ip>;
   foreach node {
      foreach interface {
         if (interface.has_ip) then {
            if (interface.is_loopback) then {
               $loopbackips.add(interface.ip);
            }
            $allinterfaceips.add(interface.ip);
         }
      }
   }
   foreach node {
      foreach bgp_neighbor {
         if (bgp_neighbor.remote_as != bgp_neighbor.local_as) then {
            $num_ebgp_neighbors++;
            assert {
               not {
                  $loopbackips.contains(bgp_neighbor.local_ip)
               }
            }
            onfailure {
               $num_ebgp_local_ip_on_loopback++;
               $view_name := "EBGP_LOCAL_IP_ON_LOOPBACK";
               $base_msg := format("Local IP %s of eBGP session configured with remote IP %s, local AS %s, and remote AS %s identified as the address of a loopback interface.",
                  bgp_neighbor.local_ip,
                  bgp_neighbor.remote_ip,
                  bgp_neighbor.local_as,
                  bgp_neighbor.remote_as);
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
            if ($allinterfaceips.contains(bgp_neighbor.remote_ip)) {
               assert {
                  not {
                     $loopbackips.contains(bgp_neighbor.remote_ip)
                  }
               }
               onfailure {
                  $num_ebgp_remote_ip_on_loopback++;
                  $view_name := "EBGP_REMOTE_IP_ON_LOOPBACK";
                  $base_msg := format("Remote IP %s of eBGP session configured with local AS %s and remote AS %s identified as the address of a loopback interface.",
                     bgp_neighbor.remote_ip,
                     bgp_neighbor.local_as,
                     bgp_neighbor.remote_as);
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
            assert {
               not {
                  $loopbackips.contains(bgp_neighbor.remote_ip)
               }
            }
            onfailure {
               $num_ebgp_remote_ip_unknown++;
               $view_name := "EBGP_REMOTE_IP_UNKNOWN";
               $base_msg := format("Remote IP %s of eBGP session configured with local AS %s and remote AS %s not identified as the address of a known interface.",
                  bgp_neighbor.remote_ip,
                  bgp_neighbor.local_as,
                  bgp_neighbor.remote_as);
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
         else {
            $num_ibgp_neighbors++;
            assert {
               $loopbackips.contains(bgp_neighbor.local_ip)
            }
            onfailure {
               $num_ibgp_local_ip_on_non_loopback++;
               $view_name := "IBGP_LOCAL_IP_ON_NON_LOOPBACK";
               $base_msg := format("Local IP %s of iBGP session configured with remote IP %s and AS %s identified as the address of a non-loopback interface.",
                  bgp_neighbor.local_ip,
                  bgp_neighbor.remote_ip,
                  bgp_neighbor.local_as);
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
            if ($allinterfaceips.contains(bgp_neighbor.remote_ip)) {
               assert {
                  $loopbackips.contains(bgp_neighbor.remote_ip)
               }
               onfailure {
                  $num_ibgp_remote_ip_on_non_loopback++;
                  $view_name := "IBGP_REMOTE_IP_ON_NON_LOOPBACK";
                  $base_msg := format("Remote IP %s of iBGP session configured with AS %s identified as the address of a non-loopback interface.",
                     bgp_neighbor.remote_ip,
                     bgp_neighbor.local_as);
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
            assert {
               $allinterfaceips.contains(bgp_neighbor.remote_ip)
            }
            onfailure {
               $num_ibgp_remote_ip_unknown++;
               $view_name := "IBGP_REMOTE_IP_UNKNOWN";
               $base_msg := format("Remote IP %s of iBGP session configured with AS %s not identified as the address of a known interface.",
                  bgp_neighbor.remote_ip,
                  bgp_neighbor.local_as);
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
   }
   printf("****Summary****\n");
   unless ($no_check_ebgp_local_ip_on_loopback){
      printf("EBGP_LOCAL_IP_ON_LOOPBACK: %s/%s\n", $num_ebgp_local_ip_on_loopback, $num_ebgp_neighbors);
   }
   unless ($no_check_ebgp_remote_ip_on_loopback){
      printf("EBGP_REMOTE_IP_ON_LOOPBACK: %s/%s\n", $num_ebgp_remote_ip_on_loopback, $num_ebgp_neighbors);
   }
   unless ($no_check_ebgp_remote_ip_unknown){
      printf("EBGP_REMOTE_IP_UNKNOWN: %s/%s\n", $num_ebgp_remote_ip_unknown, $num_ebgp_neighbors);
   }
   unless ($no_check_ibgp_local_ip_on_non_loopback){
      printf("IBGP_LOCAL_IP_ON_NON_LOOPBACK: %s/%s\n", $num_ibgp_local_ip_on_non_loopback, $num_ibgp_neighbors);
   }
   unless ($no_check_ibgp_remote_ip_on_non_loopback){
      printf("IBGP_REMOTE_IP_ON_NON_LOOPBACK: %s/%s\n", $num_ibgp_remote_ip_on_non_loopback, $num_ibgp_neighbors);
   }
   unless ($no_check_ibgp_remote_ip_unknown){
      printf("IBGP_REMOTE_IP_UNKNOWN: %s/%s\n", $num_ibgp_remote_ip_unknown, $num_ibgp_neighbors);
   }
}
