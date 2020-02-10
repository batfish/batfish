package org.batfish.minesweeper.smt;

import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.BitVecExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;

/**
 * A symbolic data plane packet. Includes the source and destination Ip address, source and
 * destination port, the icmp code and icmp type, the ip protocol, and all tcp flags
 *
 * @author Ryan Beckett
 */
class SymbolicPacket {

  private Context _ctx;

  private BitVecExpr _dstIp;

  private BitVecExpr _srcIp;

  private ArithExpr _dstPort;

  private ArithExpr _srcPort;

  private ArithExpr _icmpCode;

  private ArithExpr _icmpType;

  private BoolExpr _tcpAck;

  private BoolExpr _tcpCwr;

  private BoolExpr _tcpEce;

  private BoolExpr _tcpFin;

  private BoolExpr _tcpPsh;

  private BoolExpr _tcpRst;

  private BoolExpr _tcpSyn;

  private BoolExpr _tcpUrg;

  private ArithExpr _ipProtocol;

  SymbolicPacket(Context ctx, int id, String sliceName) {
    _ctx = ctx;
    _dstIp = ctx.mkBVConst(id + "_" + sliceName + "dst-ip", 32);
    _srcIp = ctx.mkBVConst(id + "_" + sliceName + "src-ip", 32);
    _dstPort = ctx.mkIntConst(id + "_" + sliceName + "dst-port");
    _srcPort = ctx.mkIntConst(id + "_" + sliceName + "src-port");
    _icmpCode = ctx.mkIntConst(id + "_" + sliceName + "icmp-code");
    _icmpType = ctx.mkIntConst(id + "_" + sliceName + "icmp-type");
    _tcpAck = ctx.mkBoolConst(id + "_" + sliceName + "tcp-ack");
    _tcpCwr = ctx.mkBoolConst(id + "_" + sliceName + "tcp-cwr");
    _tcpEce = ctx.mkBoolConst(id + "_" + sliceName + "tcp-ece");
    _tcpFin = ctx.mkBoolConst(id + "_" + sliceName + "tcp-fin");
    _tcpPsh = ctx.mkBoolConst(id + "_" + sliceName + "tcp-psh");
    _tcpRst = ctx.mkBoolConst(id + "_" + sliceName + "tcp-rst");
    _tcpSyn = ctx.mkBoolConst(id + "_" + sliceName + "tcp-syn");
    _tcpUrg = ctx.mkBoolConst(id + "_" + sliceName + "tcp-urg");
    _ipProtocol = ctx.mkIntConst(id + "_" + sliceName + "ip-protocol");
  }

  BoolExpr mkEqual(SymbolicPacket other) {
    return _ctx.mkAnd(
        _ctx.mkEq(getDstIp(), other.getDstIp()),
        _ctx.mkEq(getSrcIp(), other.getSrcIp()),
        _ctx.mkEq(getDstPort(), other.getDstPort()),
        _ctx.mkEq(getSrcPort(), other.getSrcPort()),
        _ctx.mkEq(getIpProtocol(), other.getIpProtocol()),
        _ctx.mkEq(getIcmpCode(), other.getIcmpCode()),
        _ctx.mkEq(getIcmpType(), other.getIcmpType()),
        _ctx.mkEq(getTcpAck(), other.getTcpAck()),
        _ctx.mkEq(getTcpCwr(), other.getTcpCwr()),
        _ctx.mkEq(getTcpEce(), other.getTcpEce()),
        _ctx.mkEq(getTcpFin(), other.getTcpFin()),
        _ctx.mkEq(getTcpSyn(), other.getTcpSyn()),
        _ctx.mkEq(getTcpPsh(), other.getTcpPsh()),
        _ctx.mkEq(getTcpRst(), other.getTcpRst()),
        _ctx.mkEq(getTcpUrg(), other.getTcpUrg()));
  }

  BitVecExpr getDstIp() {
    return _dstIp;
  }

  BitVecExpr getSrcIp() {
    return _srcIp;
  }

  ArithExpr getDstPort() {
    return _dstPort;
  }

  ArithExpr getSrcPort() {
    return _srcPort;
  }

  ArithExpr getIcmpCode() {
    return _icmpCode;
  }

  ArithExpr getIcmpType() {
    return _icmpType;
  }

  BoolExpr getTcpAck() {
    return _tcpAck;
  }

  BoolExpr getTcpCwr() {
    return _tcpCwr;
  }

  BoolExpr getTcpEce() {
    return _tcpEce;
  }

  BoolExpr getTcpFin() {
    return _tcpFin;
  }

  BoolExpr getTcpPsh() {
    return _tcpPsh;
  }

  BoolExpr getTcpRst() {
    return _tcpRst;
  }

  BoolExpr getTcpSyn() {
    return _tcpSyn;
  }

  BoolExpr getTcpUrg() {
    return _tcpUrg;
  }

  ArithExpr getIpProtocol() {
    return _ipProtocol;
  }
}
