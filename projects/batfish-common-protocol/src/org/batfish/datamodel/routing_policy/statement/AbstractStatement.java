package org.batfish.datamodel.routing_policy.statement;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class AbstractStatement implements Statement {

   private static final String COMMENT_VAR = "comment";

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private String _comment;

   @JsonProperty(COMMENT_VAR)
   public final String getComment() {
      return _comment;
   }

   @JsonProperty(COMMENT_VAR)
   public final void setComment(String comment) {
      _comment = comment;
   }

   @Override
   public List<Statement> simplify() {
      return Collections.singletonList(this);
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
