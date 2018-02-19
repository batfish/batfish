package org.batfish.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Stream;
import org.batfish.common.util.ComparableStructure;

public class Directory extends ComparableStructure<String> {

  /** */
  private static final long serialVersionUID = 1L;

  private SortedSet<Directory> _directories;

  private SortedSet<String> _files;

  public Directory(Path path) {
    super(path.getFileName().toString());
    _directories = new TreeSet<>();
    _files = new TreeSet<>();
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
      for (Path subPath : stream) {
        String name = subPath.getFileName().toString();
        if (!name.startsWith(".")) {
          if (Files.isDirectory(subPath)) {
            Directory dir = new Directory(subPath);
            _directories.add(dir);
          } else if (Files.isRegularFile(subPath)) {
            _files.add(name);
          }
        }
      }
    } catch (IOException e) {
      throw new BatfishException("Could not iterate over path: " + path, e);
    }
  }

  @JsonCreator
  public Directory(@JsonProperty(PROP_NAME) String name) {
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
