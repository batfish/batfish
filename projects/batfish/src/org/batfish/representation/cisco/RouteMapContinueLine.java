package org.batfish.representation.cisco;

import java.io.Serializable;

public class RouteMapContinueLine implements Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private Integer _target;

   public RouteMapContinueLine(Integer target) {
      _target = target;
   }

   public Integer getTarget() {
      return _target;
   }

}
