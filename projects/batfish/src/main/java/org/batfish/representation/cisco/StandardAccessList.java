package org.batfish.representation.cisco;

import java.util.ArrayList;
import java.util.List;

import org.batfish.common.util.ComparableStructure;

public class StandardAccessList extends ComparableStructure<String> {

   private static final long serialVersionUID = 1L;

   private final int _definitionLine;

   private List<StandardAccessListLine> _lines;

   public StandardAccessList(String id, int definitionLine) {
      super(id);
      _definitionLine = definitionLine;
      _lines = new ArrayList<>();
   }

   public void addLine(StandardAccessListLine all) {
      _lines.add(all);
   }

   public int getDefinitionLine() {
      return _definitionLine;
   }

   public List<StandardAccessListLine> getLines() {
      return _lines;
   }

   public ExtendedAccessList toExtendedAccessList() {
      ExtendedAccessList eal = new ExtendedAccessList(_key, _definitionLine);
      eal.setParent(this);
      eal.getLines().clear();
      for (StandardAccessListLine sall : _lines) {
         eal.addLine(sall.toExtendedAccessListLine());
      }
      return eal;
   }

}
