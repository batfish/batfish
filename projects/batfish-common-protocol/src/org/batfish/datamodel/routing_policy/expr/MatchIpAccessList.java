package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

import com.fasterxml.jackson.annotation.JsonCreator;

public class MatchIpAccessList extends AbstractBooleanExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private String _list;

   @JsonCreator
   public MatchIpAccessList() {
   }

   public MatchIpAccessList(String list) {
      _list = list;
   }

   @Override
   public Result evaluate(Environment environment, Route route) {
      Result result = new Result();
      IpAccessList list = environment.getConfiguration().getIpAccessLists()
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
