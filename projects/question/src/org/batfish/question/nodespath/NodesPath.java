package org.batfish.question.nodespath;

public class NodesPath {

   private String _description;

   private String _path;

   private boolean _suffix;

   private boolean _summary;

   public String getDescription() {
      return _description;
   }

   public String getPath() {
      return _path;
   }

   public boolean getSuffix() {
      return _suffix;
   }

   public boolean getSummary() {
      return _summary;
   }

   public void setDescription(String description) {
      _description = description;
   }

   public void setPath(String path) {
      _path = path;
   }

   public void setSuffix(boolean suffix) {
      _suffix = suffix;
   }

   public void setSummary(boolean summary) {
      _summary = summary;
   }

}
