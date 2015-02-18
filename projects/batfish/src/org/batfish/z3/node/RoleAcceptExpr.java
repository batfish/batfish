package org.batfish.z3.node;

public class RoleAcceptExpr extends PacketRelExpr {

   private static final String BASENAME = "R_role_accept";

   public RoleAcceptExpr(String roleName) {
      super(BASENAME + "_" + roleName);
   }

}
