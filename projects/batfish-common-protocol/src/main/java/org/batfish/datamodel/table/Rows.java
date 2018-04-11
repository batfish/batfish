package org.batfish.datamodel.table;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.HashMultiset;

/** Represents data rows insider {@link TableAnswerElement} */
public class Rows {

  private static final long serialVersionUID = 1L;

  private HashMultiset<ObjectNode> _data;

  public Rows() {
    this(null);
  }

  @JsonCreator
  public Rows(HashMultiset<ObjectNode> data) {
    _data = data == null ? HashMultiset.create() : data;
  }

  public void add(ObjectNode row) {
    _data.add(row);
  }

  public boolean contains(ObjectNode row) {
    return _data.contains(row);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Rows)) {
      return false;
    }
    return _data.equals(((Rows) o)._data);
  }

  @JsonValue
  private HashMultiset<ObjectNode> getData() {
    return _data;
  }

  @Override
  public int hashCode() {
    return _data.hashCode();
  }

  public int size() {
    return _data.size();
  }
}
