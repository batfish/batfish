package org.batfish.representation.juniper;

public class NamedBgpGroup extends BgpGroup {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private String _name;

   public NamedBgpGroup(String name) {
      _name = name;
   }

   public String getName() {
      return _name;
   }

}
