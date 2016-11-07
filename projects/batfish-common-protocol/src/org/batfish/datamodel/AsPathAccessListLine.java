package org.batfish.datamodel;

import java.io.Serializable;

public final class AsPathAccessListLine
      implements Serializable, Comparable<AsPathAccessListLine> {

   private static final long serialVersionUID = 1L;

   private LineAction _action;

   private String _regex;

   @Override
   public int compareTo(AsPathAccessListLine rhs) {
      int ret = _regex.compareTo(rhs._regex);
      return ret;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      AsPathAccessListLine other = (AsPathAccessListLine) obj;
      if (_action != other._action) {
         return false;
      }
      if (!_regex.equals(other._regex)) {
         return false;
      }
      return true;
   }

   public LineAction getAction() {
      return _action;
   }

   public String getRegex() {
      return _regex;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + _action.hashCode();
      result = prime * result + _regex.hashCode();
      return result;
   }

   public void setAction(LineAction action) {
      _action = action;
   }

   public void setRegex(String regex) {
      _regex = regex;
   }

}
