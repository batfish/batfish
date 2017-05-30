package org.batfish.datamodel;

import org.batfish.common.util.ComparableStructure;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SnmpCommunity extends ComparableStructure<String> {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private String _accessList;

   private String _accessList6;

   private int _accessList6Line;

   private int _accessListLine;

   private boolean _ro;

   private boolean _rw;

   public SnmpCommunity(@JsonProperty(NAME_VAR) String name) {
      super(name);
   }

   public String getAccessList() {
      return _accessList;
   }

   public String getAccessList6() {
      return _accessList6;
   }

   @JsonIgnore
   public int getAccessList6Line() {
      return _accessList6Line;
   }

   @JsonIgnore
   public int getAccessListLine() {
      return _accessListLine;
   }

   public boolean getRo() {
      return _ro;
   }

   public boolean getRw() {
      return _rw;
   }

   public void setAccessList(String accessList) {
      _accessList = accessList;
   }

   public void setAccessList6(String accessList6) {
      _accessList6 = accessList6;
   }

   @JsonIgnore
   public void setAccessList6Line(int accessList6Line) {
      _accessList6Line = accessList6Line;
   }

   @JsonIgnore
   public void setAccessListLine(int accessListLine) {
      _accessListLine = accessListLine;
   }

   public void setRo(boolean ro) {
      _ro = ro;
   }

   public void setRw(boolean rw) {
      _rw = rw;
   }

}
