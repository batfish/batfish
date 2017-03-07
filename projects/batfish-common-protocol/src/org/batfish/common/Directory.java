package org.batfish.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.SortedSet;
import java.util.TreeSet;

import org.batfish.common.util.ComparableStructure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Directory extends ComparableStructure<String> {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private SortedSet<Directory> _directories;

   private SortedSet<String> _files;

   public Directory(Path path) {
      super(path.getFileName().toString());
      _directories = new TreeSet<>();
      _files = new TreeSet<>();
      try {
         for (Path subPath : Files.newDirectoryStream(path)) {
            String name = subPath.getFileName().toString();
            if (!name.startsWith(".")) {
               if (Files.isDirectory(subPath)) {
                  Directory dir = new Directory(subPath);
                  _directories.add(dir);
               }
               else if (Files.isRegularFile(subPath)) {
                  _files.add(name);
               }
            }
         }
      }
      catch (IOException e) {
         throw new BatfishException("Could not iterate over path: " + path, e);
      }
   }

   @JsonCreator
   public Directory(@JsonProperty(NAME_VAR) String name) {
      super(name);
      _directories = new TreeSet<>();
      _files = new TreeSet<>();
   }

   public SortedSet<Directory> getDirectories() {
      return _directories;
   }

   public SortedSet<String> getFiles() {
      return _files;
   }

   public void setDirectories(SortedSet<Directory> directories) {
      _directories = directories;
   }

   public void setFiles(SortedSet<String> files) {
      _files = files;
   }

}
