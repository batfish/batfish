package org.batfish.question.assertion;

import java.util.List;

public class Assertion {

   private List<Object> _args;

   private String _description;

   private String _text;

   public List<Object> getArgs() {
      return _args;
   }

   public String getDescription() {
      return _description;
   }

   public String getText() {
      return _text;
   }

   public void setArgs(List<Object> args) {
      _args = args;
   }

   public void setDescription(String description) {
      _description = description;
   }

   public void setText(String text) {
      _text = text;
   }

}
