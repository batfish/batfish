verify {
   $assignedips:set<ip>;
   $dualassignedips:set<ip>;
   foreach node {
      foreach interface {
         if (interface.has_ip) then {
            $prev_num_ip_assignments := $assignedips.size;
            $assignedips.add(interface.ip);
            if ($assignedips.size == $prev_num_ip_assignments) then {
               $dualassignedips.add(interface.ip);
            }
         }
      }
   }
   foreach node {
      foreach interface {
         if (interface.has_ip) then {
            assert {
               not {
                  $dualassignedips.contains(interface.ip)
               }
            }
            onfailure {
               printf("%s:%s is assigned multiply-assigned ip address: %s", node.name, interface.name, interface.ip);
               if (not {interface.enabled}) then {
                  printf(" (interface is disabled)");
               }
               printf("\n");
            }
         }
      }
   }
}
