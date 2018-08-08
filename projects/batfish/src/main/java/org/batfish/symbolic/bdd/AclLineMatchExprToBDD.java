package org.batfish.symbolic.bdd;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.math.LongMath;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.common.BatfishException;
import org.batfish.common.util.CommonUtil;
import org.batfish.common.util.NonRecursiveSupplier.NonRecursiveSupplierException;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.State;
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
import org.batfish.datamodel.acl.OriginatingFromDevice;
import org.batfish.datamodel.acl.PermittedByAcl;
import org.batfish.datamodel.acl.TrueExpr;

/** Visit an {@link AclLineMatchExpr} and convert it to a BDD. */
public class AclLineMatchExprToBDD implements GenericAclLineMatchExprVisitor<BDD> {

  private final Map<String, Supplier<BDD>> _aclEnv;

  private final BDDOps _bddOps;

  private final BDDFactory _factory;

  private final @Nullable Map<String, BDD> _matchSrcInterfaceBDDs;

  private Map<String, IpSpace> _namedIpSpaces;

  private final @Nullable BDD _originatingFromDevice;

  private final BDDPacket _packet;

  /**
   * This constructor does not support MatchSrcInterface or OriginatingFromDevice. Consider it
   * "soft-deprecated".
   */
  public AclLineMatchExprToBDD(
      BDDFactory factory,
      BDDPacket packet,
      Map<String, Supplier<BDD>> aclEnv,
      Map<String, IpSpace> namedIpSpaces) {
    _aclEnv = ImmutableMap.copyOf(aclEnv);
    _factory = factory;
    _bddOps = new BDDOps(factory);
    _matchSrcInterfaceBDDs = null;
    _namedIpSpaces = ImmutableMap.copyOf(namedIpSpaces);
    _originatingFromDevice = null;
    _packet = packet;
  }

  /**
   * This constructor supports all {@link AclLineMatchExpr AclLineMatchExprs}.
   *
   * @param factory
   * @param packet
   * @param aclEnv
   * @param namedIpSpaces
   * @param originatingFromDevice
   * @param srcInterfaceVar A {@link BDDInteger} of length at least
   *     ceiling(Log_2(srcInterfaces.length() + 1)). This is used to identify the source interface
   *     of the packet. The +1 is required to reserve an extra value (0) for "no source interface"
   *     (i.e. the packet originated from the device).
   * @param srcInterfaces A {@link List<String>} of the known interface names. {@link
   *     MatchSrcInterface} expressions can match any name in this list (and only those names).
   */
  public AclLineMatchExprToBDD(
      BDDFactory factory,
      BDDPacket packet,
      Map<String, Supplier<BDD>> aclEnv,
      Map<String, IpSpace> namedIpSpaces,
      @Nonnull BDD originatingFromDevice,
      BDDInteger srcInterfaceVar,
      List<String> srcInterfaces) {
    _aclEnv = ImmutableMap.copyOf(aclEnv);
    _factory = factory;
    _bddOps = new BDDOps(factory);
    _matchSrcInterfaceBDDs = computeMatchSrcInterfaceBDDs(srcInterfaceVar, srcInterfaces);
    _namedIpSpaces = ImmutableMap.copyOf(namedIpSpaces);
    _originatingFromDevice = originatingFromDevice;
    _packet = packet;
  }

  /**
   * A packet can enter a router from at most 1 interface, possibly none (if the packet originated
   * at the router). So for N srcInterfaces, we need N+1 distinct values: one for each interface,
   * and one for "none of them". We use 0 for "none of them".
   */
  private static Map<String, BDD> computeMatchSrcInterfaceBDDs(
      BDDInteger srcInterfaceVar, List<String> srcInterfaces) {
    int bitsAllocated = srcInterfaceVar.getBitvec().length;
    int bitsRequired = LongMath.log2(srcInterfaces.size() + 1, RoundingMode.CEILING);
    if (bitsAllocated < bitsRequired) {
      throw new IllegalArgumentException(
          String.format(
              "Not enough bits in srcInterfaceVar (%d) for %d srcIntefaces. Need %d",
              bitsAllocated, srcInterfaces.size(), bitsRequired));
    }
    ImmutableMap.Builder<String, BDD> matchSrcInterfaceBDDs = ImmutableMap.builder();
    CommonUtil.forEachWithIndex(
        srcInterfaces,
        (idx, iface) -> matchSrcInterfaceBDDs.put(iface, srcInterfaceVar.value(idx + 1)));
    return matchSrcInterfaceBDDs.build();
  }

