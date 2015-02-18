package org.batfish.representation;

import org.batfish.util.SubRange;

public class RouteFilterLengthRangeLine extends RouteFilterLine {

   private static final long serialVersionUID = 1L;

   private SubRange _lengthRange;

   private Prefix _prefix;

   public RouteFilterLengthRangeLine(LineAction action, Prefix prefix,
         SubRange lengthRange) {
      super(action);
      _prefix = prefix;
      _lengthRange = lengthRange;
   }

   public SubRange getLengthRange() {
      return _lengthRange;
   }

   public Prefix getPrefix() {
      return _prefix;
   }

   @Override
   public RouteFilterLineType getType() {
      return RouteFilterLineType.LENGTH_RANGE;
   }

}
