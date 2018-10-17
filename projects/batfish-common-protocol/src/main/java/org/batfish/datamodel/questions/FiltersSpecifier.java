package org.batfish.datamodel.questions;

import static org.apache.commons.lang3.ObjectUtils.firstNonNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip6AccessList;
import org.batfish.datamodel.IpAccessList;

/**
 * Enables specification of groups of filters in various questions.
 *
 * <p>Currently supported example specifiers:
 *
 * <ul>
 *   <li>lhr-.* -&gt; all filters with matching names
 *   <li>name:lhr-.* -&gt; same as above; name: is optional
 *   <li>ipv4:lhr-.* all IPv4 access lists with matching names
 *   <li>ipv6:lhr-.* all IPv6 access lists with matching names
 * </ul>
 *
 * <p>In the future, we might need other tags (e.g., loc:) and boolean expressions (e.g., role:srv.*
 * AND lhr-* for all servers with matching names)
 */
public class FiltersSpecifier {

  public enum Type {
    INPUTFILTERON,
    IPV4,
    IPV6,
    NAME,
    OUTPUTFILTERON
  }

  public static final FiltersSpecifier ALL = new FiltersSpecifier(".*");

  public static final FiltersSpecifier NONE = new FiltersSpecifier("");

  private final String _expression;

  private final Pattern _regex;

  private final Type _type;

  public FiltersSpecifier(String expression) {
    _expression = expression;

    String upperExpression = expression.toUpperCase();
    Type parsedType = null;
    for (Type type : Type.values()) {
      if (upperExpression.startsWith(type.name() + ":")) {
        parsedType = type;
        break;
      }
    }
    _type = firstNonNull(parsedType, Type.NAME);

    if (parsedType == null) {
      _regex = Pattern.compile(_expression);
    } else {
      _regex = Pattern.compile(_expression.substring(_type.name().length() + 1));
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof FiltersSpecifier)) {
      return false;
    }
    FiltersSpecifier rhs = (FiltersSpecifier) obj;
    return Objects.equals(_expression, rhs._expression)
        && Objects.equals(_regex.pattern(), rhs._regex.pattern())
        && Objects.equals(_type, rhs._type);
  }

  @JsonIgnore
  public Pattern getRegex() {
    return _regex;
  }

  @JsonIgnore
  public Type getType() {
    return _type;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_expression, _regex.pattern(), _type.ordinal());
  }

  private boolean isFilterOnInterface(
      Configuration c, Function<Interface, IpAccessList> filterGetter, IpAccessList filter) {
    InterfacesSpecifier interfaceSpecifiers = new InterfacesSpecifier(_regex);
    return c.getAllInterfaces()
        .values()
        .stream()
        .filter(interfaceSpecifiers::matches)
        .map(filterGetter)
        .anyMatch(o -> o == filter);
  }

  /**
   * Evaluates if the given IPv6 filter matches this specifier
   *
   * @param filter The filter to evaluate
   * @return Results of the match
   */
  public boolean matches(Ip6AccessList filter) {
    switch (_type) {
      case IPV4:
        return false;
      case IPV6:
        return _regex.matcher(filter.getName()).matches();
      case NAME:
        return _regex.matcher(filter.getName()).matches();
      default:
        throw new BatfishException("Unhandled FiltersSpecifier type: " + _type);
    }
  }

  /**
   * Evaluates if the given IPv4 filter matches this specifier
   *
   * @param filter The filter to evaluate
   * @param c The configuration providing context in which to evaluate
   * @return Results of the match
   */
  public boolean matches(IpAccessList filter, Configuration c) {
    switch (_type) {
      case INPUTFILTERON:
        return isFilterOnInterface(c, Interface::getIncomingFilter, filter);
      case IPV4:
        return _regex.matcher(filter.getName()).matches();
      case IPV6:
        return false;
      case NAME:
        return _regex.matcher(filter.getName()).matches();
      case OUTPUTFILTERON:
        return isFilterOnInterface(c, Interface::getOutgoingFilter, filter);
      default:
        throw new BatfishException("Unhandled FiltersSpecifier type: " + _type);
    }
  }

  @JsonValue
  @Override
  public String toString() {
    return _expression;
  }
}
