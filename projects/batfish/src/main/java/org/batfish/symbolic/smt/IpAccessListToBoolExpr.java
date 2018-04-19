package org.batfish.symbolic.smt;

import com.google.common.collect.Lists;
import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.BitVecExpr;
import com.microsoft.z3.BoolExpr;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TcpFlags;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AndMatchExpr;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.datamodel.acl.GenericAclLineMatchExprVisitor;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.MatchSrcInterface;
import org.batfish.datamodel.acl.NotMatchExpr;
import org.batfish.datamodel.acl.OrMatchExpr;
import org.batfish.datamodel.acl.PermittedByAcl;
import org.batfish.datamodel.acl.TrueExpr;

public class IpAccessListToBoolExpr implements GenericAclLineMatchExprVisitor<BoolExpr> {

  private final Encoder _encoder;

  private final SymbolicPacket _packet;

  public IpAccessListToBoolExpr(Encoder encoder, SymbolicPacket packet) {
    _encoder = encoder;
    _packet = packet;
  }

  private BoolExpr toBoolExpr(AclLineMatchExpr matchExpr) {
    return matchExpr.accept(this);
  }

  public BoolExpr toBoolExpr(IpAccessList ipAccessList) {
    BoolExpr expr = _encoder.mkFalse();
    for (IpAccessListLine line : Lists.reverse(ipAccessList.getLines())) {
      BoolExpr matchExpr = line.getMatchCondition().accept(this);
      BoolExpr actionExpr =
          line.getAction() == LineAction.ACCEPT ? _encoder.mkTrue() : _encoder.mkFalse();
      expr = _encoder.mkIf(matchExpr, actionExpr, expr);
    }
    return expr;
  }

  private @Nullable BoolExpr toBoolExpr(@Nullable IpSpace ipSpace, BitVecExpr var) {
    if (ipSpace == null) {
      return null;
    }
    return ipSpace.accept(new IpSpaceToBoolExpr(_encoder, var));
  }

  private @Nullable BoolExpr toBoolExpr(Set<IpProtocol> ipProtocols) {
    if (ipProtocols == null || ipProtocols.isEmpty()) {
      return null;
    }

    return _encoder.mkOr(
        ipProtocols
            .stream()
            .map(
                ipProtocol ->
                    _encoder.mkEq(_packet.getIpProtocol(), _encoder.mkInt(ipProtocol.number())))
            .toArray(BoolExpr[]::new));
  }

  private @Nullable BoolExpr toBoolExpr(@Nullable Set<SubRange> ranges, ArithExpr var) {
    if (ranges == null || ranges.isEmpty()) {
      return null;
    }
    return _encoder.mkOr(
        ranges.stream().map(range -> toBoolExpr(range, var)).toArray(BoolExpr[]::new));
  }

  private BoolExpr toBoolExpr(SubRange range, ArithExpr var) {
    int start = range.getStart();
    int end = range.getEnd();
    ArithExpr startExpr = _encoder.mkInt(start);
    ArithExpr endExpr = _encoder.mkInt(end);
    return start == end
        ? _encoder.mkEq(var, startExpr)
        : _encoder.mkAnd(_encoder.mkGe(var, startExpr), _encoder.mkLe(var, endExpr));
  }

  private BoolExpr toBoolExpr(List<TcpFlags> tcpFlags) {
    return _encoder.mkOr(tcpFlags.stream().map(this::toBoolExpr).toArray(BoolExpr[]::new));
  }

  /** For TcpFlags */
  private BoolExpr toBoolExpr(boolean useFlag, boolean flagValue, BoolExpr flagExpr) {
    return useFlag ? flagValue ? flagExpr : _encoder.mkNot(flagExpr) : null;
  }

