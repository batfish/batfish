/**
 * Check whether each loopback interface subnet will be exported (either actively or passively) into OSPF
 */
verify {
   query.set("name", "Loopbacks not exported into OSPF");
   query.set("color", "error");
   query.set("type", "query");
   $views := query.get_map("views");
   foreach node {
      foreach interface {
         if (interface.is_loopback) {
            assert {
               or {
                  interface.ospf.active,
                  interface.ospf.passive
               }
            }
            onfailure {
               if (interface.has_ip) {
                  $ospf_network := interface.prefix;
                  $ospf_network_str := format("%s",$ospf_network);
               }
               else {
                  $ospf_network_str := "<unassigned>";
               }
               if (interface.enabled) {
                  $interface_disabled_str := "";
               }
               else {
                  $interface_disabled_str := " (interface is disabled)";
               }
               $base_msg := format("Loopback interface %s is neither active nor passive wrt OSPF, so its network %s will not appear in OSPF RIB.%s",
                  interface.name,
                  $ospf_network_str,
                  $interface_disabled_str);
               printf("%s: %s\n",
                  node.name,
                  $base_msg);
               $view_name := "violations";
               $v := $views.get_map($view_name);
               $v.set("name", $view_name);
               $v.set("type", "view");
               $n := $v.get_map("nodes").get_map(node.name);
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
}
