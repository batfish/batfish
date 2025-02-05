package org.batfish.representation.palo_alto;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.batfish.representation.palo_alto.PaloAltoConfiguration.computeServiceGroupMemberAclName;

import com.google.common.collect.ImmutableList;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;

@ParametersAreNonnullByDefault
public final class Service implements ServiceGroupMember {

  public static class Builder {
    private final @Nonnull String _name;
    private @Nullable String _description;
    private @Nullable Integer _icmpType;
    private @Nullable IpProtocol _ipProtocol;
    private @Nullable IntegerSpace _ports;
    private @Nullable IntegerSpace _sourcePorts;

    private Builder(@Nonnull String name) {
      _name = name;
    }

    public @Nonnull Builder addPort(int port) {
      IntegerSpace old = firstNonNull(_ports, IntegerSpace.EMPTY);
      _ports = IntegerSpace.builder().including(old).including(port).build();
      return this;
    }

    public @Nonnull Builder addPorts(int... ports) {
      IntegerSpace old = firstNonNull(_ports, IntegerSpace.EMPTY);
      _ports = IntegerSpace.builder().including(old).including(ports).build();
      return this;
    }

    public @Nonnull Builder addPorts(SubRange ports) {
      IntegerSpace old = firstNonNull(_ports, IntegerSpace.EMPTY);
      _ports = IntegerSpace.builder().including(old).including(ports).build();
      return this;
    }

    public @Nonnull Builder addSourcePort(int sourcePort) {
      IntegerSpace old = firstNonNull(_sourcePorts, IntegerSpace.EMPTY);
      _sourcePorts = IntegerSpace.builder().including(old).including(sourcePort).build();
      return this;
    }

    public @Nonnull Builder addSourcePorts(SubRange sourcePorts) {
      IntegerSpace old = firstNonNull(_sourcePorts, IntegerSpace.EMPTY);
      _sourcePorts = IntegerSpace.builder().including(old).including(sourcePorts).build();
      return this;
    }

    public @Nonnull Builder setDescription(@Nullable String description) {
      _description = description;
      return this;
    }

    public @Nonnull Builder setIcmpType(@Nullable Integer icmpType) {
      _icmpType = icmpType;
      return this;
    }

    public @Nonnull Builder setIpProtocol(@Nullable IpProtocol ipProtocol) {
      _ipProtocol = ipProtocol;
      return this;
    }

    public @Nonnull Service build() {
      Service ret = new Service(_name);
      ret._description = _description;
      ret._protocol = _ipProtocol;
      if (_ports != null) {
        ret._ports = _ports;
      }
      if (_sourcePorts != null) {
        ret._sourcePorts = _sourcePorts;
      }
      if (_icmpType != null) {
        ret._icmpType = _icmpType;
      }
      return ret;
    }
  }

  public static Builder builder(@Nonnull String name) {
    return new Builder(name);
  }

  private final String _name;
  private @Nullable String _description;
  private @Nullable Integer _icmpType;
  private @Nullable IpProtocol _protocol;
  private @Nonnull IntegerSpace _sourcePorts;
  private @Nonnull IntegerSpace _ports;
  private final @Nonnull Set<String> _tags;

  public Service(String name) {
    _name = name;
    _ports = IntegerSpace.EMPTY;
    _sourcePorts = IntegerSpace.EMPTY;
    _tags = new HashSet<>();
  }

  public @Nullable String getDescription() {
    return _description;
  }

  public @Nullable Integer getIcmpType() {
    return _icmpType;
  }

  @Override
  public String getName() {
    return _name;
  }

  public void addPort(int port) {
    _ports = IntegerSpace.builder().including(_ports).including(port).build();
  }

  public void addPorts(SubRange ports) {
    _ports = IntegerSpace.builder().including(_ports).including(ports).build();
  }

  public @Nullable IpProtocol getProtocol() {
    return _protocol;
  }

  public void addSourcePort(int port) {
    _sourcePorts = IntegerSpace.builder().including(_sourcePorts).including(port).build();
  }

  public void addSourcePorts(SubRange ports) {
    _sourcePorts = IntegerSpace.builder().including(_sourcePorts).including(ports).build();
  }

  public void setDescription(String description) {
    _description = description;
  }

  public void setIcmpType(@Nullable Integer icmpType) {
    _icmpType = icmpType;
  }

  public void setProtocol(IpProtocol protocol) {
    _protocol = protocol;
  }

  public @Nonnull IntegerSpace getPorts() {
    return _ports;
  }

  public @Nonnull Set<String> getTags() {
    return _tags;
  }

  public void addTag(String tag) {
    _tags.add(tag);
  }

  @Override
  public IpAccessList toIpAccessList(
      LineAction action, PaloAltoConfiguration pc, Vsys vsys, Warnings w) {
    IpAccessList.Builder retAcl =
        IpAccessList.builder()
            .setName(computeServiceGroupMemberAclName(vsys.getName(), _name))
            .setSourceName(_name)
            .setSourceType(PaloAltoStructureType.SERVICE.getDescription());

    return retAcl
        .setLines(
            ImmutableList.of(
                ExprAclLine.builder().setAction(action).setMatchCondition(toMatchExpr(w)).build()))
        .build();
  }

  public @Nonnull AclLineMatchExpr toMatchExpr(@Nonnull Warnings w) {
    if (_protocol == null) {
      w.redFlagf(
          "Unable to convert %s %s: missing IP Protocol type",
          PaloAltoStructureType.SERVICE.getDescription(), _name);
      return FalseExpr.INSTANCE;
    }
    HeaderSpace.Builder headerSpace =
        HeaderSpace.builder()
            .setIpProtocols(ImmutableList.of(_protocol))
            .setSrcPorts(_sourcePorts.getSubRanges())
            .setDstPorts(_ports.getSubRanges());
    if (_icmpType != null) {
      headerSpace.setIcmpTypes(_icmpType);
    }
    return new MatchHeaderSpace(headerSpace.build());
  }
}
