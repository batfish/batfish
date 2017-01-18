package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.routing_policy.Environment;

import com.fasterxml.jackson.annotation.JsonCreator;

public class LiteralLong extends LongExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private long _value;

   @JsonCreator
   private LiteralLong() {
   }

   public LiteralLong(long value) {
      _value = value;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      if (obj == null) {
         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      LiteralLong other = (LiteralLong) obj;
      if (_value != other._value) {
         return false;
      }
      return true;
   }

   @Override
   public long evaluate(Environment environment) {
      return _value;
   }

   public long getValue() {
      return _value;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + (int) (_value ^ (_value >>> 32));
      return result;
   }

   public void setValue(long value) {
      _value = value;
   }

}
