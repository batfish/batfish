/**
 * Check that the spaces of prefixes originated from each BGP process are mutually disjoint. Note that this query may overapproximate such prefix spaces, and so not all violations (overlaps) are guaranteed to be true positives.   
 */
verify {
   query.set("name", "Nodes with overlapping BGP prefix export space");
   query.set("color", "error");
   query.set("type", "query");
   $views := query.get_map("views");
   foreach node $i {
      foreach node $j {
         if (and {
               $i.name < $j.name,
               $i.bgp.configured,
               $j.bgp.configured
               }) {
            assert {
               not{
                  $i.bgp_origination_space_explicit.overlaps($j.bgp_origination_space_explicit)
               }
            }
            onfailure {
               /* base message for first node */
               $base_msg := format("BGP export space overlaps with that of node '%s'",
                  $j.name);
               printf("%s: %s\n",
                  $i.name,
                  $base_msg);
               $view_name := "violations";
               $v := $views.get_map($view_name);
               $v.set("name", $view_name);
               $v.set("type", "view");
               $n := $v.get_map("nodes").get_map($i.name);
               $n.set("name", $i.name);
               $n.set("type", "node");
               $description := format("%s%s<br>",
                  $n.get("description"),
                  $base_msg);
               $n.set("description", $description);
               /* intersection addon for first node */
               $intersection := $i.bgp_origination_space_explicit.intersection($j.bgp_origination_space_explicit);
               $base_msg := format("Intersection: '%s'",
                  $intersection);
               printf("\t%s\n",
                  $base_msg);
               $n := $v.get_map("nodes").get_map($i.name);
               $n.set("name", $i.name);
               $n.set("type", "node");
               $description := format("%s&nbsp;%s<br>",
                  $n.get("description"),
                  $base_msg);
               $n.set("description", $description);

               /* base message for second node (JSON only) */
               $base_msg := format("BGP export space overlaps with that of node '%s'",
                  $i.name);
               $view_name := "violations";
               $v := $views.get_map($view_name);
               $v.set("name", $view_name);
               $v.set("type", "view");
               $n := $v.get_map("nodes").get_map($j.name);
               $n.set("name", $j.name);
               $n.set("type", "node");
               $description := format("%s%s<br>",
                  $n.get("description"),
                  $base_msg);
               $n.set("description", $description);
               /* intersection addon for second node (JSON only)*/
               $base_msg := format("Intersection: '%s'",
                  $intersection);
               $n := $v.get_map("nodes").get_map($j.name);
               $n.set("name", $j.name);
               $n.set("type", "node");
               $description := format("%s&nbsp;%s<br>",
                  $n.get("description"),
                  $base_msg);
               $n.set("description", $description);
            }
         }
      }
   }
}
