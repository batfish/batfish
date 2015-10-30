verify {
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
            assert {
               not {
                  $loopbackips.contains_ip(bgp_neighbor.remote_ip)
               }
            }
            onfailure {
               printf("Node %s in AS %s configured for eBGP session with neighbor in AS %s at ip address %s identified as the address of a loopback interface\n", node.name, bgp_neighbor.local_as, bgp_neighbor.remote_as, bgp_neighbor.remote_ip);
            }
         }
         else {
            assert {
               $loopbackips.contains_ip(bgp_neighbor.remote_ip)
            }
            onfailure {
               printf("Node %s in AS %s configured for iBGP session with neighbor at ip address %s, which is NOT identified to be the address of a loopback interface\n", node.name, bgp_neighbor.local_as, bgp_neighbor.remote_ip);
            }
            assert {
               $allinterfaceips.contains_ip(bgp_neighbor.remote_ip)
            }
            onfailure {
               printf("Node %s in AS %s configured for iBGP session with neighbor at ip address %s, which is NOT identified to be the address of a known interface\n", node.name, bgp_neighbor.local_as, bgp_neighbor.remote_ip);
            }
         }
      }
   }
}
