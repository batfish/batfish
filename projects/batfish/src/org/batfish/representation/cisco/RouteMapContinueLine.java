package org.batfish.representation.cisco;

import java.io.Serializable;

public class RouteMapContinueLine implements Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private int _target;

   public RouteMapContinueLine(int target) {
      _target = target;
   }

   public int getTarget() {
      return _target;
   }

}
