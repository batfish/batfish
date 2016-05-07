package org.batfish.representation.cisco;

import java.io.Serializable;

public class RoutePolicyBooleanOr extends RoutePolicyBoolean {

   private static final long serialVersionUID = 1L;
   private RoutePolicyBoolean _left;
   private RoutePolicyBoolean _right;

   public RoutePolicyBooleanOr(RoutePolicyBoolean left, RoutePolicyBoolean right) {
   		_left = left;
   		_right = right;
   }

   public RoutePolicyBooleanType getType() { return RoutePolicyBooleanType.OR; }


   public RoutePolicyBoolean getLeft() { return _left; }
   public RoutePolicyBoolean getRight() { return _right; }

}
