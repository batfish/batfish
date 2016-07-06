package org.batfish.datamodel.routing_policy.statement;

import java.util.List;

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

   public List<Integer> getAsList() {
      return _asList;
   }

   public void setAsList(List<Integer> asList) {
      _asList = asList;
   }

}
