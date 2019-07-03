package org.batfish.datamodel.table;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.TreeMultiset;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Objects;

/** Represents data rows insider {@link TableAnswerElement} */
public class Rows implements Serializable {

  private Multiset<Row> _data;

  public Rows() {
    this(null);
  }

  @JsonCreator
  public Rows(Multiset<Row> data) {
    _data = data == null ? TreeMultiset.create() : data;
  }

  public Rows add(Row row) {
    _data.add(row);
    return this;
  }

  public boolean contains(Row row) {
    return _data.contains(row);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Rows)) {
      return false;
    }
    return _data.equals(((Rows) o)._data);
  }

  /**
   * Returns an immutable copy of the rows in this object
   *
   * @return An ImmutableMultiset
   */
  @JsonValue
  public Multiset<Row> getData() {
    return ImmutableMultiset.copyOf(_data);
  }

  public Iterator<Row> iterator() {
    return _data.iterator();
  }

  @Override
  public int hashCode() {
    return _data.hashCode();
  }

  public int size() {
    return _data.size();
  }

  @Override
  public String toString() {
    return Objects.toString(_data);
  }
}
