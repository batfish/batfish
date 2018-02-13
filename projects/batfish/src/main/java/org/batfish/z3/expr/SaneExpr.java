package org.batfish.z3.expr;

import com.google.common.collect.ImmutableList;
import java.util.Objects;
import org.batfish.datamodel.IcmpCode;
import org.batfish.datamodel.IcmpType;
import org.batfish.datamodel.IpProtocol;
import org.batfish.z3.HeaderField;
import org.batfish.z3.expr.visitors.BooleanExprVisitor;
import org.batfish.z3.expr.visitors.ExprVisitor;

public class SaneExpr extends BooleanExpr {

  public static final SaneExpr INSTANCE = new SaneExpr();

  private BooleanExpr _expr;

  private SaneExpr() {
    EqExpr noDstPort =
        new EqExpr(
            new VarIntExpr(HeaderField.DST_PORT),
            new LitIntExpr(0, HeaderField.SRC_PORT.getSize()));
    EqExpr noSrcPort =
        new EqExpr(
            new VarIntExpr(HeaderField.SRC_PORT),
            new LitIntExpr(0, HeaderField.SRC_PORT.getSize()));
    AndExpr noPortNumbers = new AndExpr(ImmutableList.of(noDstPort, noSrcPort));
    LitIntExpr zero = new LitIntExpr(0, 1);
    AndExpr noTcpFlags =
        new AndExpr(
            ImmutableList.of(
                new EqExpr(new VarIntExpr(HeaderField.TCP_FLAGS_CWR), zero),
                new EqExpr(new VarIntExpr(HeaderField.TCP_FLAGS_ECE), zero),
                new EqExpr(new VarIntExpr(HeaderField.TCP_FLAGS_URG), zero),
                new EqExpr(new VarIntExpr(HeaderField.TCP_FLAGS_ACK), zero),
                new EqExpr(new VarIntExpr(HeaderField.TCP_FLAGS_PSH), zero),
                new EqExpr(new VarIntExpr(HeaderField.TCP_FLAGS_RST), zero),
                new EqExpr(new VarIntExpr(HeaderField.TCP_FLAGS_SYN), zero),
                new EqExpr(new VarIntExpr(HeaderField.TCP_FLAGS_FIN), zero)));
    EqExpr noIcmpCode =
        new EqExpr(
            new VarIntExpr(HeaderField.ICMP_CODE),
            new LitIntExpr(IcmpCode.UNSET, HeaderField.ICMP_CODE.getSize()));
    EqExpr noIcmpType =
        new EqExpr(
            new VarIntExpr(HeaderField.ICMP_TYPE),
            new LitIntExpr(IcmpType.UNSET, HeaderField.ICMP_TYPE.getSize()));
    AndExpr noIcmp = new AndExpr(ImmutableList.of(noIcmpType, noIcmpCode));
    EqExpr icmpProtocol =
        new EqExpr(
            new VarIntExpr(HeaderField.IP_PROTOCOL),
            new LitIntExpr(IpProtocol.ICMP.number(), HeaderField.IP_PROTOCOL.getSize()));
    EqExpr tcpProtocol =
        new EqExpr(
            new VarIntExpr(HeaderField.IP_PROTOCOL),
            new LitIntExpr(IpProtocol.TCP.number(), HeaderField.IP_PROTOCOL.getSize()));
    EqExpr udpProtocol =
        new EqExpr(
            new VarIntExpr(HeaderField.IP_PROTOCOL),
            new LitIntExpr(IpProtocol.UDP.number(), HeaderField.IP_PROTOCOL.getSize()));
    AndExpr tcp = new AndExpr(ImmutableList.of(tcpProtocol, noIcmp));
    AndExpr udp = new AndExpr(ImmutableList.of(udpProtocol, noIcmp, noTcpFlags));
    AndExpr icmp = new AndExpr(ImmutableList.of(icmpProtocol, noTcpFlags, noPortNumbers));
    AndExpr otherIp = new AndExpr(ImmutableList.of(noIcmp, noTcpFlags, noPortNumbers));
    _expr = new OrExpr(ImmutableList.of(icmp, tcp, udp, otherIp));
  }

  @Override
  public void accept(BooleanExprVisitor visitor) {
    visitor.visitSaneExpr(this);
  }

  @Override
  public void accept(ExprVisitor visitor) {
    visitor.visitSaneExpr(this);
  }

  @Override
  public boolean exprEquals(Expr e) {
    return Objects.equals(_expr, ((SaneExpr) e)._expr);
  }

  public BooleanExpr getExpr() {
    return _expr;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_expr);
  }
}
