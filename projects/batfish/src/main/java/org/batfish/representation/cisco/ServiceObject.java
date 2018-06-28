package org.batfish.representation.cisco;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import org.batfish.common.util.ComparableStructure;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;

public class ServiceObject extends ComparableStructure<String> implements ServiceObjectGroupLine {
  /** */
  private static final long serialVersionUID = 1L;

  private String _description;

  private List<SubRange> _dstPorts;

  private Integer _icmpType;

  private List<IpProtocol> _protocols;

  private List<SubRange> _srcPorts;

  public ServiceObject(String name) {
    super(name);
    _protocols = new ArrayList<>();
  }

  public void addProtocol(IpProtocol protocol) {
    _protocols.add(protocol);
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

  public List<IpProtocol> getProtocols() {
    return _protocols;
  }

  public List<SubRange> getSrcPorts() {
    return _srcPorts;
  }

  public void setDescription(String description) {
    _description = description;
  }

  public void setDstPorts(List<SubRange> dstPorts) {
    _dstPorts = dstPorts;
  }

  public void setIcmpType(Integer icmpType) {
    _icmpType = icmpType;
  }

  public void setSrcPorts(List<SubRange> srcPorts) {
    _srcPorts = srcPorts;
  }

  @Override
  public AclLineMatchExpr toAclLineMatchExpr() {
    HeaderSpace.Builder b = HeaderSpace.builder().setIpProtocols(ImmutableList.copyOf(_protocols));
    if (_dstPorts != null && !_dstPorts.isEmpty()) {
      b.setDstPorts(_dstPorts);
    }
    if (_srcPorts != null && !_srcPorts.isEmpty()) {
      b.setSrcPorts(_srcPorts);
    }
    if (_icmpType != null) {
      b.setIcmpTypes(ImmutableList.of(new SubRange(_icmpType)));
    }
    return new MatchHeaderSpace(b.build());
  }
}
