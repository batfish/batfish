package org.batfish.question.map_expr;

import org.batfish.common.BatfishException;
import org.batfish.question.Environment;
import org.batfish.question.QMap;

public enum BaseCaseMapExpr implements MapExpr {
   QUERY;

   @Override
   public QMap evaluate(Environment environment) {
      switch (this) {
      case QUERY:
         QMap query = environment.getQuery();
         return query;

      default:
         throw new BatfishException("invalid "
               + this.getClass().getSimpleName());

      }
   }

   @Override
   public String print(Environment environment) {
      return BaseMapExpr.print(this, environment);
   }

}
