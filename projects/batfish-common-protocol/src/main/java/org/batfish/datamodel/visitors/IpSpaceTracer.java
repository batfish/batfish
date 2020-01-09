package org.batfish.datamodel.visitors;

import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.AclIpSpaceLine;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpIpSpace;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpSpaceMetadata;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.IpWildcardIpSpace;
import org.batfish.datamodel.IpWildcardSetIpSpace;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.PrefixIpSpace;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.acl.DefaultDeniedByAclIpSpace;
import org.batfish.datamodel.acl.DeniedByAclIpSpaceLine;
import org.batfish.datamodel.acl.DeniedByNamedIpSpace;
import org.batfish.datamodel.acl.PermittedByAclIpSpaceLine;
import org.batfish.datamodel.acl.PermittedByNamedIpSpace;
import org.batfish.datamodel.trace.Tracer;

/**
 * Evaluates whether an {@link IpSpace} contains an {@link Ip}.<br>
 * Visit functions:<br>
 * - add tracing information via {@code _aclTracer} if the {@link IpSpace} is named<br>
 * - return true iff the {@link IpSpace} contains {@code _ip}.
 */
public class IpSpaceTracer implements GenericIpSpaceVisitor<Boolean> {

  private final IpSpaceDescriber _ipSpaceDescriber;

  private final Ip _ip;

  private final Tracer _tracer;

  private final String _ipDescription;

  private final Map<IpSpace, IpSpaceMetadata> _ipSpaceMetadata;

  private final Map<IpSpace, String> _ipSpaceNames;

  private final Map<String, IpSpace> _namedIpSpaces;

  public IpSpaceTracer(
      Tracer tracer,
      Ip ip,
      String ipDescription,
      Map<IpSpace, String> ipSpaceNames,
      Map<IpSpace, IpSpaceMetadata> ipSpaceMetadata,
      Map<String, IpSpace> namedIpSpaces) {
    _tracer = tracer;
    _ip = ip;
    _ipDescription = ipDescription;
    _ipSpaceDescriber = new IpSpaceDescriber(ipSpaceMetadata, namedIpSpaces);
    _ipSpaceMetadata = ipSpaceMetadata;
    _ipSpaceNames = ipSpaceNames;
    _namedIpSpaces = namedIpSpaces;
  }

  @Override
  public Boolean castToGenericIpSpaceVisitorReturnType(Object o) {
    return (Boolean) o;
  }

  private static String computeLineDescription(AclIpSpaceLine line, IpSpaceDescriber describer) {
    String srcText = line.getSrcText();
    if (srcText != null) {
      return srcText;
    }
    return line.getIpSpace().accept(describer);
  }

  private void recordAction(
      @Nonnull String aclIpSpaceName,
      @Nullable IpSpaceMetadata ipSpaceMetadata,
      int index,
      @Nonnull AclIpSpaceLine line,
      Ip ip,
      String ipDescription,
      IpSpaceDescriber describer) {
    if (line.getAction() == LineAction.PERMIT) {
      _tracer.setEvent(
          new PermittedByAclIpSpaceLine(
              aclIpSpaceName,
              ipSpaceMetadata,
              index,
              computeLineDescription(line, describer),
              ip,
              ipDescription));
    } else {
      _tracer.setEvent(
          new DeniedByAclIpSpaceLine(
              aclIpSpaceName,
              ipSpaceMetadata,
              index,
              computeLineDescription(line, describer),
              ip,
              ipDescription));
    }
  }

  private void recordDefaultDeny(
      @Nonnull String aclIpSpaceName,
      @Nullable IpSpaceMetadata ipSpaceMetadata,
      Ip ip,
      String ipDescription) {
    _tracer.setEvent(
        new DefaultDeniedByAclIpSpace(aclIpSpaceName, ip, ipDescription, ipSpaceMetadata));
  }

  private void recordNamedIpSpaceAction(
      @Nonnull String name,
      @Nonnull String ipSpaceDescription,
      IpSpaceMetadata ipSpaceMetadata,
      boolean permit,
      Ip ip,
      String ipDescription) {
    if (permit) {
      _tracer.setEvent(
          new PermittedByNamedIpSpace(
              ip, ipDescription, ipSpaceDescription, ipSpaceMetadata, name));
    } else {
      _tracer.setEvent(
          new DeniedByNamedIpSpace(ip, ipDescription, ipSpaceDescription, ipSpaceMetadata, name));
    }
  }

  @Override
  public Boolean visitAclIpSpace(AclIpSpace aclIpSpace) {
    String name = _ipSpaceNames.get(aclIpSpace);
    _tracer.newSubTrace();
    List<AclIpSpaceLine> lines = aclIpSpace.getLines();
    for (int i = 0; i < lines.size(); i++) {
      AclIpSpaceLine line = lines.get(i);
      if (line.getIpSpace().accept(this)) {
        if (name != null) {
          recordAction(
              name,
              _ipSpaceMetadata.get(aclIpSpace),
              i,
              line,
              _ip,
              _ipDescription,
              _ipSpaceDescriber);
        }
        _tracer.endSubTrace();
        return line.getAction() == LineAction.PERMIT;
      }
      _tracer.resetSubTrace();
    }
    if (name != null) {
      recordDefaultDeny(name, _ipSpaceMetadata.get(aclIpSpace), _ip, _ipDescription);
    }
    _tracer.endSubTrace();
    return false;
  }

  @Override
  public Boolean visitEmptyIpSpace(EmptyIpSpace emptyIpSpace) {
    return reportIfNamed(emptyIpSpace);
  }

  @Override
  public Boolean visitIpIpSpace(IpIpSpace ipIpSpace) {
    return reportIfNamed(ipIpSpace);
  }

  @Override
  public Boolean visitIpSpaceReference(IpSpaceReference ipSpaceReference) {
    String name = ipSpaceReference.getName();
    IpSpace ipSpace = _namedIpSpaces.get(name);
    if (ipSpace != null) {
      _tracer.newSubTrace();
      Boolean accepted = ipSpace.accept(this);
      _tracer.endSubTrace();
      return accepted;
    } else {
      return false;
    }
  }

  private boolean reportIfNamed(IpSpace ipSpace) {
    boolean result = ipSpace.containsIp(_ip, _namedIpSpaces);
    String name = _ipSpaceNames.get(ipSpace);
    if (name != null) {
      recordNamedIpSpaceAction(
          name,
          ipSpace.accept(_ipSpaceDescriber),
          _ipSpaceMetadata.get(ipSpace),
          result,
          _ip,
          _ipDescription);
    }
    return result;
  }

  @Override
  public Boolean visitIpWildcardIpSpace(IpWildcardIpSpace ipWildcardIpSpace) {
    return reportIfNamed(ipWildcardIpSpace);
  }

  @Override
  public Boolean visitIpWildcardSetIpSpace(IpWildcardSetIpSpace ipWildcardSetIpSpace) {
    return reportIfNamed(ipWildcardSetIpSpace);
  }

  @Override
  public Boolean visitPrefixIpSpace(PrefixIpSpace prefixIpSpace) {
    return reportIfNamed(prefixIpSpace);
  }

  @Override
  public Boolean visitUniverseIpSpace(UniverseIpSpace universeIpSpace) {
    return reportIfNamed(universeIpSpace);
  }
}
