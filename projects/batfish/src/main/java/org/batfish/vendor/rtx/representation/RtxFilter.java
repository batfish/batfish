package org.batfish.vendor.rtx.representation;

import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.LineAction;

/**
 * One {@code ip filter} definition: a match condition plus an action.
 *
 * <p>A {@code null} field means the corresponding header field is unconstrained, which is what the
 * wildcard {@code *} and an omitted trailing argument both mean.
 */
public final class RtxFilter implements Serializable {

  private final long _number;
  private final @Nonnull LineAction _action;
  private final @Nullable IpSpace _srcIps;
  private final @Nullable IpSpace _dstIps;
  private final @Nonnull Set<IpProtocol> _protocols;
  private final boolean _established;
  private final @Nullable IntegerSpace _srcPorts;
  private final @Nullable IntegerSpace _dstPorts;
  private final @Nullable IntegerSpace _icmpTypes;
  private final @Nullable IntegerSpace _icmpCodes;

  public RtxFilter(
      long number,
      LineAction action,
      @Nullable IpSpace srcIps,
      @Nullable IpSpace dstIps,
      Set<IpProtocol> protocols,
      boolean established,
      @Nullable IntegerSpace srcPorts,
      @Nullable IntegerSpace dstPorts,
      @Nullable IntegerSpace icmpTypes,
      @Nullable IntegerSpace icmpCodes) {
    _number = number;
    _action = action;
    _srcIps = srcIps;
    _dstIps = dstIps;
    _protocols = ImmutableSet.copyOf(protocols);
    _established = established;
    _srcPorts = srcPorts;
    _dstPorts = dstPorts;
    _icmpTypes = icmpTypes;
    _icmpCodes = icmpCodes;
  }

  public long getNumber() {
    return _number;
  }

  public @Nonnull LineAction getAction() {
    return _action;
  }

  public @Nullable IpSpace getSrcIps() {
    return _srcIps;
  }

  public @Nullable IpSpace getDstIps() {
    return _dstIps;
  }

  /** The IP protocols this filter matches; empty means any protocol. */
  public @Nonnull Set<IpProtocol> getProtocols() {
    return _protocols;
  }

  /** Whether the protocol argument was the {@code established} keyword. */
  public boolean getEstablished() {
    return _established;
  }

  public @Nullable IntegerSpace getSrcPorts() {
    return _srcPorts;
  }

  public @Nullable IntegerSpace getDstPorts() {
    return _dstPorts;
  }

  public @Nullable IntegerSpace getIcmpTypes() {
    return _icmpTypes;
  }

  public @Nullable IntegerSpace getIcmpCodes() {
    return _icmpCodes;
  }
}
