package org.batfish.datamodel.applications;

import static org.batfish.datamodel.IpProtocol.ICMP;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IcmpType;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.specifier.parboiled.CommonParser;

/** An {@link IcmpApplication} of a specific type and one or more codes */
@ParametersAreNonnullByDefault
public final class IcmpTypeCodesApplication extends IcmpApplication {

  public static final SubRange ALL_CODES = new SubRange(0, IcmpApplication.MAX_TYPE_CODE);

  private final int _type;

  @Nonnull private final List<SubRange> _codes;

  public IcmpTypeCodesApplication(int type, int code) {
    this(type, ImmutableList.of(SubRange.singleton(code)));
  }

  public IcmpTypeCodesApplication(int type, List<SubRange> codes) {
    _type = type;
    _codes = ImmutableList.copyOf(codes);
  }

  public int getType() {
    return _type;
  }

  @Nonnull
  public List<SubRange> getCodes() {
    return _codes;
  }

  public static final Map<Integer, SubRange> VALID_TYPE_CODES =
      new ImmutableMap.Builder<Integer, SubRange>()
          .put(IcmpType.ECHO_REPLY, SubRange.singleton(0))
          .put(IcmpType.ECHO_REQUEST, SubRange.singleton(0))
          .put(IcmpType.ROUTER_ADVERTISEMENT, SubRange.singleton(0))
          .put(IcmpType.ROUTER_SOLICITATION, SubRange.singleton(0))
          .put(IcmpType.SOURCE_QUENCH, SubRange.singleton(0))
          .put(IcmpType.TIMESTAMP_REQUEST, SubRange.singleton(0))
          .put(IcmpType.TIMESTAMP_REPLY, SubRange.singleton(0))
          .put(IcmpType.INFO_REQUEST, SubRange.singleton(0))
          .put(IcmpType.INFO_REPLY, SubRange.singleton(0))
          .put(IcmpType.MASK_REQUEST, SubRange.singleton(0))
          .put(IcmpType.MASK_REPLY, SubRange.singleton(0))
          .put(IcmpType.TRACEROUTE, SubRange.singleton(0))
          .put(IcmpType.REQUEST_EXTENDED_ECHO, SubRange.singleton(0))
          .put(IcmpType.DESTINATION_UNREACHABLE, new SubRange(0, 15))
          .put(IcmpType.REDIRECT_MESSAGE, new SubRange(0, 4))
          .put(IcmpType.REQUEST_EXTENDED_REPLY, new SubRange(0, 4))
          .put(IcmpType.TIME_EXCEEDED, new SubRange(0, 1))
          .put(IcmpType.PARAMETER_PROBLEM, new SubRange(0, 2))
          .build();

  /**
   * Returns if the ICMP code is valid for the provided type. Assumes that type and code are
   * non-null and non-negative numbers (as parsed by {@link CommonParser#Number()}).
   *
   * <p>The classification below is based on
   * https://en.wikipedia.org/wiki/Internet_Control_Message_Protocol#Control_messages
   */
  public static boolean isValidTypeCode(int type, int code) {
    // types with no valid codes
    if (type == 1
        || type == 2
        || type == 6
        || type == 7
        || (type >= 19 && type <= 29)
        || (type >= 31 && type <= 41)
        || type >= 44) {
      return false;
    }

    switch (type) {
      case 0: // echo request
      case 8:
      case 9: // router advertisement
      case 10: // router solicitation
      case 4: // source quench
      case 13: // Timestamp
      case 14:
      case 15:
      case 16:
      case 17:
      case 18: // Address Mask Reply
      case 30: // traceroute
      case 42:
        return code == 0;
      case 3: // destination unreachable
        return code <= 15;
      case 5: // redirect message
      case 43:
        return code <= 4;
      case 11: // time exceeded
        return code <= 1;
      case 12: // Parameter Problem: Bad IP header
        return code <= 2;
      default:
        throw new IllegalArgumentException("Invalid ICMP type " + type);
    }
  }

  public static boolean isValidTypeCode2(int type, int code) {
    return VALID_TYPE_CODES.containsKey(type) && VALID_TYPE_CODES.get(type).includes(code);
  }

  @Override
  public AclLineMatchExpr toAclLineMatchExpr() {
    return new MatchHeaderSpace(
        HeaderSpace.builder()
            .setIpProtocols(ICMP)
            .setIcmpTypes(_type)
            .setIcmpCodes(_codes)
            .build());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof IcmpTypeCodesApplication)) {
      return false;
    }
    IcmpTypeCodesApplication that = (IcmpTypeCodesApplication) o;
    return _type == that._type && _codes.equals(that._codes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_type, _codes);
  }

  @Override
  public String toString() {
    if (_codes.equals(ImmutableList.of(ALL_CODES))) {
      return "icmp/" + _type;
    }
    return String.format("icmp/%d/%s", _type, stringifySubRanges(_codes));
  }

  @Override
  public <T> T accept(ApplicationVisitor<T> visitor) {
    return visitor.visitIcmpTypeCodesApplication(this);
  }
}
