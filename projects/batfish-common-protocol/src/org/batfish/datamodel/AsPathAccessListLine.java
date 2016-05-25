package org.batfish.datamodel;

import java.io.Serializable;

public final class AsPathAccessListLine implements Serializable, Comparable<AsPathAccessListLine> {

   private static final long serialVersionUID = 1L;

   private LineAction _action;

   private SubRange _as1Range;

   private SubRange _as2Range;

   private boolean _atBeginning;

   private boolean _matchEmpty;

   @Override
   public int compareTo(AsPathAccessListLine  rhs) {
      int ret = rhs._as1Range.compareTo(this._as1Range);
      if (ret == 0) {
         ret = rhs._as2Range.compareTo(this._as2Range);
      }
      if (ret == 0) {
         ret = Boolean.compare(rhs._atBeginning, this._atBeginning);
      }
      return ret;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      AsPathAccessListLine other = (AsPathAccessListLine) obj;
      if (other._as1Range != _as1Range) {
         return false;
      }
      if (other._as2Range != _as2Range) {
         return false;
      }
      return true;
   }
   
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
