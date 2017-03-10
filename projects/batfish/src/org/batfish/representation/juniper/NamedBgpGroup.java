package org.batfish.representation.juniper;

public class NamedBgpGroup extends BgpGroup {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final int _definitionLine;

   public NamedBgpGroup(String name, int definitionLine) {
      _groupName = name;
      _definitionLine = definitionLine;
   }

   public int getDefinitionLine() {
      return _definitionLine;
   }

   public boolean getInherited() {
      return _inherited;
   }

   public String getName() {
      return _groupName;
   }

}
