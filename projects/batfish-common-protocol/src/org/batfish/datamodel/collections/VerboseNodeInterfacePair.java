package org.batfish.datamodel.collections;

import org.batfish.common.Pair;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class VerboseNodeInterfacePair extends Pair<Configuration, Interface> {

   private static final String HOST_VAR = "host";

   private static final String INTERFACE_VAR = "interface";
   /**
    *
    */
   private static final long serialVersionUID = 1L;

   @JsonCreator
   public VerboseNodeInterfacePair(@JsonProperty(HOST_VAR) Configuration node,
         @JsonProperty(INTERFACE_VAR) Interface iface) {
      super(node, iface);
   }

   @JsonProperty(HOST_VAR)
   public Configuration getHost() {
      return _first;
   }

   @JsonProperty(INTERFACE_VAR)
   public Interface getInterface() {
      return _second;
   }

   @Override
   public String toString() {
      return _first + ":" + _second;
   }

}
