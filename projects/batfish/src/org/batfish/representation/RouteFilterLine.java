package org.batfish.representation;

import java.io.Serializable;

import org.batfish.util.SubRange;

public class RouteFilterLine implements Serializable {

   private static final long serialVersionUID = 1L;

   private LineAction _action;

   private SubRange _lengthRange;

   private Prefix _prefix;

   public RouteFilterLine(LineAction action, Prefix prefix, SubRange lengthRange) {
      _action = action;
      _prefix = prefix;
      _lengthRange = lengthRange;
   }

   public LineAction getAction() {
      return _action;
   }

   public SubRange getLengthRange() {
      return _lengthRange;
   }

   public Prefix getPrefix() {
      return _prefix;
   }

}
