package org.batfish.datamodel.routing_policy.expr;

import java.util.ArrayList;
import java.util.List;

import org.batfish.datamodel.routing_policy.Environment;

import com.fasterxml.jackson.annotation.JsonCreator;

public class LiteralAsList implements AsPathListExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private List<AsExpr> _list;

   @JsonCreator
   public LiteralAsList() {
   }

   public LiteralAsList(List<AsExpr> list) {
      _list = list;
   }

   @Override
   public List<Integer> evaluate(Environment environment) {
      List<Integer> list = new ArrayList<>();
      for (AsExpr expr : _list) {
         int as = expr.evaluate(environment);
         list.add(as);
      }
      return list;
   }

   public List<AsExpr> getList() {
      return _list;
   }

   public void setList(List<AsExpr> list) {
      _list = list;
   }

}
