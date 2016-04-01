/**
 * Report IP addresses assigned to multiple interfaces.
 * @param check_disabled If set to true, also include interfaces that are disabled
 */
defaults {
   $check_disabled=false;
}
verify {
   query.set("name", "Unique IP Address Assignment Violations");
   query.set("color", "error");
   query.set("type", "query");
   $views := query.get_map("views");
   $loopbackip := 127.0.0.1;
   $assignedips:set<ip>;
   $dualassignedips:set<ip>;
   foreach node {
      foreach interface {
         if (or{$check_disabled, interface.enabled}) {
            if (interface.has_ip) {
               unless (and{interface.is_loopback, interface.ip == $loopbackip}) {
                  $prev_num_ip_assignments := $assignedips.size;
                  $assignedips.add(interface.ip);
                  if ($assignedips.size == $prev_num_ip_assignments) {
                     $dualassignedips.add(interface.ip);
                  }
               }
            }
         }
      }
   }
   foreach node {
      foreach interface {
         if (or{$check_disabled, interface.enabled}) {
            if (interface.has_ip) {
               unless (and{interface.is_loopback, interface.ip == $loopbackip}) {
                  assert {
                     not {
                        $dualassignedips.contains(interface.ip)
                     }
                  }
                  onfailure {
                     printf("%s:%s is assigned multiply-assigned ip address: %s",
                        node.name,
                        interface.name,
                        interface.ip);
                     if (not {interface.enabled}) {
                        printf(" (interface is disabled)");
                     }
                     printf("\n");
                     $v := $views.get_map(interface.ip);
                     $v.set("name", interface.ip);
                     $v.set("type", "view");
                     $n := $v.get_map("nodes").get_map(node.name);
                     $n.set("name", node.name);
                     $n.set("type", "node");
                     $n.set("description", $n.get("description") + " " + interface.name);
                  }
               }
            }
         }
      }
   }
}
