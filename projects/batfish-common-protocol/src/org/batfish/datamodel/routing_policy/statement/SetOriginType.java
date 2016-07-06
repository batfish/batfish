package org.batfish.datamodel.routing_policy.statement;

import org.batfish.datamodel.OriginType;

import com.fasterxml.jackson.annotation.JsonCreator;

public class SetOriginType extends AbstractStatement {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private OriginType _originType;

   @JsonCreator
   public SetOriginType() {
   }

   public SetOriginType(OriginType originType) {
      _originType = originType;
   }

   public OriginType getOriginType() {
      return _originType;
   }

   public void setOriginType(OriginType originType) {
      _originType = originType;
   }

}
