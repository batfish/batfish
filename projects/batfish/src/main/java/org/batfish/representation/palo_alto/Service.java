package org.batfish.representation.palo_alto;

import static org.batfish.representation.palo_alto.PaloAltoConfiguration.computeServiceGroupMemberAclName;

import com.google.common.collect.ImmutableList;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.acl.MatchHeaderSpace;

@ParametersAreNonnullByDefault
public final class Service implements ServiceGroupMember {
  private static final long serialVersionUID = 1L;

  private final String _name;
  @Nullable private String _description;

  @Nullable private IpProtocol _protocol;

  @Nonnull private IntegerSpace _sourcePorts;
  @Nonnull private IntegerSpace _ports;

  public Service(String name) {
    _name = name;
    _ports = IntegerSpace.EMPTY;
    _sourcePorts = IntegerSpace.EMPTY;
  }

  @Nullable
  public String getDescription() {
    return _description;
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

  @Nullable
  public IpProtocol getProtocol() {
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

  public void setProtocol(IpProtocol protocol) {
    _protocol = protocol;
  }

  @Override
  public IpAccessList toIpAccessList(
      LineAction action, PaloAltoConfiguration pc, Vsys vsys, Warnings w) {
    IpAccessList.Builder retAcl =
        IpAccessList.builder()
            .setName(computeServiceGroupMemberAclName(vsys.getName(), _name))
            .setSourceName(_name)
            .setSourceType(PaloAltoStructureType.SERVICE.getDescription());
    if (_protocol == null) {
      w.redFlag(
          "Unable to convert "
              + PaloAltoStructureType.SERVICE.getDescription()
              + " "
              + _name
              + ": missing IP Protocol type");
      return retAcl.build();
    }

    HeaderSpace.Builder headerSpaceBuilder = HeaderSpace.builder();
    headerSpaceBuilder.setSrcPorts(_sourcePorts.getSubRanges());
    headerSpaceBuilder.setDstPorts(_ports.getSubRanges());
    headerSpaceBuilder.setIpProtocols(ImmutableList.of(_protocol));
    return retAcl
        .setLines(
            ImmutableList.of(
                IpAccessListLine.builder()
                    .setAction(action)
                    .setMatchCondition(new MatchHeaderSpace(headerSpaceBuilder.build()))
                    .build()))
        .build();
  }
}
