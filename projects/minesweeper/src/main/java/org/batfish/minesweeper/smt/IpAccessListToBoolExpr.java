package org.batfish.minesweeper.smt;

import com.google.common.collect.Lists;
import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.BitVecExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TcpFlagsMatchConditions;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AndMatchExpr;
import org.batfish.datamodel.acl.DeniedByAcl;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.datamodel.acl.GenericAclLineMatchExprVisitor;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.MatchSrcInterface;
import org.batfish.datamodel.acl.NotMatchExpr;
import org.batfish.datamodel.acl.OrMatchExpr;
import org.batfish.datamodel.acl.OriginatingFromDevice;
import org.batfish.datamodel.acl.PermittedByAcl;
import org.batfish.datamodel.acl.TrueExpr;

public class IpAccessListToBoolExpr implements GenericAclLineMatchExprVisitor<BoolExpr> {
  private final BoolExprOps _boolExprOps;

  private final Context _context;

  private final SymbolicPacket _packet;

  public IpAccessListToBoolExpr(Context context, SymbolicPacket packet) {
    _boolExprOps = new BoolExprOps(context);
    _context = context;
    _packet = packet;
  }

  private BoolExpr not(BoolExpr expr) {
    return expr == null ? null : _context.mkNot(expr);
  }

  private BoolExpr toBoolExpr(AclLineMatchExpr matchExpr) {
    return matchExpr.accept(this);
  }

  public BoolExpr toBoolExpr(IpAccessList ipAccessList) {
    BoolExpr expr = _context.mkFalse();
    for (AclLine l : Lists.reverse(ipAccessList.getLines())) {
      if (!(l instanceof ExprAclLine)) {
        throw new UnsupportedOperationException();
      }
      ExprAclLine line = (ExprAclLine) l;
      BoolExpr matchExpr = line.getMatchCondition().accept(this);
      BoolExpr actionExpr =
          line.getAction() == LineAction.PERMIT ? _context.mkTrue() : _context.mkFalse();
      expr = (BoolExpr) _context.mkITE(matchExpr, actionExpr, expr);
    }
    return expr;
  }

  private @Nullable BoolExpr toBoolExpr(@Nullable IpSpace ipSpace, BitVecExpr var) {
    if (ipSpace == null) {
      return null;
    }
    return ipSpace.accept(new IpSpaceToBoolExpr(_context, var));
  }

  private @Nullable BoolExpr toBoolExpr(Set<IpProtocol> ipProtocols) {
    if (ipProtocols == null || ipProtocols.isEmpty()) {
      return null;
    }

    return _context.mkOr(
        ipProtocols.stream()
            .map(
                ipProtocol ->
                    _context.mkEq(_packet.getIpProtocol(), _context.mkInt(ipProtocol.number())))
            .toArray(BoolExpr[]::new));
  }

  private @Nullable BoolExpr toBoolExpr(@Nullable Set<SubRange> ranges, ArithExpr var) {
    if (ranges == null || ranges.isEmpty()) {
      return null;
    }
    return _context.mkOr(
        ranges.stream().map(range -> toBoolExpr(range, var)).toArray(BoolExpr[]::new));
  }

  private BoolExpr toBoolExpr(SubRange range, ArithExpr var) {
    int start = range.getStart();
    int end = range.getEnd();
    ArithExpr startExpr = _context.mkInt(start);
    ArithExpr endExpr = _context.mkInt(end);
    return start == end
        ? _context.mkEq(var, startExpr)
        : _context.mkAnd(_context.mkGe(var, startExpr), _context.mkLe(var, endExpr));
  }

  private BoolExpr toBoolExpr(List<TcpFlagsMatchConditions> tcpFlags) {
    if (tcpFlags == null || tcpFlags.isEmpty()) {
      return null;
    }

    return _context.mkOr(tcpFlags.stream().map(this::toBoolExpr).toArray(BoolExpr[]::new));
  }

  /** For TcpFlagsMatchConditions */
  private BoolExpr toBoolExpr(boolean useFlag, boolean flagValue, BoolExpr flagExpr) {
    return useFlag ? flagValue ? flagExpr : _context.mkNot(flagExpr) : null;
  }

  private BoolExpr toBoolExpr(TcpFlagsMatchConditions tcpFlags) {
    if (!tcpFlags.anyUsed()) {
      return null;
    }

    return _boolExprOps.and(
        toBoolExpr(tcpFlags.getUseAck(), tcpFlags.getTcpFlags().getAck(), _packet.getTcpAck()),
        toBoolExpr(tcpFlags.getUseCwr(), tcpFlags.getTcpFlags().getCwr(), _packet.getTcpCwr()),
        toBoolExpr(tcpFlags.getUseEce(), tcpFlags.getTcpFlags().getEce(), _packet.getTcpEce()),
        toBoolExpr(tcpFlags.getUseFin(), tcpFlags.getTcpFlags().getFin(), _packet.getTcpFin()),
        toBoolExpr(tcpFlags.getUsePsh(), tcpFlags.getTcpFlags().getPsh(), _packet.getTcpPsh()),
        toBoolExpr(tcpFlags.getUseRst(), tcpFlags.getTcpFlags().getRst(), _packet.getTcpRst()),
        toBoolExpr(tcpFlags.getUseSyn(), tcpFlags.getTcpFlags().getSyn(), _packet.getTcpSyn()),
        toBoolExpr(tcpFlags.getUseUrg(), tcpFlags.getTcpFlags().getUrg(), _packet.getTcpUrg()));
  }

