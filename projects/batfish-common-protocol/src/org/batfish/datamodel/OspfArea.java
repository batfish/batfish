package org.batfish.datamodel;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIdentityReference;

public class OspfArea implements Serializable {

   private static final long serialVersionUID = 1L;

   private Set<Interface> _interfaces;
   private long _number;

   public OspfArea(long areaNum) {
      _interfaces = new LinkedHashSet<Interface>();
      _number = areaNum;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public Set<Interface> getInterfaces() {
      return _interfaces;
   }

   public long getNumber() {
      return _number;
   }

}
