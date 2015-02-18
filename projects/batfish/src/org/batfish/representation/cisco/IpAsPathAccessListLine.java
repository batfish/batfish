package org.batfish.representation.cisco;

import java.io.Serializable;
import java.util.List;

import org.batfish.representation.AsPathAccessList;
import org.batfish.representation.AsPathAccessListLine;
import org.batfish.representation.LineAction;
import org.batfish.util.SubRange;

public class IpAsPathAccessListLine implements Serializable {

   private static final long serialVersionUID = 1L;

   private LineAction _action;

   private SubRange _as1Range;

   private SubRange _as2Range;

   private boolean _atBeginning;

   private boolean _matchEmpty;

   public IpAsPathAccessListLine(LineAction action) {
      _action = action;
   }

   public void applyTo(AsPathAccessList newList) {
      List<AsPathAccessListLine> lines = newList.getLines();
      AsPathAccessListLine line = new AsPathAccessListLine();
      line.setAction(_action);
      line.setMatchEmpty(_matchEmpty);
      line.setAtBeginning(_atBeginning);
      line.setAs1Range(_as1Range);
      line.setAs2Range(_as2Range);
      lines.add(line);
   }

   public LineAction getAction() {
      return _action;
   }

   public void setAs1Range(SubRange as1Range) {
      _as1Range = as1Range;
   }

   public void setAs2Range(SubRange as2Range) {
      _as2Range = as2Range;
   }

   public void setAtBeginning(boolean atBeginning) {
      _atBeginning = atBeginning;
   }

   public void setMatchEmpty(boolean matchEmpty) {
      _matchEmpty = matchEmpty;
   }

}
