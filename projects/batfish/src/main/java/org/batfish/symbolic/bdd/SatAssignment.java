package org.batfish.symbolic.bdd;

import java.util.List;
import java.util.Objects;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.TcpFlags;
import org.batfish.symbolic.CommunityVar;
import org.batfish.symbolic.OspfType;

/*
 * Helper class for translating back from a BDD to a human-understandable
 * form. We walk the BDD, collecting the true/false bits for each value
 * and translate it to a collection of values, one for each represented
 * field in BDDRoute and/or BDDPacket.
 */
public class SatAssignment {
  private IpProtocol _ipProtocol;
  private Ip _dstIp;
  private Ip _srcIp;
  private int _dstPort;
  private int _srcPort;
  private int _icmpCode;
  private int _icmpType;
  private int _prefixLen;
  private int _adminDist;
  private int _localPref;
  private int _med;
  private int _metric;
  private OspfType _ospfMetric;
  private List<CommunityVar> _communities;
  private TcpFlags _tcpFlags;
  private String _srcRouter;
  private String _dstRouter;
  private RoutingProtocol _routingProtocol;
  private Ip _nextHopIp;

  public IpProtocol getIpProtocol() {
    return _ipProtocol;
  }

  public void setIpProtocol(IpProtocol ipProtocol) {
    this._ipProtocol = ipProtocol;
  }

  public Ip getDstIp() {
    return _dstIp;
  }

  public void setDstIp(Ip dstIp) {
    this._dstIp = dstIp;
  }

  public Ip getSrcIp() {
    return _srcIp;
  }

  public void setSrcIp(Ip srcIp) {
    this._srcIp = srcIp;
  }

  public int getDstPort() {
    return _dstPort;
  }

  public void setDstPort(int dstPort) {
    this._dstPort = dstPort;
  }

  public int getSrcPort() {
    return _srcPort;
  }

  public void setSrcPort(int srcPort) {
    this._srcPort = srcPort;
  }

  public int getIcmpCode() {
    return _icmpCode;
  }

  public void setIcmpCode(int icmpCode) {
    this._icmpCode = icmpCode;
  }

  public int getIcmpType() {
    return _icmpType;
  }

  public void setIcmpType(int icmpType) {
    this._icmpType = icmpType;
  }

  public TcpFlags getTcpFlags() {
    return _tcpFlags;
  }

  public void setTcpFlags(TcpFlags tcpFlags) {
    this._tcpFlags = tcpFlags;
  }

  public int getPrefixLen() {
    return _prefixLen;
  }

  public void setPrefixLen(int prefixLen) {
    this._prefixLen = prefixLen;
  }

  public int getAdminDist() {
    return _adminDist;
  }

  public void setAdminDist(int adminDist) {
    this._adminDist = adminDist;
  }

  public int getLocalPref() {
    return _localPref;
  }

  public void setLocalPref(int localPref) {
    this._localPref = localPref;
  }

  public int getMed() {
    return _med;
  }

  public void setMed(int med) {
    this._med = med;
  }

  public int getMetric() {
    return _metric;
  }

  public void setMetric(int metric) {
    this._metric = metric;
  }

  public OspfType getOspfMetric() {
    return _ospfMetric;
  }

  public void setOspfMetric(OspfType ospfMetric) {
    this._ospfMetric = ospfMetric;
  }

  public List<CommunityVar> getCommunities() {
    return _communities;
  }

  public void setCommunities(List<CommunityVar> communities) {
    this._communities = communities;
  }

  public String getSrcRouter() {
    return _srcRouter;
  }

  public void setSrcRouter(String srcRouter) {
    this._srcRouter = srcRouter;
  }

  public String getDstRouter() {
    return _dstRouter;
  }

  public void setDstRouter(String dstRouter) {
    this._dstRouter = dstRouter;
  }

  public RoutingProtocol getRoutingProtocol() {
    return _routingProtocol;
  }

  public void setRoutingProtocol(RoutingProtocol protocol) {
    this._routingProtocol = protocol;
  }

  public Ip getNextHopIp() {
    return _nextHopIp;
  }

  public void setNextHopIp(Ip x) {
    this._nextHopIp = x;
  }

  public Flow toFlow() {
    Flow.Builder builder = Flow.builder();
    builder.setTag("AI");
    builder.setIngressNode(getSrcRouter());
    builder.setDstIp(getDstIp());
    builder.setSrcIp(getSrcIp());
    builder.setDstPort(getDstPort());
    builder.setSrcPort(getSrcPort());
    builder.setIpProtocol(getIpProtocol());
    builder.setIcmpCode(getIcmpCode());
    builder.setIcmpType(getIcmpType());
    builder.setTcpFlagsAck(getTcpFlags().getAck() ? 1 : 0);
    builder.setTcpFlagsSyn(getTcpFlags().getSyn() ? 1 : 0);
    builder.setTcpFlagsCwr(getTcpFlags().getCwr() ? 1 : 0);
    builder.setTcpFlagsEce(getTcpFlags().getEce() ? 1 : 0);
    builder.setTcpFlagsFin(getTcpFlags().getFin() ? 1 : 0);
    builder.setTcpFlagsPsh(getTcpFlags().getPsh() ? 1 : 0);
    builder.setTcpFlagsRst(getTcpFlags().getRst() ? 1 : 0);
    builder.setTcpFlagsUrg(getTcpFlags().getUrg() ? 1 : 0);
    return builder.build();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SatAssignment that = (SatAssignment) o;
    return _dstPort == that._dstPort
        && _srcPort == that._srcPort
        && _icmpCode == that._icmpCode
        && _icmpType == that._icmpType
        && _prefixLen == that._prefixLen
        && _adminDist == that._adminDist
        && _localPref == that._localPref
        && _med == that._med
        && _metric == that._metric
        && _ipProtocol == that._ipProtocol
        && Objects.equals(_dstIp, that._dstIp)
        && Objects.equals(_srcIp, that._srcIp)
        && _ospfMetric == that._ospfMetric
        && Objects.equals(_communities, that._communities)
        && Objects.equals(_tcpFlags, that._tcpFlags)
        && Objects.equals(_srcRouter, that._srcRouter)
        && Objects.equals(_dstRouter, that._dstRouter)
        && _routingProtocol == that._routingProtocol
        && Objects.equals(_nextHopIp, that._nextHopIp);
  }

  @Override
  public int hashCode() {

    return Objects.hash(
        _ipProtocol,
        _dstIp,
        _srcIp,
        _dstPort,
        _srcPort,
        _icmpCode,
        _icmpType,
        _prefixLen,
        _adminDist,
        _localPref,
        _med,
        _metric,
        _ospfMetric,
        _communities,
        _tcpFlags,
        _srcRouter,
        _dstRouter,
        _routingProtocol,
        _nextHopIp);
  }
}
