package org.batfish.representation;

import java.io.Serializable;

import org.batfish.util.SubRange;

public final class AsPathAccessListLine implements Serializable {

   private static final long serialVersionUID = 1L;

   private LineAction _action;

   private SubRange _as1Range;

   private SubRange _as2Range;

   private boolean _atBeginning;

   private boolean _matchEmpty;

   public LineAction getAction() {
      return _action;
   }

   public SubRange getAs1Range() {
      return _as1Range;
   }

   public SubRange getAs2Range() {
      return _as2Range;
   }

   public boolean getAtBeginning() {
      return _atBeginning;
   }

   public boolean getMatchEmpty() {
      return _matchEmpty;
   }

   public void setAction(LineAction action) {
      _action = action;
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
