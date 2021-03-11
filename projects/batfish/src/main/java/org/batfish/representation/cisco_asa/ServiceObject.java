package org.batfish.representation.cisco_asa;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
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
public final class ServiceObject implements ServiceObjectGroupLine {

  private String _description;

  private final List<SubRange> _dstPorts;

  private Integer _icmpType;

  private final String _name;

  private final List<IpProtocol> _protocols;

  private final List<SubRange> _srcPorts;

  public ServiceObject(String name) {
    _dstPorts = new ArrayList<>();
    _name = name;
    _protocols = new ArrayList<>();
    _srcPorts = new ArrayList<>();
  }

  public void addDstPorts(List<SubRange> dstPorts) {
    _dstPorts.addAll(dstPorts);
  }

  public void addProtocol(@Nonnull IpProtocol protocol) {
    _protocols.add(protocol);
  }

  public void addSrcPorts(List<SubRange> srcPorts) {
    _srcPorts.addAll(srcPorts);
  }

  public String getDescription() {
    return _description;
  }

  public List<SubRange> getDstPorts() {
    return _dstPorts;
  }

  public Integer getIcmpType() {
    return _icmpType;
  }

  public String getName() {
    return _name;
  }

  public List<IpProtocol> getProtocols() {
    return _protocols;
  }

  public List<SubRange> getSrcPorts() {
    return _srcPorts;
  }

  public void setDescription(String description) {
    _description = description;
  }

  public void setIcmpType(Integer icmpType) {
    _icmpType = icmpType;
  }

  @Override
  public @Nonnull AclLineMatchExpr toAclLineMatchExpr(
      Map<String, ServiceObject> serviceObjects,
      Map<String, ServiceObjectGroup> serviceObjectGroups) {
    HeaderSpace.Builder b = HeaderSpace.builder().setIpProtocols(ImmutableList.copyOf(_protocols));
    b.setDstPorts(_dstPorts);
    b.setSrcPorts(_srcPorts);
    if (_icmpType != null) {
      b.setIcmpTypes(ImmutableList.of(new SubRange(_icmpType)));
    }
    return new MatchHeaderSpace(b.build());
  }
}
