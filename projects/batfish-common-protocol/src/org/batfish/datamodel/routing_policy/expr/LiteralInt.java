package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.routing_policy.Environment;

import com.fasterxml.jackson.annotation.JsonCreator;

public class LiteralInt implements IntExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private int _value;

   @JsonCreator
   public LiteralInt() {
   }

   public LiteralInt(int value) {
      _value = value;
   }

   @Override
   public int evaluate(Environment environment) {
      return _value;
   }

   public int getValue() {
      return _value;
   }

   public void setValue(int value) {
      _value = value;
   }

}