  private BoolExpr toBoolExpr(TcpFlags tcpFlags) {
    return _encoder.mkAnd(
        toBoolExpr(tcpFlags.getUseAck(), tcpFlags.getAck(), _packet.getTcpAck()),
        toBoolExpr(tcpFlags.getUseCwr(), tcpFlags.getCwr(), _packet.getTcpCwr()),
        toBoolExpr(tcpFlags.getUseEce(), tcpFlags.getEce(), _packet.getTcpEce()),
        toBoolExpr(tcpFlags.getUseFin(), tcpFlags.getFin(), _packet.getTcpFin()),
        toBoolExpr(tcpFlags.getUsePsh(), tcpFlags.getPsh(), _packet.getTcpPsh()),
        toBoolExpr(tcpFlags.getUseRst(), tcpFlags.getRst(), _packet.getTcpRst()),
        toBoolExpr(tcpFlags.getUseSyn(), tcpFlags.getSyn(), _packet.getTcpSyn()),
        toBoolExpr(tcpFlags.getUseUrg(), tcpFlags.getUrg(), _packet.getTcpUrg()));
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
    return _encoder.mkAnd(
        andMatchExpr.getConjuncts().stream().map(this::toBoolExpr).toArray(BoolExpr[]::new));
  }

  @Override
  public BoolExpr visitFalseExpr(FalseExpr falseExpr) {
    return _encoder.mkFalse();
  }

  @Override
  public BoolExpr visitMatchHeaderSpace(MatchHeaderSpace matchHeaderSpace) {
    HeaderSpace headerSpace = matchHeaderSpace.getHeaderspace();
    forbidHeaderSpaceField("dscps", headerSpace.getDscps());
    forbidHeaderSpaceField("ecns", headerSpace.getEcns());
    forbidHeaderSpaceField("fragmentOffsets", headerSpace.getFragmentOffsets());
    forbidHeaderSpaceField("notDscps", headerSpace.getNotDscps());
    forbidHeaderSpaceField("notDstIps", headerSpace.getNotDstIps());
    forbidHeaderSpaceField("notDstPorts", headerSpace.getNotDstPorts());
    forbidHeaderSpaceField("notEcns", headerSpace.getNotEcns());
    forbidHeaderSpaceField("notFragmentOffsets", headerSpace.getNotFragmentOffsets());
    forbidHeaderSpaceField("notIcmpCodes", headerSpace.getNotIcmpCodes());
    forbidHeaderSpaceField("notIcmpTypes", headerSpace.getNotIcmpTypes());
    forbidHeaderSpaceField("notIpProtocols", headerSpace.getNotIpProtocols());
    forbidHeaderSpaceField("notSrcIps", headerSpace.getNotSrcIps());
    forbidHeaderSpaceField("notSrcPorts", headerSpace.getNotSrcPorts());
    forbidHeaderSpaceField("states", headerSpace.getStates());

    return _encoder.mkAnd(
        toBoolExpr(headerSpace.getDstIps(), _packet.getDstIp()),
        toBoolExpr(headerSpace.getSrcIps(), _packet.getSrcIp()),
        toBoolExpr(headerSpace.getDstPorts(), _packet.getDstPort()),
        toBoolExpr(headerSpace.getSrcPorts(), _packet.getSrcPort()),
        toBoolExpr(headerSpace.getTcpFlags()),
        toBoolExpr(headerSpace.getIcmpCodes(), _packet.getIcmpCode()),
        toBoolExpr(headerSpace.getIcmpTypes(), _packet.getIcmpType()),
        toBoolExpr(headerSpace.getIpProtocols()));
  }

  @Override
  public BoolExpr visitMatchSrcInterface(MatchSrcInterface matchSrcInterface) {
    throw new BatfishException("MatchSrcInterface is not supported");
  }

  @Override
  public BoolExpr visitNotMatchExpr(NotMatchExpr notMatchExpr) {
    return _encoder.mkNot(notMatchExpr.getOperand().accept(this));
  }

  @Override
  public BoolExpr visitOrMatchExpr(OrMatchExpr orMatchExpr) {
    return _encoder.mkOr(
        orMatchExpr.getDisjuncts().stream().map(this::toBoolExpr).toArray(BoolExpr[]::new));
  }

  @Override
  public BoolExpr visitPermittedByAcl(PermittedByAcl permittedByAcl) {
    throw new BatfishException("TODO: support PermittedByAcl");
  }

  @Override
  public BoolExpr visitTrueExpr(TrueExpr trueExpr) {
    return _encoder.mkTrue();
  }
}
