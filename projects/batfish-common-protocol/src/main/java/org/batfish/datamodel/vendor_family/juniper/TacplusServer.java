package org.batfish.datamodel.vendor_family.juniper;

import org.batfish.common.util.ComparableStructure;
import org.batfish.datamodel.Ip;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TacplusServer extends ComparableStructure<String> {

   private String _secret;

   private Ip _sourceAddress;

   public Ip getSourceAddress() {
      return _sourceAddress;
   }

   @JsonCreator
   public TacplusServer(@JsonProperty(NAME_VAR) String name) {
      super(name);
   }

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   public void setSecret(String secret) {
      _secret = secret;
   }

   public String getSecret() {
      return _secret;
   }

   public void setSourceAddress(Ip sourceAddress) {
      _sourceAddress = sourceAddress;
   }

}
