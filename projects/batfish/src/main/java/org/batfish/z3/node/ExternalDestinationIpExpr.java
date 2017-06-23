package org.batfish.z3.node;

public class ExternalDestinationIpExpr extends PacketRelExpr {

   public static final ExternalDestinationIpExpr INSTANCE = new ExternalDestinationIpExpr();

   private static final String NAME = "External_destination_ip";

   private ExternalDestinationIpExpr() {
      super(NAME);
   }

}
