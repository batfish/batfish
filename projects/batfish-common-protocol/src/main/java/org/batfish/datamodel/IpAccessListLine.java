package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.common.base.Objects;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import java.util.List;
import java.util.SortedSet;
import org.batfish.datamodel.expr.BooleanExpr;

@JsonSchemaDescription("A line in an IpAccessList")
public final class IpAccessListLine extends HeaderSpace {

  public static class Builder extends HeaderSpace.Builder<Builder, IpAccessListLine> {

    private LineAction _action;

    private String _name;

    private Builder() {}

    @Override
    public IpAccessListLine build() {
      return new IpAccessListLine(
          _action,
          _dscps,
          _dstIps,
          _dstPorts,
          _dstProtocols,
          _ecns,
          _fragmentOffsets,
          _icmpCodes,
          _icmpTypes,
          _ipProtocols,
          _name,
          _negate,
          _notDscps,
          _notDstIps,
          _notDstPorts,
          _notDstProtocols,
          _notEcns,
          _notFragmentOffsets,
          _notIcmpCodes,
          _notIcmpTypes,
          _notIpProtocols,
          _notPacketLengths,
          _notSrcIps,
          _notSrcPorts,
          _notSrcProtocols,
          _packetLengths,
          _srcIps,
          _srcOrDstIps,
          _srcOrDstPorts,
          _srcOrDstProtocols,
          _srcPorts,
          _srcProtocols,
          _states,
          _tcpFlags);
    }

    @Override
    protected Builder getThis() {
      return this;
    }

    public Builder setAction(LineAction action) {
      _action = action;
      return this;
    }

    public Builder setName(String name) {
      _name = name;
      return this;
    }
  }

  private static final long serialVersionUID = 1L;

  public static Builder builder() {
    return new Builder();
  }

  private LineAction _action;

  private BooleanExpr _matchCondition;

  private String _name;

  public IpAccessListLine(LineAction action, BooleanExpr matchCondition, String name) {
    _action = action;
    _matchCondition = matchCondition;
    _name = name;
  }

  public IpAccessListLine() {}

  public IpAccessListLine(
      LineAction action,
      SortedSet<Integer> dscps,
      SortedSet<IpWildcard> dstIps,
      SortedSet<SubRange> dstPorts,
      SortedSet<Protocol> dstProtocols,
      SortedSet<Integer> ecns,
      SortedSet<SubRange> fragmentOffsets,
      SortedSet<SubRange> icmpCodes,
      SortedSet<SubRange> icmpTypes,
      SortedSet<IpProtocol> ipProtocols,
      String name,
      boolean negate,
      SortedSet<Integer> notDscps,
      SortedSet<IpWildcard> notDstIps,
      SortedSet<SubRange> notDstPorts,
      SortedSet<Protocol> notDstProtocols,
      SortedSet<Integer> notEcns,
      SortedSet<SubRange> notFragmentOffsets,
      SortedSet<SubRange> notIcmpCodes,
      SortedSet<SubRange> notIcmpTypes,
      SortedSet<IpProtocol> notIpProtocols,
      SortedSet<SubRange> notPacketLengths,
      SortedSet<IpWildcard> notSrcIps,
      SortedSet<SubRange> notSrcPorts,
      SortedSet<Protocol> notSrcProtocols,
      SortedSet<SubRange> packetLengths,
      SortedSet<IpWildcard> srcIps,
      SortedSet<IpWildcard> srcOrDstIps,
      SortedSet<SubRange> srcOrDstPorts,
      SortedSet<Protocol> srcOrDstProtocols,
      SortedSet<SubRange> srcPorts,
      SortedSet<Protocol> srcProtocols,
      SortedSet<State> states,
      List<TcpFlags> tcpFlags) {
    super(
        dscps,
        dstIps,
        dstPorts,
        dstProtocols,
        ecns,
        fragmentOffsets,
        icmpCodes,
        icmpTypes,
        ipProtocols,
        negate,
        notDscps,
        notDstIps,
        notDstPorts,
        notDstProtocols,
        notEcns,
        notFragmentOffsets,
        notIcmpCodes,
        notIcmpTypes,
        notIpProtocols,
        notPacketLengths,
        notSrcIps,
        notSrcPorts,
        notSrcProtocols,
        packetLengths,
        srcIps,
        srcOrDstIps,
        srcOrDstPorts,
        srcOrDstProtocols,
        srcPorts,
        srcProtocols,
        states,
        tcpFlags);
    _action = action;
    _name = name;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof IpAccessListLine)) {
      return false;
    }
    IpAccessListLine other = (IpAccessListLine) obj;
    return _action == other._action && Objects.equal(_name, other._name) && super.equals(other);
  }

  @JsonPropertyDescription(
      "The action the underlying access-list will take when this line matches an IPV4 packet.")
  public LineAction getAction() {
    return _action;
  }

  @JsonPropertyDescription("The condition(s) for this line to match.")
  public BooleanExpr getMatchCondition() {
    return _matchCondition;
  }

  @JsonSchemaDescription("The name of this line in the list")
  public String getName() {
    return _name;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        _action,
        getDscps(),
        getDstIps(),
        getDstPorts(),
        getDstProtocols(),
        getEcns(),
        getFragmentOffsets(),
        getIcmpCodes(),
        getIcmpTypes(),
        getIpProtocols(),
        _name,
        getNegate(),
        getNotDscps(),
        getNotDstIps(),
        getNotDstPorts(),
        getNotDstProtocols(),
        getNotEcns(),
        getNotFragmentOffsets(),
        getNotIcmpCodes(),
        getNotIcmpTypes(),
        getNotIpProtocols(),
        getNotPacketLengths(),
        getNotSrcIps(),
        getNotSrcPorts(),
        getNotSrcProtocols(),
        getPacketLengths(),
        getSrcIps(),
        getSrcOrDstIps(),
        getSrcOrDstPorts(),
        getSrcOrDstProtocols(),
        getSrcPorts(),
        getSrcProtocols(),
        getStates(),
        getTcpFlags());
  }

  /** @return A Builder with fields set to this' fields. */
  public IpAccessListLine.Builder rebuild() {
    IpAccessListLine.Builder builder = builder();
    super.rebuild(builder);
    builder.setAction(_action).setName(_name);
    return builder;
  }

  public void setAction(LineAction action) {
    _action = action;
  }

  public void setMatchCondition(BooleanExpr matchCondition) {
    _matchCondition = matchCondition;
  }

  public void setName(String name) {
    _name = name;
  }

  @Override
  public String toString() {
    return "[Action:" + _action + ", Base: " + super.toString() + "]";
  }
}
