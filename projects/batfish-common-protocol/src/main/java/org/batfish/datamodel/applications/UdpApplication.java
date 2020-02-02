package org.batfish.datamodel.applications;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.SubRange;

/** An abstract class that represents an application that has ports (TCP, Udp) */
@ParametersAreNonnullByDefault
public final class UdpApplication extends PortsApplication {

  /** A TCP application with all ports */
  public static final UdpApplication ALL =
      new UdpApplication(ImmutableList.of(new SubRange(0, MAX_PORT_NUMBER)));

  public UdpApplication(List<SubRange> ports) {
    super(IpProtocol.UDP, ports);
  }

  public UdpApplication(int port) {
    super(IpProtocol.UDP, port);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof UdpApplication)) {
      return false;
    }
    UdpApplication that = (UdpApplication) o;
    return Objects.equals(_ports, that._ports);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_ports);
  }
}
