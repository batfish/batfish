package org.batfish.datamodel;

import java.util.List;

import org.batfish.datamodel.routing_policy.expr.AsExpr;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PolicyMapSetAsPathPrependLine extends PolicyMapSetLine {

   private static final String LIST_VAR = "list";

   private static final long serialVersionUID = 1L;

   private final List<AsExpr> _asList;

   @JsonCreator
   public PolicyMapSetAsPathPrependLine(
         @JsonProperty(LIST_VAR) List<AsExpr> asList) {
      _asList = asList;
   }

   @JsonProperty(LIST_VAR)
   public List<AsExpr> getAsList() {
      return _asList;
   }

   @Override
   public PolicyMapSetType getType() {
      return PolicyMapSetType.AS_PATH_PREPEND;
   }

}
