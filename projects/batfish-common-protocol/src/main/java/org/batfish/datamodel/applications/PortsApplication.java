package org.batfish.datamodel.applications;

import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;

/** An abstract class that represents an application that has ports (TCP, Udp) */
@ParametersAreNonnullByDefault
public abstract class PortsApplication extends Application {

  public static int MAX_PORT_NUMBER = 65535;

  protected final @Nonnull List<SubRange> _ports;

  protected PortsApplication(IpProtocol ipProtocol, int port) {
    this(ipProtocol, ImmutableList.of(SubRange.singleton(port)));
  }

  protected PortsApplication(IpProtocol ipProtocol, List<SubRange> ports) {
    super(ipProtocol);
    _ports = ImmutableList.copyOf(ports);
  }

  @Override
  public AclLineMatchExpr toAclLineMatchExpr() {
    return new MatchHeaderSpace(
        HeaderSpace.builder().setIpProtocols(super.getIpProtocol()).setDstPorts(_ports).build());
  }

  public @Nonnull List<SubRange> getPorts() {
    return _ports;
  }
}
