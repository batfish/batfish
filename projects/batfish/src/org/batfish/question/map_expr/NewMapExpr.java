package org.batfish.question.map_expr;

import org.batfish.question.Environment;
import org.batfish.question.QMap;

public final class NewMapExpr extends BaseMapExpr {

   @Override
   public QMap evaluate(Environment environment) {
      return new QMap();
   }

}
