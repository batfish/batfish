package org.batfish.datamodel.applications;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.SubRange;

/** Represents a TCP application */
@ParametersAreNonnullByDefault
public final class TcpApplication extends PortsApplication {

  /** A TCP application with all ports */
  public static final TcpApplication ALL =
      new TcpApplication(ImmutableList.of(new SubRange(0, MAX_PORT_NUMBER)));

  public TcpApplication(List<SubRange> ports) {
    super(IpProtocol.TCP, ports);
  }

  public TcpApplication(int port) {
    super(IpProtocol.TCP, port);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof TcpApplication)) {
      return false;
    }
    TcpApplication that = (TcpApplication) o;
    return Objects.equals(_ports, that._ports);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_ports);
  }

  @Override
  public String toString() {
    if (ALL.equals(this)) {
      return "tcp";
    }
    return "tcp/" + stringifySubRanges(_ports);
  }

  @Override
  public <T> T accept(ApplicationVisitor<T> visitor) {
    return visitor.visitTcpApplication(this);
  }
}
