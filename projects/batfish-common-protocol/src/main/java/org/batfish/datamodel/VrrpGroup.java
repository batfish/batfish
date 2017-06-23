package org.batfish.datamodel;

import org.batfish.common.util.ComparableStructure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class VrrpGroup extends ComparableStructure<Integer> {

   private static final String PREEMPT_VAR = "preempt";

   private static final String PRIORITY_VAR = "priority";

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private static final String VIRTUAL_ADDRESS_VAR = "virtualAddress";

   private boolean _preempt;

   private int _priority;

   private Prefix _virtualAddress;

   @JsonCreator
   public VrrpGroup(@JsonProperty(NAME_VAR) Integer name) {
      super(name);
   }

   @JsonProperty(PREEMPT_VAR)
   public boolean getPreempt() {
      return _preempt;
   }

   @JsonProperty(PRIORITY_VAR)
   public int getPriority() {
      return _priority;
   }

   @JsonProperty(VIRTUAL_ADDRESS_VAR)
   public Prefix getVirtualAddress() {
      return _virtualAddress;
   }

   @JsonProperty(PREEMPT_VAR)
   public void setPreempt(boolean preempt) {
      _preempt = preempt;
   }

   @JsonProperty(PRIORITY_VAR)
   public void setPriority(int priority) {
      _priority = priority;
   }

   @JsonProperty(VIRTUAL_ADDRESS_VAR)
   public void setVirtualAddress(Prefix virtualAddress) {
      _virtualAddress = virtualAddress;
   }

}
