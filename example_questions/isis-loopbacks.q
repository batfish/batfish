verify {
   foreach node {
      foreach interface {
         if (interface.is_loopback) then {
            assert {
               or {
                  interface.isis.l1_active,
                  interface.isis.l1_passive,
                  interface.isis.l2_active,
                  interface.isis.l2_passive
               }
            }
            onfailure {
               printf("Loopback interface %s:%s is neither active nor passive wrt level1 nor level2 IS-IS, so its network %s will not appear in IS-IS RIB.\n", node.name, interface.name, interface.prefix);
            }
         }
      }
   }
}
