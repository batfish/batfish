verify {
   $loopbackip := 127.0.0.1;
   foreach node {
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
      foreach interface {
         if (interface.has_ip) then {
            unless (and{interface.is_loopback, interface.ip == $loopbackip}) {
               assert {
                  not {
                     $dual_assigned_subnets.contains(interface.subnet)
                  }
               }
               onfailure {
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
   }
}
