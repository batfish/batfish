package org.batfish.symbolic;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Representation of a community variable for the symbolic encoding. Configuration languages allow
 * users match community values using either <b>exact matches</b> or <b>regular expression</b>
 * matches. For example, a regular expression match such as .*:65001 will match any community string
 * that ends with 65001.
 *
 * <p>To encode community semantics, the model introduces a single new boolean variable for every
 * exact match, and two new boolean variables for every regex match. The first variable says whether
 * there is a community value that matches the regex, but is not specified in the configuration
 * (e.g., came from a neighbor). The second variable says if the regex match is successful, which is
 * based on both the communities in the configuration as well as other communities possibly sent by
 * neighbors.
 *
 * @author Ryan Beckett
 */
public class CommunityVar implements Comparable<CommunityVar> {

  public enum Type {
    EXACT,
    REGEX,
    OTHER
  }

  private Type _type;

  private String _value;

  private Long _long;

  public CommunityVar(Type type, String value, @Nullable Long l) {
    _type = type;
    _value = value;
    _long = l;
  }

  public Type getType() {
    return _type;
  }

  public String getValue() {
    return _value;
  }

  public Long asLong() {
    return _long;
  }

  @Override
  public String toString() {
    String val = '<' + _value + ',' + _type + '>';
    String typ;
    switch (_type) {
      case EXACT:
        typ = "Exact";
        break;
      case REGEX:
        typ = "Regex";
        break;
      case OTHER:
        typ = "Other";
        break;
      default:
        typ = "Exact";
    }
    return typ + val;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof CommunityVar)) {
      return false;
    }
    CommunityVar other = (CommunityVar) o;
    return Objects.equals(_value, other._value) && _type == other._type;
  }

  @Override
  public int hashCode() {
    int result = _type != null ? _type.ordinal() : 0;
    result = 31 * result + (_value != null ? _value.hashCode() : 0);
    return result;
  }

  @Override
  public int compareTo(@Nonnull CommunityVar that) {
    if (this._type.compareTo(that._type) < 0) {
      return -1;
    } else if (this._type.compareTo(that._type) > 0) {
      return 1;
    }

    if (this._value.compareTo(that._value) < 0) {
      return -1;
    } else if (this._value.compareTo(that._value) > 0) {
      return 1;
    }
    return 0;
  }
}
