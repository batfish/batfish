package org.batfish.common.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.io.Serializable;
import org.batfish.common.BatfishException;

public abstract class ComparableStructure<KeyT extends Comparable<? super KeyT>>
    implements Comparable<ComparableStructure<? extends KeyT>>, Serializable {

  protected static final String PROP_NAME = "name";
  private static final long serialVersionUID = 1L;
  protected KeyT _key;

  @JsonCreator
  public ComparableStructure(@JsonProperty(PROP_NAME) KeyT name) {
    _key = name;
  }

  @Override
  public int compareTo(ComparableStructure<? extends KeyT> rhs) {
    return _key.compareTo(rhs._key);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof ComparableStructure)) {
      return false;
    }
    ComparableStructure<?> rhs = (ComparableStructure<?>) o;
    if (rhs._key.getClass().equals(_key.getClass())) {
      return _key.equals(rhs._key);
    } else {
      throw new BatfishException("Keys are of incompatible types");
    }
  }

  @JsonProperty(PROP_NAME)
  @JsonPropertyDescription("The name of this structure")
  public KeyT getName() {
    return _key;
  }

  @Override
  public int hashCode() {
    return _key.hashCode();
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "<" + _key + ">";
  }
}
