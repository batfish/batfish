package org.batfish.representation.cisco_xr;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;

public class TcpUdpServiceObjectGroupLine implements ServiceObjectGroupLine {

  private final List<SubRange> _ports;

  public TcpUdpServiceObjectGroupLine(@Nonnull List<SubRange> ports) {
    _ports = ImmutableList.copyOf(requireNonNull(ports));
  }

  public List<SubRange> getPorts() {
    return _ports;
  }

  @Override
  public AclLineMatchExpr toAclLineMatchExpr() {
    return new MatchHeaderSpace(
        HeaderSpace.builder()
            .setIpProtocols(ImmutableList.of(IpProtocol.TCP, IpProtocol.UDP))
            .setSrcOrDstPorts(_ports)
            .build());
  }
}
