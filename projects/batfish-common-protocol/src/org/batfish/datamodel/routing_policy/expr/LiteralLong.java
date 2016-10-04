package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.routing_policy.Environment;

import com.fasterxml.jackson.annotation.JsonCreator;

public class LiteralLong implements LongExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private long _value;

   @JsonCreator
   public LiteralLong() {
   }

   public LiteralLong(long value) {
      _value = value;
   }

   @Override
   public long evaluate(Environment environment) {
      return _value;
   }

   public long getValue() {
      return _value;
   }

   public void setValue(long value) {
      _value = value;
   }

}