  private @Nullable BDD toBDD(Collection<Integer> ints, BDDInteger var) {
    if (ints == null || ints.isEmpty()) {
      return null;
    }
    return _bddOps.or(ints.stream().map(var::value).collect(Collectors.toList()));
  }

  private @Nullable BDD toBDD(@Nullable IpSpace ipSpace, BDDInteger var) {
    if (ipSpace == null) {
      return null;
    }
    return ipSpace.accept(new IpSpaceToBDD(_factory, var, _namedIpSpaces));
  }

  private @Nullable BDD toBDD(Set<IpProtocol> ipProtocols) {
    if (ipProtocols == null || ipProtocols.isEmpty()) {
      return null;
    }

    return _bddOps.or(
        ipProtocols
            .stream()
            .map(IpProtocol::number)
            .map(_packet.getIpProtocol()::value)
            .collect(Collectors.toList()));
  }

  private @Nullable BDD toBDD(@Nullable Set<SubRange> ranges, BDDInteger var) {
    if (ranges == null || ranges.isEmpty()) {
      return null;
    }
    return _bddOps.or(
        ranges.stream().map(range -> toBDD(range, var)).collect(ImmutableList.toImmutableList()));
  }

  private static BDD toBDD(SubRange range, BDDInteger var) {
    long start = range.getStart();
    long end = range.getEnd();
    return start == end ? var.value(start) : var.geq(start).and(var.leq(end));
  }

  private BDD toBDD(List<TcpFlags> tcpFlags) {
    if (tcpFlags == null || tcpFlags.isEmpty()) {
      return null;
    }

    return _bddOps.or(tcpFlags.stream().map(this::toBDD).collect(ImmutableList.toImmutableList()));
  }

  /** For TcpFlags */
  private static BDD toBDD(boolean useFlag, boolean flagValue, BDD flagBDD) {
    return useFlag ? flagValue ? flagBDD : flagBDD.not() : null;
  }

  private BDD toBDD(TcpFlags tcpFlags) {
    return _bddOps.and(
        toBDD(tcpFlags.getUseAck(), tcpFlags.getAck(), _packet.getTcpAck()),
        toBDD(tcpFlags.getUseCwr(), tcpFlags.getCwr(), _packet.getTcpCwr()),
        toBDD(tcpFlags.getUseEce(), tcpFlags.getEce(), _packet.getTcpEce()),
        toBDD(tcpFlags.getUseFin(), tcpFlags.getFin(), _packet.getTcpFin()),
        toBDD(tcpFlags.getUsePsh(), tcpFlags.getPsh(), _packet.getTcpPsh()),
        toBDD(tcpFlags.getUseRst(), tcpFlags.getRst(), _packet.getTcpRst()),
        toBDD(tcpFlags.getUseSyn(), tcpFlags.getSyn(), _packet.getTcpSyn()),
        toBDD(tcpFlags.getUseUrg(), tcpFlags.getUrg(), _packet.getTcpUrg()));
  }

  @Override
  public BDD visitAndMatchExpr(AndMatchExpr andMatchExpr) {
    return _bddOps.and(
        andMatchExpr
            .getConjuncts()
            .stream()
            .map(this::visit)
            .collect(ImmutableList.toImmutableList()));
  }

  @Override
  public BDD visitFalseExpr(FalseExpr falseExpr) {
    return _factory.zero();
  }

