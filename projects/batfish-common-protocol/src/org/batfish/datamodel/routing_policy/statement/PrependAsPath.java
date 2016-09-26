package org.batfish.datamodel.routing_policy.statement;

import java.util.List;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

import com.fasterxml.jackson.annotation.JsonCreator;

public class PrependAsPath extends AbstractStatement {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private List<Integer> _asList;

   @JsonCreator
   public PrependAsPath() {
   }

   public PrependAsPath(List<Integer> asList) {
      _asList = asList;
   }

   @Override
   public Result execute(Environment environment) {
      throw new BatfishException("unimplemented");
   }

   public List<Integer> getAsList() {
      return _asList;
   }

   public void setAsList(List<Integer> asList) {
      _asList = asList;
   }

}
