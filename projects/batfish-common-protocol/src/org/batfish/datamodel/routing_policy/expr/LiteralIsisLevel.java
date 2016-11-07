package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.IsisLevel;
import org.batfish.datamodel.routing_policy.Environment;

public class LiteralIsisLevel implements IsisLevelExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private IsisLevel _level;

   public LiteralIsisLevel(IsisLevel level) {
      _level = level;
   }

   @Override
   public IsisLevel evaluate(Environment env) {
      return _level;
   }

   public IsisLevel getLevel() {
      return _level;
   }

   public void setLevel(IsisLevel level) {
      _level = level;
   }

}
