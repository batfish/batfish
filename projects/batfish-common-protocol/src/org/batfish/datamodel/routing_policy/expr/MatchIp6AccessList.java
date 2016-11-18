package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.Ip6AccessList;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

import com.fasterxml.jackson.annotation.JsonCreator;

public class MatchIp6AccessList extends AbstractBooleanExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private String _list;

   @JsonCreator
   public MatchIp6AccessList() {
   }

   public MatchIp6AccessList(String list) {
      _list = list;
   }

   @Override
   public Result evaluate(Environment environment) {
      Result result = new Result();
      Ip6AccessList list = environment.getConfiguration().getIp6AccessLists()
            .get(_list);
      if (list != null) {
         // TODO
      }
      else {
         environment.setError(true);
         result.setBooleanValue(false);
         return result;
      }
      throw new UnsupportedOperationException(
            "no implementation for generated method");
   }

   public String getList() {
      return _list;
   }

   public void setList(String list) {
      _list = list;
   }

}
