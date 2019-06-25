package org.batfish.representation.juniper;

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;

/**
 * Represents a member of a Juniper Interface range.
 *
 * <p>We currently do not support '*' as a component in the member (e.g., ge-0/0/*) because we don't
 * know offhand the maximum value of the fpc, pic, or port. In the future, we can allow the max to
 * be supplied by the user or infer from what we see in the configs.
 */
public class InterfaceRangeMember implements Serializable {

  private static final Pattern SOLO_PATTERN = Pattern.compile("[0-9]+");

  private static final Pattern RANGE_PATTERN = Pattern.compile("\\[[0-9]+-[0-9]+]");

  private static final Pattern LIST_PATTERN = Pattern.compile("\\[[0-9,]+]");

  private static final String COMPONENT_REGEX =
      String.format("%s|%s|%s", SOLO_PATTERN, RANGE_PATTERN, LIST_PATTERN);

  private static final Pattern MEMBER_ID_PATTERN =
      Pattern.compile(
          String.format(
              "^([a-z][a-z])-(%s)/(%s)/(%s)$", COMPONENT_REGEX, COMPONENT_REGEX, COMPONENT_REGEX));

  @Nonnull private final String _type;
  @Nonnull private final String _fpc;
  @Nonnull private final String _pic;
  @Nonnull private final String _port;

  public InterfaceRangeMember(String memberId) {
    Matcher matcher = MEMBER_ID_PATTERN.matcher(memberId);
    if (matcher.matches()) {
      _type = matcher.group(1);
      _fpc = matcher.group(2);
      _pic = matcher.group(3);
      _port = matcher.group(4);
    } else {
      StringBuilder message =
          new StringBuilder(String.format("Unexpected member id pattern in '%s'", memberId));
      if (memberId.contains("*")) {
        message.append(" Use of '*' as fpc or pic or port is not currently supported.");
      }
      throw new IllegalArgumentException(message.toString());
    }
  }

  static List<Integer> expandComponent(String component) {
    List<Integer> retList = new LinkedList<>();
    if (SOLO_PATTERN.matcher(component).matches()) {
      retList.add(Integer.parseInt(component));
    } else if (RANGE_PATTERN.matcher(component).matches()) {
      String strippedComponent = component.replaceAll("\\[|]", "");
      String[] range = strippedComponent.split("-");
      int lo = Integer.parseInt(range[0]);
      int hi = Integer.parseInt(range[1]);
      for (int i = lo; i <= hi; i++) {
        retList.add(i);
      }
    } else if (LIST_PATTERN.matcher(component).matches()) {
      String strippedComponent = component.replaceAll("\\[|]", "");
      String[] list = strippedComponent.split(",");
      Arrays.stream(list).forEach(i -> retList.add(Integer.parseInt(i)));
    } else {
      throw new IllegalArgumentException("Unknown pattern for component " + component);
    }
    return retList;
  }

  List<String> getAllMembers() {
    List<String> retList = new LinkedList<>();
    for (Integer fpc : InterfaceRangeMember.expandComponent(_fpc)) {
      for (Integer pic : InterfaceRangeMember.expandComponent(_pic)) {
        for (Integer port : InterfaceRangeMember.expandComponent(_port)) {
          retList.add(InterfaceRange.toInterfaceId(_type, fpc, pic, port));
        }
      }
    }
    return retList;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof InterfaceRangeMember)) {
      return false;
    }
    InterfaceRangeMember rhs = (InterfaceRangeMember) o;
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
