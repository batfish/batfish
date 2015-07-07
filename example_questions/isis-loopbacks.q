verify {
   foreach node {
      foreach interface {
         if (interface.is_loopback) then {
            assert {
               or {
                  interface.isis.active,
                  interface.isis.passive
               }
            }
         }
      }
   }
}
