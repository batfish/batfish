package org.batfish.datamodel.routing_policy.expr;

public class RegexAsPathSetElem implements AsPathSetElem {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private String _regex;

   public RegexAsPathSetElem(String regex) {
      _regex = regex;
   }

   public String getRegex() {
      return _regex;
   }

   public void setRegex(String regex) {
      _regex = regex;
   }

}
