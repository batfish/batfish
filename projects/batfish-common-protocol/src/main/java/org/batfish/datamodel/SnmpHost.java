package org.batfish.datamodel;

import org.batfish.common.util.ComparableStructure;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SnmpHost extends ComparableStructure<String> {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   public SnmpHost(@JsonProperty(NAME_VAR) String name) {
      super(name);
   }

}
