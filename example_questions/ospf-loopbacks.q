verify {
   foreach node {
      foreach interface {
         if (interface.is_loopback) then {
            assert {
               or {
                  interface.ospf.active,
                  interface.ospf.passive
               }
            }
         }
      }
   }
}
