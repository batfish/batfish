package org.batfish.datamodel.table;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;
import java.io.Serializable;
import java.util.Iterator;
import javax.annotation.Nonnull;

/** Represents data rows insider {@link TableAnswerElement} */
public class Rows implements Serializable {

  private final Multiset<Row> _data;

  public Rows() {
    _data = LinkedHashMultiset.create();
  }

  @VisibleForTesting
  public Rows(@Nonnull Multiset<Row> rows) {
    _data = ImmutableMultiset.copyOf(rows);
  }

  public Rows add(Row row) {
    _data.add(row);
    return this;
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
    return _data.toString();
  }

  // Jackson serializes a Multiset as a list of items.
  @JsonCreator
  private Rows(Iterable<Row> data) {
    _data = ImmutableMultiset.copyOf(data);
  }

  @JsonValue
  private Iterable<Row> asJsonValue() {
    return _data;
  }
}
