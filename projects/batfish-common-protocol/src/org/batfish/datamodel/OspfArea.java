package org.batfish.datamodel;

import java.io.Serializable;
import java.util.SortedSet;
import java.util.TreeSet;

import org.batfish.common.util.ComparableStructure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;

public class OspfArea extends ComparableStructure<Long>
      implements Serializable {

   private static final String INTERFACES_VAR = "interfaces";

   private static final long serialVersionUID = 1L;

   private SortedSet<Interface> _interfaces;

   @JsonCreator
   public OspfArea(@JsonProperty(NAME_VAR) Long number) {
      super(number);
      _interfaces = new TreeSet<>();
   }

   @JsonIdentityReference(alwaysAsId = true)
   @JsonProperty(INTERFACES_VAR)
   public SortedSet<Interface> getInterfaces() {
      return _interfaces;
   }

   @JsonProperty(NAME_VAR)
   public Long getNumber() {
      return _key;
   }

   @JsonProperty(INTERFACES_VAR)
   public void setInterfaces(SortedSet<Interface> interfaces) {
      _interfaces = interfaces;
   }

}
