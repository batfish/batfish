package org.batfish.datamodel.routing_policy.statement;

import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.expr.IntExpr;

import com.fasterxml.jackson.annotation.JsonCreator;

public class SetTag extends Statement {

   /**
   *
   */
   private static final long serialVersionUID = 1L;

   private IntExpr _tag;

   @JsonCreator
   private SetTag() {
   }

   public SetTag(IntExpr expr) {
      _tag = expr;
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
      SetTag other = (SetTag) obj;
      if (_tag == null) {
         if (other._tag != null) {
            return false;
         }
      }
      else if (!_tag.equals(other._tag)) {
         return false;
      }
      return true;
   }

   @Override
   public Result execute(Environment environment) {
      Result result = new Result();
      int tag = _tag.evaluate(environment);
      environment.getOutputRoute().setTag(tag);
      if (environment.getWriteToIntermediateBgpAttributes()) {
         environment.getIntermediateBgpAttributes().setTag(tag);
      }
      return result;
   }

   public IntExpr getTag() {
      return _tag;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((_tag == null) ? 0 : _tag.hashCode());
      return result;
   }

   public void setTag(IntExpr tag) {
      _tag = tag;
   }

}
