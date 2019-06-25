package org.batfish.representation.juniper;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.representation.juniper.InterfaceRange.toInterfaceId;

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** Represents a range of members of Juniper interface range */
@ParametersAreNonnullByDefault
public class InterfaceRangeMemberRange implements Serializable {

  private static final Pattern INTERFACE_ID_PATTERN =
      Pattern.compile("^([a-z][a-z])-([0-9]+)/([0-9]+)/([0-9]+)$");

  private class InterfaceComponents implements Serializable {

    @Nonnull final String _type;
    final int _fpc;
    final int _pic;
    final int _port;

    public InterfaceComponents(String interfaceId) {
      Matcher matcher = INTERFACE_ID_PATTERN.matcher(interfaceId);
      if (matcher.matches()) {
        _type = matcher.group(1);
        _fpc = Integer.parseInt(matcher.group(2));
        _pic = Integer.parseInt(matcher.group(3));
        _port = Integer.parseInt(matcher.group(4));
      } else {
        throw new IllegalArgumentException("Unexpected interface id pattern: " + interfaceId);
      }
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof InterfaceComponents)) {
        return false;
      }
      InterfaceComponents rhs = (InterfaceComponents) o;
      return Objects.equals(_type, rhs._type)
          && Objects.equals(_fpc, rhs._fpc)
          && Objects.equals(_pic, rhs._pic)
          && Objects.equals(_port, rhs._port);
    }

    @Override
    public int hashCode() {
      return Objects.hash(_type, _fpc, _pic, _port);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(getClass())
          .add("type", _type)
          .add("fpc", _fpc)
          .add("pic", _pic)
          .add("port", _port)
          .toString();
    }
  }

  @Nonnull private final InterfaceComponents _fromInterface;

  @Nonnull private final InterfaceComponents _toInterface;

  public InterfaceRangeMemberRange(String fromInterfaceId, String toInterface) {
    _fromInterface = new InterfaceComponents(fromInterfaceId);
    _toInterface = new InterfaceComponents(toInterface);
    checkArgument(
        _fromInterface._type.equals(_toInterface._type),
        String.format(
            "Cannot have a range with different interface types: %s and %s",
            _fromInterface._type, _toInterface._type));
  }

  /**
   * Returns the full list of members indicated by this range, per
   * https://www.juniper.net/documentation/en_US/junos/topics/task/configuration/interfaces-expanding-interface-range-member-and-member-range-statements.html
   */
  public List<String> getAllMembers() {
    List<String> retList = new LinkedList<>();
    for (int fpc = _fromInterface._fpc; fpc <= _toInterface._fpc; fpc++) {
      for (int pic = _fromInterface._pic; pic <= _toInterface._pic; pic++) {
        for (int port = _fromInterface._port; port <= _toInterface._port; port++) {
          retList.add(toInterfaceId(_fromInterface._type, fpc, pic, port));
        }
      }
    }
    return retList;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof InterfaceRangeMemberRange)) {
      return false;
    }
    InterfaceRangeMemberRange rhs = (InterfaceRangeMemberRange) o;
    return Objects.equals(_fromInterface, rhs._fromInterface)
        && Objects.equals(_toInterface, rhs._toInterface);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_fromInterface, _toInterface);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass())
        .add("from", _fromInterface)
        .add("to", _toInterface)
        .toString();
  }

  @Nonnull
  public InterfaceComponents getFromInterface() {
    return _fromInterface;
  }

  @Nonnull
  public InterfaceComponents getToInterface() {
    return _toInterface;
  }
}
