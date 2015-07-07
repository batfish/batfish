verify {
   $assignedips.clear_ips;
   $dualassignedips.clear_ips;
   foreach node {
      foreach interface {
         if (interface.has_ip) then {
            $prev_num_ip_assignments := $assignedips.num_ips;
            $assignedips.add_ip(interface.ip);
            if ($assignedips.num_ips == $prev_num_ip_assignments) then {
               $dualassignedips.add_ip(interface.ip);
            }
         }
      }
   }
   foreach node {
      foreach interface {
         if (interface.has_ip) then {
            assert {
               not {
                  $dualassignedips.contains_ip(interface.ip)
               }
            }
            onfailure {
               $offendingIp:interface.ip
            }
         }
      }
   }
}
