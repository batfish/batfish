package org.batfish.logicblox;

import java.io.File;

public class ProjectFile extends File {

   private static final long serialVersionUID = 1L;

   private String _pathname;

   public ProjectFile(String pathname) {
      super(pathname);
      _pathname = pathname;
   }

   @Override
   public File getAbsoluteFile() {
      return this;
   }

   @Override
   public String getAbsolutePath() {
      return _pathname;
   }

   @Override
   public File getCanonicalFile() {
      return this;
   }

   @Override
   public String getCanonicalPath() {
      return _pathname;
   }

}
