package org.batfish.representation.cisco;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;

@ParametersAreNonnullByDefault
public class UdpServiceObjectGroupLine implements ServiceObjectGroupLine {

  private final List<SubRange> _ports;

  public UdpServiceObjectGroupLine(List<SubRange> ports) {
    _ports = ImmutableList.copyOf(requireNonNull(ports));
  }

  public List<SubRange> getPorts() {
    return _ports;
  }

  @Override
  public @Nonnull AclLineMatchExpr toAclLineMatchExpr(
      Map<String, ServiceObject> serviceObjects,
      Map<String, ServiceObjectGroup> serviceObjectGroups) {
    return new MatchHeaderSpace(
        HeaderSpace.builder()
            .setIpProtocols(ImmutableList.of(IpProtocol.UDP))
            .setDstPorts(_ports)
            .build());
  }
}
