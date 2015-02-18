package org.batfish.representation;

public enum OriginType {
   EGP("egp"),
   IGP("igp"),
   INCOMPLETE("incomplete");

   public static OriginType fromString(String originType) {
      for (OriginType ot : values()) {
         if (ot._originType.equals(originType)) {
            return ot;
         }
      }
      throw new Error("bad origin type string");
   }

   private String _originType;

   private OriginType(String originType) {
      _originType = originType;
   }

   @Override
   public String toString() {
      return _originType;
   }
}
