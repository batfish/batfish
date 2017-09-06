package org.batfish.datamodel.pojo;

import com.google.common.base.MoreObjects;
import java.util.Objects;

/**
 * {@link TableObject TableObject} is an Object representation of an bgpTable/routingTable file.
 * {@link #_name} stores the name of the file, and {@link #_content} contains the file content.
 */
public class TableObject {
  private final String _name;
  private final String _content;

  public TableObject(String name, String content) {
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
    return MoreObjects.toStringHelper(TableObject.class)
        .add("name", _name)
        .add("content", _content)
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof TableObject)) {
      return false;
    }
    TableObject other = (TableObject) o;
    return Objects.equals(_name, other._name) && Objects.equals(_content, other._content);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_name, _content);
  }
}