  private static <T> void forbidHeaderSpaceField(String fieldName, Set<T> fieldValue) {
    if (fieldValue != null && !fieldValue.isEmpty()) {
      throw new BatfishException("unsupported HeaderSpace field " + fieldName);
    }
  }

  private static void forbidHeaderSpaceField(String fieldName, IpSpace fieldValue) {
    if (fieldValue != null && fieldValue != EmptyIpSpace.INSTANCE) {
      throw new BatfishException("unsupported HeaderSpace field " + fieldName);
    }
  }

  @Override
  public BoolExpr visitAndMatchExpr(AndMatchExpr andMatchExpr) {
    return _context.mkAnd(
        andMatchExpr.getConjuncts().stream().map(this::toBoolExpr).toArray(BoolExpr[]::new));
  }

  @Override
  public BoolExpr visitDeniedByAcl(DeniedByAcl deniedByAcl) {
    throw new BatfishException("TODO: support DeniedByAcl");
  }

  @Override
  public BoolExpr visitFalseExpr(FalseExpr falseExpr) {
    return _context.mkFalse();
  }

  @Override
  public BoolExpr visitMatchHeaderSpace(MatchHeaderSpace matchHeaderSpace) {
    HeaderSpace headerSpace = matchHeaderSpace.getHeaderspace();
    forbidHeaderSpaceField("dscps", headerSpace.getDscps());
    forbidHeaderSpaceField("ecns", headerSpace.getEcns());
    forbidHeaderSpaceField("fragmentOffsets", headerSpace.getFragmentOffsets());
    forbidHeaderSpaceField("notDscps", headerSpace.getNotDscps());
    forbidHeaderSpaceField("notEcns", headerSpace.getNotEcns());
    forbidHeaderSpaceField("notFragmentOffsets", headerSpace.getNotFragmentOffsets());
    forbidHeaderSpaceField("notIcmpCodes", headerSpace.getNotIcmpCodes());
    forbidHeaderSpaceField("notIcmpTypes", headerSpace.getNotIcmpTypes());
    forbidHeaderSpaceField("notIpProtocols", headerSpace.getNotIpProtocols());
    forbidHeaderSpaceField("srcOrDstIps", headerSpace.getSrcOrDstIps());
    forbidHeaderSpaceField("srcOrDstPorts", headerSpace.getSrcOrDstPorts());

    BoolExpr expr =
        _boolExprOps.and(
            toBoolExpr(headerSpace.getDstIps(), _packet.getDstIp()),
            toBoolExpr(headerSpace.getSrcIps(), _packet.getSrcIp()),
            toBoolExpr(headerSpace.getDstPorts(), _packet.getDstPort()),
            toBoolExpr(headerSpace.getSrcPorts(), _packet.getSrcPort()),
            not(toBoolExpr(headerSpace.getNotDstIps(), _packet.getDstIp())),
            not(toBoolExpr(headerSpace.getNotDstPorts(), _packet.getDstPort())),
            not(toBoolExpr(headerSpace.getNotSrcIps(), _packet.getSrcIp())),
            not(toBoolExpr(headerSpace.getSrcPorts(), _packet.getSrcPort())),
            toBoolExpr(headerSpace.getTcpFlags()),
            toBoolExpr(headerSpace.getIcmpCodes(), _packet.getIcmpCode()),
            toBoolExpr(headerSpace.getIcmpTypes(), _packet.getIcmpType()),
            toBoolExpr(headerSpace.getIpProtocols()));

    return headerSpace.getNegate() ? _context.mkNot(expr) : expr;
  }

  @Override
  public BoolExpr visitMatchSrcInterface(MatchSrcInterface matchSrcInterface) {
    throw new BatfishException("MatchSrcInterface is not supported");
  }

  @Override
  public BoolExpr visitNotMatchExpr(NotMatchExpr notMatchExpr) {
    return _context.mkNot(notMatchExpr.getOperand().accept(this));
  }

  @Override
  public BoolExpr visitOriginatingFromDevice(OriginatingFromDevice originatingFromDevice) {
    throw new BatfishException("OriginatingFromDevice is not supported");
  }

  @Override
  public BoolExpr visitOrMatchExpr(OrMatchExpr orMatchExpr) {
    return _context.mkOr(
        orMatchExpr.getDisjuncts().stream().map(this::toBoolExpr).toArray(BoolExpr[]::new));
  }

  @Override
  public BoolExpr visitPermittedByAcl(PermittedByAcl permittedByAcl) {
    throw new BatfishException("TODO: support PermittedByAcl");
  }

  @Override
  public BoolExpr visitTrueExpr(TrueExpr trueExpr) {
    return _context.mkTrue();
  }
}
