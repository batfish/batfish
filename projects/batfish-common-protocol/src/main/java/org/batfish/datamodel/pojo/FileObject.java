package org.batfish.datamodel.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.Objects;

/**
 * {@link FileObject FileObject} is an Object representation of an actual file. {@link #_name}
 * stores the name of the file, and {@link #_content} contains the file content.
 */
public class FileObject {

  private static final String PROP_NAME = "name";
  private static final String PROP_CONTENT = "content";

  private final String _name;
  private final String _content;

  @JsonCreator
  public FileObject(@JsonProperty(PROP_NAME) String name, @JsonProperty(PROP_CONTENT) String content) {
    this._name = name;
    this._content = content;
  }

  @JsonProperty(PROP_NAME)
  public String getName() {
    return _name;
  }

  @JsonProperty(PROP_CONTENT)
  public String getContent() {
    return _content;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(FileObject.class)
        .add(PROP_NAME, _name)
        .add(PROP_CONTENT, _content)
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
