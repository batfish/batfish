package org.batfish.z3;

import java.util.Objects;

public class Field {
  private final String _name;
  private final int _size;

  public Field(String name, int size) {
    _name = name;
    _size = size;
  }

  public String getName() {
    return _name;
  }

  public int getSize() {
    return _size;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Field)) {
      return false;
    }
    Field field = (Field) other;
    return _name.equals(field._name) && _size == field._size;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_name, _size);
  }
}
