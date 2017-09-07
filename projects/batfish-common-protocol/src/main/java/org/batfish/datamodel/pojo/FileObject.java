package org.batfish.datamodel.pojo;

import com.google.common.base.MoreObjects;
import java.util.Objects;

/**
 * {@link FileObject FileObject} is an Object representation of an actual file. {@link #_name}
 * stores the name of the file, and {@link #_content} contains the file content.
 */
public class FileObject {
  private final String _name;
  private final String _content;

  public FileObject(String name, String content) {
    this._name = name;
    this._content = content;
  }

  public String getName() {
    return _name;
  }

  public String getContent() {
    return _content;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(FileObject.class)
        .add("name", _name)
        .add("content", _content)
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof FileObject)) {
      return false;
    }
    FileObject other = (FileObject) o;
    return Objects.equals(_name, other._name) && Objects.equals(_content, other._content);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_name, _content);
  }
}
