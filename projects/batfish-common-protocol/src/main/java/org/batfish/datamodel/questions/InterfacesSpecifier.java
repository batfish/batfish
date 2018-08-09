package org.batfish.datamodel.questions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;

/**
 * Enables specification of groups of interfaces in various questions.
 *
 * <p>Currently supported example specifiers:
 *
 * <ul>
 *   <li>Loopback.* -&gt; all interfaces with matching names
 *   <li>desc:KK.* -&gt; all interfaces whose description matches KK.*
 * </ul>
 *
 * <p>In the future, we might need other tags and boolean expressions over tags.
 */
public class InterfacesSpecifier {

  public enum Type {
    DESC,
    NAME,
    /** refers to {@link InterfaceType} */
    TYPE,
    VRF
  }

  public static InterfacesSpecifier ALL = new InterfacesSpecifier(".*");

  public static InterfacesSpecifier NONE = new InterfacesSpecifier("");

  private final String _expression;

  private final Pattern _regex;

  private final Type _type;

  @JsonCreator
  public InterfacesSpecifier(String expression) {
    _expression = expression;

    String[] parts = expression.split(":");

    if (parts.length == 1) {
      _type = Type.NAME;
      _regex = Pattern.compile(_expression, Pattern.CASE_INSENSITIVE);
    } else if (parts.length == 2) {
      try {
        _type = Type.valueOf(parts[0].toUpperCase());
        _regex = Pattern.compile(parts[1], Pattern.CASE_INSENSITIVE);
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException(
            "Illegal InterfacesSpecifier filter "
                + parts[0]
                + ".  Should be one of "
                + Arrays.stream(Type.values())
                    .map(Object::toString)
                    .collect(Collectors.joining(", ")));
      }
    } else {
      throw new IllegalArgumentException("Cannot parse InterfacesSpecifier " + expression);
    }
  }

  public InterfacesSpecifier(Pattern regex) {
    _expression = regex.toString();
    _type = Type.NAME;
    _regex = regex;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof InterfacesSpecifier)) {
      return false;
    }
    InterfacesSpecifier that = (InterfacesSpecifier) obj;
    return Objects.equals(_expression, that._expression)
        && Objects.equals(_regex.pattern(), that._regex.pattern())
        && Objects.equals(_type, that._type);
  }

  @JsonIgnore
  public Pattern getRegex() {
    return _regex;
  }

  @JsonIgnore
  public Type getType() {
    return _type;
  }

  public boolean matches(Interface iface) {
    switch (_type) {
      case DESC:
        return _regex.matcher(iface.getDescription()).matches();
      case NAME:
        return _regex.matcher(iface.getName()).matches();
      case TYPE:
        return _regex.matcher(iface.getInterfaceType().toString()).matches();
      case VRF:
        return _regex.matcher(iface.getVrfName()).matches();
      default:
        throw new BatfishException("Unhandled InterfacesSpecifier type: " + _type);
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(_expression, _regex.pattern(), _type.ordinal());
  }

  @Override
  @JsonValue
  public String toString() {
    return _expression;
  }
}
