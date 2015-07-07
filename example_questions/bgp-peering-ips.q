verify {
   foreach node {
      foreach interface {
         if (interface.has_ip) then {
            if (interface.is_loopback) then {
               $loopbackips.add_ip(interface.ip);
            }
            $allinterfaceips.add_ip(interface.ip);
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
               $localas:bgp_neighbor.local_as, $remoteas:bgp_neighbor.local_as, $remoteip:bgp_neighbor.remote_ip
            }
         }
         else {
            assert {
               $loopbackips.contains_ip(bgp_neighbor.remote_ip)
            }
            onfailure {
               $localas:bgp_neighbor.local_as, $remoteas:bgp_neighbor.local_as, $remoteip:bgp_neighbor.remote_ip
            }
            assert {
               $allinterfaceips.contains_ip(bgp_neighbor.remote_ip)
            }
            onfailure {
               $localas:bgp_neighbor.local_as, $remoteas:bgp_neighbor.local_as, $remoteip:bgp_neighbor.remote_ip
            }
         }
      }
   }
}
