/**
 * Report the existence of multiple interfaces on a single router that share the same subnet.
 */
verify {
   query.set("name", "Self-link Violations");
   query.set("color", "error");
   query.set("type", "query");
   $views := query.get_map("views");
   $loopbackip := 127.0.0.1;
   foreach node {
      $subnet_interfaces := new_map;
      $assigned_subnets:set<prefix>;
      $dual_assigned_subnets:set<prefix>;
      $assigned_subnets.clear;
      $dual_assigned_subnets.clear;
      foreach interface {
         if (interface.has_ip) then {
            unless (and{interface.is_loopback, interface.ip == $loopbackip}) {
               $prev_num_subnet_assignments := $assigned_subnets.size;
               $assigned_subnets.add(interface.subnet);
               if ($assigned_subnets.size == $prev_num_subnet_assignments) then {
                  $dual_assigned_subnets.add(interface.subnet);
               }
            }
         }
      }
      $interface_addresses := new_map;
      foreach interface {
         if (interface.has_ip) then {
            unless (and{interface.is_loopback, interface.ip == $loopbackip}) {
               assert {
                  not {
                     $dual_assigned_subnets.contains(interface.subnet)
                  }
               }
               onfailure {
                  $subnet_interfaces.get_map(interface.subnet).get_map("interfaces").set(interface.name, interface.all_prefixes);
                  $interface_addresses.set(interface.name, interface.ip);
                  printf("'%s':'%s' is one of multiple interfaces on '%s' assigned to subnet '%s'",
                     node.name,
                     interface.name,
                     node.name,
                     interface.subnet);
                  if (not {interface.enabled}) then {
                     printf(" (interface is DISABLED)");
                  }
                  printf("\n");
               }
            }
         }
      }
      $subnet_names := $subnet_interfaces.keys;
      foreach $subnet_name : $subnet_names {
         $interfaces := $subnet_interfaces.get_map($subnet_name).get_map("interfaces").keys;
         foreach $interface1 : $interfaces {
            foreach $interface2 : $interfaces {
               if ($interface1 < $interface2) {
                  $link_name := $subnet_name + ":" + $interface1 + ":" + $interface2;
                  $view := $views.get_map(node.name);
                  $view.set("name", node.name);
                  $view.set("type", "view");
                  $link := $view.get_map("links").get_map($link_name);
                  $link.set("type", "link");
                  $link_int1 := $link.get_map("interface1");
                  $link_int1.set("type", "interface");
                  $link_int1.set("node", node.name);
                  $link_int1.set("name", $interface1);
                  $link_int2 := $link.get_map("interface2");
                  $link_int2.set("type", "interface");
                  $link_int2.set("node", node.name);
                  $link_int2.set("name", $interface2);
                  $link.set("description",
                     node.name + ":" + $interface1 + " prefixes: " + $subnet_interfaces.get_map($subnet_name).get_map("interfaces").get($interface1) + "<br>" +
                     node.name + ":" + $interface2 + " prefixes: " + $subnet_interfaces.get_map($subnet_name).get_map("interfaces").get($interface2));
               }
            }
         }
      }
   }
}
