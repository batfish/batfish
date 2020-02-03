package org.batfish.datamodel.applications;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.SubRange;
import org.parboiled.common.ImmutableList;

/** An abstract class that represents an application that has ports (TCP, Udp) */
@ParametersAreNonnullByDefault
public abstract class PortsApplication extends Application {

  public static int MAX_PORT_NUMBER = 65535;

  @Nonnull protected final List<SubRange> _ports;

  protected PortsApplication(IpProtocol ipProtocol, int port) {
    this(ipProtocol, ImmutableList.of(SubRange.singleton(port)));
  }

  protected PortsApplication(IpProtocol ipProtocol, List<SubRange> ports) {
    super(ipProtocol);
    _ports = ImmutableList.copyOf(ports);
  }

  @Nonnull
  public List<SubRange> getPorts() {
    return _ports;
  }
}
