package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.IsisLevel;
import org.batfish.datamodel.routing_policy.Environment;

public class VarIsisLevel implements IsisLevelExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private String _var;

   public VarIsisLevel(String var) {
      _var = var;
   }

   @Override
   public IsisLevel evaluate(Environment env) {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

   public String getVar() {
      return _var;
   }

   public void setVar(String var) {
      _var = var;
   }

}
