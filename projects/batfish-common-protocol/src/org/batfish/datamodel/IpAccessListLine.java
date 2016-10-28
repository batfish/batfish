package org.batfish.datamodel;

public final class IpAccessListLine extends HeaderSpace {

   private static final long serialVersionUID = 1L;

   private LineAction _action;

   private String _name;

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      IpAccessListLine other = (IpAccessListLine) obj;
      if (!super.equals(obj)) {
         return false;
      }
      if (_action != other._action) {
         return false;
      }
      return true;
   }

   public LineAction getAction() {
      return _action;
   }

   public String getName() {
      return _name;
   }

   public void setAction(LineAction action) {
      _action = action;
   }

   public void setName(String name) {
      _name = name;
   }

   @Override
   public String toString() {
      return "[Action:" + _action + ", Base: " + super.toString() + "]";
   }

}
