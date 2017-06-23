package org.batfish.z3.node;

public class ExternalSourceIpExpr extends PacketRelExpr {

   public static final ExternalSourceIpExpr INSTANCE = new ExternalSourceIpExpr();

   private static final String NAME = "External_source_ip";

   private ExternalSourceIpExpr() {
      super(NAME);
   }

}