  @Override
  public BDD visitMatchHeaderSpace(MatchHeaderSpace matchHeaderSpace) {
    HeaderSpace headerSpace = matchHeaderSpace.getHeaderspace();
    return _bddOps.and(
        toBDD(headerSpace.getDstIps(), _packet.getDstIp()),
        BDDOps.negateIfNonNull(toBDD(headerSpace.getNotDstIps(), _packet.getDstIp())),
        toBDD(headerSpace.getSrcIps(), _packet.getSrcIp()),
        BDDOps.negateIfNonNull(toBDD(headerSpace.getNotSrcIps(), _packet.getSrcIp())),
        BDDOps.orNull(
            toBDD(headerSpace.getSrcOrDstIps(), _packet.getDstIp()),
            toBDD(headerSpace.getSrcOrDstIps(), _packet.getSrcIp())),
        toBDD(headerSpace.getDstPorts(), _packet.getDstPort()),
        BDDOps.negateIfNonNull(toBDD(headerSpace.getNotDstPorts(), _packet.getDstPort())),
        toBDD(headerSpace.getSrcPorts(), _packet.getSrcPort()),
        BDDOps.negateIfNonNull(toBDD(headerSpace.getNotSrcPorts(), _packet.getSrcPort())),
        BDDOps.orNull(
            toBDD(headerSpace.getSrcOrDstPorts(), _packet.getSrcPort()),
            toBDD(headerSpace.getSrcOrDstPorts(), _packet.getDstPort())),
        toBDD(headerSpace.getTcpFlags()),
        toBDD(headerSpace.getIcmpCodes(), _packet.getIcmpCode()),
        BDDOps.negateIfNonNull(toBDD(headerSpace.getNotIcmpCodes(), _packet.getIcmpCode())),
        toBDD(headerSpace.getIcmpTypes(), _packet.getIcmpType()),
        BDDOps.negateIfNonNull(toBDD(headerSpace.getNotIcmpTypes(), _packet.getIcmpType())),
        toBDD(headerSpace.getIpProtocols()),
        BDDOps.negateIfNonNull(toBDD(headerSpace.getNotIpProtocols())),
        toBDD(headerSpace.getDscps(), _packet.getDscp()),
        BDDOps.negateIfNonNull(toBDD(headerSpace.getNotDscps(), _packet.getDscp())),
        toBDD(headerSpace.getEcns(), _packet.getEcn()),
        BDDOps.negateIfNonNull(toBDD(headerSpace.getNotEcns(), _packet.getEcn())),
        toBDD(headerSpace.getFragmentOffsets(), _packet.getFragmentOffset()),
        BDDOps.negateIfNonNull(
            toBDD(headerSpace.getNotFragmentOffsets(), _packet.getFragmentOffset())),
        toBDD(
            headerSpace.getStates().stream().map(State::number).collect(Collectors.toList()),
            _packet.getState()));
  }

  @Override
  public BDD visitMatchSrcInterface(MatchSrcInterface matchSrcInterface) {
    if (_matchSrcInterfaceBDDs == null) {
      throw new BatfishException("MatchSrcInterface is unsupported");
    }
    matchSrcInterface
        .getSrcInterfaces()
        .forEach(
            iface ->
                Preconditions.checkArgument(
                    _matchSrcInterfaceBDDs.containsKey(iface),
                    "MatchSrcInterface: unknown interface " + iface));
    return _bddOps.or(
        matchSrcInterface
            .getSrcInterfaces()
            .stream()
            .map(_matchSrcInterfaceBDDs::get)
            .collect(Collectors.toList()));
  }

  @Override
  public BDD visitNotMatchExpr(NotMatchExpr notMatchExpr) {
    return notMatchExpr.getOperand().accept(this).not();
  }

  @Override
  public BDD visitOriginatingFromDevice(OriginatingFromDevice originatingFromDevice) {
    if (_originatingFromDevice == null) {
      throw new BatfishException("OriginatingFromDevice is unsupported");
    }
    return _originatingFromDevice;
  }

  @Override
  public BDD visitOrMatchExpr(OrMatchExpr orMatchExpr) {
    return _bddOps.or(
        orMatchExpr
            .getDisjuncts()
            .stream()
            .map(this::visit)
            .collect(ImmutableList.toImmutableList()));
  }

  @Override
  public BDD visitPermittedByAcl(PermittedByAcl permittedByAcl) {
    String name = permittedByAcl.getAclName();
    Preconditions.checkArgument(
        _aclEnv.containsKey(name), "Undefined PermittedByAcl reference: %s", name);
    try {
      return _aclEnv.get(name).get();
    } catch (NonRecursiveSupplierException e) {
      throw new BatfishException("Circular PermittedByAcl reference: " + name);
    }
  }

  @Override
  public BDD visitTrueExpr(TrueExpr trueExpr) {
    return _factory.one();
  }
}
