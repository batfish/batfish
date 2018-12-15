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

  private static final long serialVersionUID = 1L;

  private static final Pattern INTERFACE_ID_PATTERN =
      Pattern.compile("^([a-z][a-z])-([0-9]+)/([0-9]+)/([0-9]+)$");

  private class InterfaceComponents implements Serializable {

    private static final long serialVersionUID = 1L;

    @Nonnull final String type;
    final int fpc;
    final int pic;
    final int port;

    public InterfaceComponents(String interfaceId) {
      Matcher matcher = INTERFACE_ID_PATTERN.matcher(interfaceId);
      if (matcher.matches()) {
        type = matcher.group(1);
        fpc = Integer.parseInt(matcher.group(2));
        pic = Integer.parseInt(matcher.group(3));
        port = Integer.parseInt(matcher.group(4));
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
      return Objects.equals(type, rhs.type)
          && Objects.equals(fpc, rhs.fpc)
          && Objects.equals(pic, rhs.pic)
          && Objects.equals(port, rhs.port);
    }

    @Override
    public int hashCode() {
      return Objects.hash(type, fpc, pic, port);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(getClass())
          .add("type", type)
          .add("fpc", fpc)
          .add("pic", pic)
          .add("port", port)
          .toString();
    }
  }

  @Nonnull private final InterfaceComponents _fromInterface;

  @Nonnull private final InterfaceComponents _toInterface;

  public InterfaceRangeMemberRange(String fromInterfaceId, String toInterface) {
    _fromInterface = new InterfaceComponents(fromInterfaceId);
    _toInterface = new InterfaceComponents(toInterface);
    checkArgument(
        _fromInterface.type.equals(_toInterface.type),
        String.format(
            "Cannot have a range with different interface types: %s and %s",
            _fromInterface.type, _toInterface.type));
  }

  /**
   * Returns the full list of members indicated by this range, per
   * https://www.juniper.net/documentation/en_US/junos/topics/task/configuration/interfaces-expanding-interface-range-member-and-member-range-statements.html
   */
  public List<String> getAllMembers() {
    List<String> retList = new LinkedList<>();
    for (int fpc = _fromInterface.fpc; fpc <= _toInterface.fpc; fpc++) {
      for (int pic = _fromInterface.pic; pic <= _toInterface.pic; pic++) {
        for (int port = _fromInterface.port; port <= _toInterface.port; port++) {
          retList.add(toInterfaceId(_fromInterface.type, fpc, pic, port));
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
