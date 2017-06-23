package org.batfish.datamodel.routing_policy.expr;

import java.io.Serializable;

import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
public abstract class BooleanExpr implements Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private String _comment;

   @Override
   public abstract boolean equals(Object obj);

   public abstract Result evaluate(Environment environment);

   public String getComment() {
      return _comment;
   }

   @Override
   public abstract int hashCode();

   public void setComment(String comment) {
      _comment = comment;
   }

   public BooleanExpr simplify() {
      return this;
   }

   @Override
   public String toString() {
      if (_comment != null) {
         return getClass().getSimpleName() + "<" + _comment + ">";
      }
      else {
         return super.toString();
      }
   }

}
