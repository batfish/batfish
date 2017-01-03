package org.batfish.datamodel;

import java.io.Serializable;
import java.util.SortedSet;
import java.util.TreeSet;

import org.batfish.common.util.ComparableStructure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

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
   @JsonPropertyDescription("The interfaces assigned to this OSPF area. Stored as @id")
   public SortedSet<Interface> getInterfaces() {
      return _interfaces;
   }

   @JsonProperty(INTERFACES_VAR)
   public void setInterfaces(SortedSet<Interface> interfaces) {
      _interfaces = interfaces;
   }

}
