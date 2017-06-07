package org.batfish.datamodel;

public class IsisRoute extends AbstractRoute {

   public static class Builder extends AbstractRouteBuilder<IsisRoute> {

      @Override
      public IsisRoute build() {
         throw new UnsupportedOperationException(
               "no implementation for generated method"); // TODO Auto-generated
                                                          // method stub
      }

      public void setLevel(IsisLevel level) {
         throw new UnsupportedOperationException(
               "no implementation for generated method"); // TODO Auto-generated
                                                          // method stub
      }

   }

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   public IsisRoute(Prefix network, Ip nextHopIp) {
      super(network, nextHopIp);
   }

   @Override
   public boolean equals(Object o) {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

   @Override
   public int getAdministrativeCost() {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

   @Override
   public Integer getMetric() {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

   @Override
   public String getNextHopInterface() {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

   @Override
   public RoutingProtocol getProtocol() {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

   @Override
   public int getTag() {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

   @Override
   public int hashCode() {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

   @Override
   public String protocolRouteString() {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

}
