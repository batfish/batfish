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
            onfailure {
               printf("Loopback interface %s:%s is neither active nor passive wrt OSPF, so its network %s will not appear in OSPF RIB.\n", node.name, interface.name, interface.prefix);
            }
         }
      }
   }
}
