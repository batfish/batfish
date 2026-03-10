package org.batfish.representation.cisco;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AclLineMatchExprs;

@ParametersAreNonnullByDefault
public class IcmpServiceObjectGroupLine implements ServiceObjectGroupLine {

  private final @Nullable Integer _icmpType;
  private final @Nullable Integer _icmpCode;

  public IcmpServiceObjectGroupLine(@Nullable Integer type, @Nullable Integer code) {
    checkArgument(
        code == null || type != null, "Cannot provide ICMP code %s without ICMP type", code);
    _icmpType = type;
    _icmpCode = code;
  }

  @Override
  public @Nonnull AclLineMatchExpr toAclLineMatchExpr(
      Map<String, ServiceObject> serviceObjects,
      Map<String, ServiceObjectGroup> serviceObjectGroups) {
    AclLineMatchExpr icmp = AclLineMatchExprs.matchIpProtocol(IpProtocol.ICMP);
    if (_icmpType == null && _icmpCode == null) {
      return icmp;
    } else if (_icmpCode == null) {
      return AclLineMatchExprs.and(icmp, AclLineMatchExprs.matchIcmpType(_icmpType));
    }
    return AclLineMatchExprs.and(icmp, AclLineMatchExprs.matchIcmp(_icmpType, _icmpCode));
  }
}
