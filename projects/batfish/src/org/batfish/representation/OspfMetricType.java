package org.batfish.representation;

public enum OspfMetricType {
   E1,
   E2;

   public static OspfMetricType fromInteger(int i) {
      switch (i) {
      case 1:
         return E1;
      case 2:
         return E2;
      default:
         throw new Error("invalid ospf metric type");
      }
   }
}
