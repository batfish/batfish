package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;

public class RegexAsPathSetElem extends AsPathSetElem {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private String _regex;

   @JsonCreator
   private RegexAsPathSetElem() {
   }

   public RegexAsPathSetElem(String regex) {
      _regex = regex;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      if (obj == null) {
         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      RegexAsPathSetElem other = (RegexAsPathSetElem) obj;
      if (_regex == null) {
         if (other._regex != null) {
            return false;
         }
      }
      else if (!_regex.equals(other._regex)) {
         return false;
      }
      return true;
   }

   public String getRegex() {
      return _regex;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((_regex == null) ? 0 : _regex.hashCode());
      return result;
   }

   @Override
   public String regex() {
      return _regex;
   }

   public void setRegex(String regex) {
      _regex = regex;
   }

}
