package org.batfish.datamodel;

import java.io.Serializable;
import java.util.SortedSet;
import java.util.TreeSet;

import org.batfish.common.util.ComparableStructure;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;

public class OspfArea extends ComparableStructure<Long> implements Serializable {

   private static final String INTERFACES_VAR = "interfaces";

   private static final long serialVersionUID = 1L;

   private final SortedSet<Interface> _interfaces;

   public OspfArea(long number) {
      super(number);
      _interfaces = new TreeSet<Interface>();
   }

   public OspfArea(@JsonProperty(NAME_VAR)Long number, @JsonProperty(INTERFACES_VAR) SortedSet<Interface> interfaces) {
      super(number);
      _interfaces = interfaces;
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

}
