package org.batfish.question.assertion;

import java.util.List;

public class Assertion {

   private List<Object> _args;

   private Check _check;

   private String _description;

   private String _path;

   public List<Object> getArgs() {
      return _args;
   }

   public Check getCheck() {
      return _check;
   }

   public String getDescription() {
      return _description;
   }

   public String getPath() {
      return _path;
   }

   public void setArgs(List<Object> args) {
      _args = args;
   }

   public void setCheck(Check check) {
      _check = check;
   }

   public void setDescription(String description) {
      _description = description;
   }

   public void setPath(String path) {
      _path = path;
   }

}
