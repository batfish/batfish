package org.batfish.representation.cisco_asa;

import com.google.common.collect.ImmutableList;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.TrueExpr;

public class ProtocolObjectGroupProtocolLine implements ProtocolObjectGroupLine {

  private final @Nullable IpProtocol _protocol;

  public ProtocolObjectGroupProtocolLine(@Nullable IpProtocol protocol) {
    _protocol = protocol;
  }

  public Optional<IpProtocol> getProtocol() {
    return Optional.ofNullable(_protocol);
  }

  @Override
  public AclLineMatchExpr toAclLineMatchExpr(
      Map<String, ProtocolObjectGroup> protocolObjectGroups) {
    if (_protocol == null) {
      return TrueExpr.INSTANCE;
    }
    return new MatchHeaderSpace(
        HeaderSpace.builder().setIpProtocols(ImmutableList.of(_protocol)).build());
  }
}
