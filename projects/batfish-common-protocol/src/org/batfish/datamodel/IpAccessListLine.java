package org.batfish.datamodel;

public final class IpAccessListLine extends HeaderSpace {

   private static final long serialVersionUID = 1L;

   private LineAction _action;

   private HeaderSpace _headerSpace;

   public HeaderSpace getHeaderSpace() {
      return _headerSpace;
   }

   public void setHeaderSpace(HeaderSpace headerSpace) {
      _headerSpace = headerSpace;
   }

   private String _name;

   public IpAccessListLine() {
      _headerSpace = new HeaderSpace();
   }

   public LineAction getAction() {
      return _action;
   }

   public void setName(String name) {
      _name = name;
   }

   public String getName() {
      return _name;
   }

   public void setAction(LineAction action) {
      _action = action;
   }

   @Override
   public String toString() {
      return "[Action:" + _action + ", Base: " + _headerSpace.toString() + "]";
   }
}
