package org.batfish.z3.node;

public class RoleOriginateExpr extends PacketRelExpr {

   private static final String BASENAME = "R_role_originate";

   public RoleOriginateExpr(String roleName) {
      super(BASENAME + "_" + roleName);
   }

}
