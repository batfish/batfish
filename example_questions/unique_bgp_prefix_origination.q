/**
 * Check that the spaces of prefixes originated from each BGP process are mutually disjoint. Note that this query may overapproximate such prefix spaces, and so not all violations (overlaps) are guaranteed to be true positives.   
 */
verify {
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
               printf("BGP export space on node '%s' overlaps with that of node '%s'\n",
                  $i.name,
                  $j.name);
               $intersection := $i.bgp_origination_space_explicit.intersection($j.bgp_origination_space_explicit);
               printf("\tIntersection: '%s'\n", $intersection);
            }
         }
      }
   }
}
