package org.batfish.representation.cisco;

import org.batfish.representation.Configuration;
import org.batfish.representation.OriginType;
import org.batfish.representation.PolicyMapSetLine;
import org.batfish.representation.PolicyMapSetOriginTypeLine;

public class RouteMapSetOriginTypeLine extends RouteMapSetLine {

   private static final long serialVersionUID = 1L;

   private Integer _asNum;
   private OriginType _originType;

   public RouteMapSetOriginTypeLine(OriginType originType, Integer asNum) {
      _originType = originType;
      _asNum = asNum;
   }

   public Integer getAsNum() {
      return _asNum;
   }

   public OriginType getOriginType() {
      return _originType;
   }

   @Override
   public RouteMapSetType getType() {
      return RouteMapSetType.ORIGIN_TYPE;
   }

   @Override
   public PolicyMapSetLine toPolicyMapSetLine(Configuration c) {
      return new PolicyMapSetOriginTypeLine(_originType);
   }

}
