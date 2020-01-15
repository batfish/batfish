package org.batfish.datamodel.visitors;

import static org.batfish.datamodel.acl.TraceElements.permittedByNamedIpSpace;

import java.util.Map;
import org.batfish.datamodel.AbstractIpSpaceContainsIp;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpSpaceMetadata;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.trace.Tracer;

/**
 * Evaluates whether an {@link IpSpace} contains an {@link Ip}.<br>
 * Visit functions:<br>
 * - add tracing information via {@code _aclTracer} if the {@link IpSpace} is named<br>
 * - return true iff the {@link IpSpace} contains {@code _ip}.
 */
public class IpSpaceTracer extends AbstractIpSpaceContainsIp {

  private final Ip _ip;

  private final String _ipDescription;

  private final Tracer _tracer;

  private final Map<String, IpSpaceMetadata> _ipSpaceMetadata;

  private final Map<String, IpSpace> _namedIpSpaces;

  public IpSpaceTracer(
      Tracer tracer,
      Ip ip,
      String ipDescription,
      Map<String, IpSpaceMetadata> ipSpaceMetadata,
      Map<String, IpSpace> namedIpSpaces) {
    super(ip);
    _tracer = tracer;
    _ip = ip;
    _ipDescription = ipDescription;
    _ipSpaceMetadata = ipSpaceMetadata;
    _namedIpSpaces = namedIpSpaces;
  }

  @Override
  public Boolean visitIpSpaceReference(IpSpaceReference ipSpaceReference) {
    String name = ipSpaceReference.getName();
    IpSpace ipSpace = _namedIpSpaces.get(name);
    IpSpaceMetadata metadata = _ipSpaceMetadata.get(name);
    if (ipSpace != null) {
      _tracer.newSubTrace();
      Boolean accepted = ipSpace.accept(this);
      if (accepted) {
        _tracer.setTraceElement(permittedByNamedIpSpace(_ip, _ipDescription, metadata, name));
        _tracer.endSubTrace();
      } else {
        _tracer.discardSubTrace();
      }
      return accepted;
    } else {
      return false;
    }
  }
}
