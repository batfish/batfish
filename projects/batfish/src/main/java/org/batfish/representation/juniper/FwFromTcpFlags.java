package org.batfish.representation.juniper;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.TcpFlags;
import org.batfish.datamodel.TcpFlagsMatchConditions;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.representation.juniper.FwTerm.Field;

/** Class for firewall filter from tcp-flags */
public final class FwFromTcpFlags implements FwFrom {

  /**
   * Three types of commands used in firewall filter:
   *
   * <p>1. from tcp-established
   *
   * <p>2. from tcp-initial
   *
   * <p>3. from tcp-flags flags
   */
  enum CommandType {
    ESTABLISHED,
    FLAGS,
    INITIAL
  }

  /** from tcp-established */
  public static final FwFromTcpFlags TCP_ESTABLISHED =
      new FwFromTcpFlags(
          ImmutableList.of(
              TcpFlagsMatchConditions.builder()
                  .setTcpFlags(TcpFlags.builder().setAck(true).build())
                  .setUseAck(true)
                  .build(),
              TcpFlagsMatchConditions.builder()
                  .setTcpFlags(TcpFlags.builder().setRst(true).build())
                  .setUseRst(true)
                  .build()),
          CommandType.ESTABLISHED);

  /** from tcp-initial */
  public static final FwFromTcpFlags TCP_INITIAL =
      new FwFromTcpFlags(
          ImmutableList.of(
              TcpFlagsMatchConditions.builder()
                  .setTcpFlags(TcpFlags.builder().setAck(true).setSyn(true).build())
                  .setUseAck(true)
                  .setUseSyn(true)
                  .build()),
          CommandType.INITIAL);

  /** from tcp-flags */
  public static FwFromTcpFlags fromTcpFlags(List<TcpFlagsMatchConditions> tcpFlags) {
    return new FwFromTcpFlags(tcpFlags, CommandType.FLAGS);
  }

  private List<TcpFlagsMatchConditions> _tcpFlags;
  private CommandType _commandType;

  private FwFromTcpFlags(List<TcpFlagsMatchConditions> tcpFlags, CommandType commandType) {
    _tcpFlags = tcpFlags;
    _commandType = commandType;
  }

  @Override
  public AclLineMatchExpr toAclLineMatchExpr(JuniperConfiguration jc, Configuration c, Warnings w) {
    return new MatchHeaderSpace(toHeaderSpace(), getTraceElement());
  }

  @Override
  public Field getField() {
    return Field.TCP_FLAG;
  }

  private HeaderSpace toHeaderSpace() {
    return HeaderSpace.builder().setIpProtocols(IpProtocol.TCP).setTcpFlags(_tcpFlags).build();
  }

  private TraceElement getTraceElement() {
    String tcpFlagString =
        switch (_commandType) {
          case ESTABLISHED -> "tcp-established";
          case INITIAL -> "tcp-initial";
          case FLAGS -> // TODO: better description
              String.join(
                  " ",
                  _tcpFlags.stream()
                      .map(TcpFlagsMatchConditions::toString)
                      .collect(ImmutableList.toImmutableList()));
        };

    return TraceElement.of(String.format("Matched tcp-flags %s", tcpFlagString));
  }
}
