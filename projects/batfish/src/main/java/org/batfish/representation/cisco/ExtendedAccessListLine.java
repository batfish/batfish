package org.batfish.representation.cisco;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.State;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TcpFlags;

public class ExtendedAccessListLine implements Serializable {

  public static class Builder {

    private LineAction _action;

    private Set<Integer> _dscps;

    private ExtendedAccessListAddressSpecifier _dstAddressSpecifier;

    private List<SubRange> _dstPortRanges;

    private Set<Integer> _ecns;

    private Integer _icmpCode;

    private Integer _icmpType;

    private String _name;

    private IpProtocol _protocol;

    private ExtendedAccessListServiceSpecifier _serviceSpecifier;

    public Builder setServiceSpecifier(ExtendedAccessListServiceSpecifier serviceSpecifier) {
      _serviceSpecifier = serviceSpecifier;
      return this;
    }

    private ExtendedAccessListAddressSpecifier _srcAddressSpecifier;

    private List<SubRange> _srcPortRanges;

    private Set<State> _states;

    private List<TcpFlags> _tcpFlags;

    private Builder() {
      _dscps = ImmutableSet.of();
      _dstPortRanges = ImmutableList.of();
      _ecns = ImmutableSet.of();
      _srcPortRanges = ImmutableList.of();
    }

    public Builder setAction(@Nonnull LineAction action) {
      _action = action;
      return this;
    }

    public Builder setDscps(Iterable<Integer> dscps) {
      _dscps = ImmutableSet.copyOf(dscps);
      return this;
    }

    public Builder setDstAddressSpecifier(ExtendedAccessListAddressSpecifier dstAddressSpecifier) {
      _dstAddressSpecifier = dstAddressSpecifier;
      return this;
    }

    public Builder setDstPortRanges(Iterable<SubRange> dstPortRanges) {
      _dstPortRanges = ImmutableList.copyOf(dstPortRanges);
      return this;
    }

    public Builder setEcns(Iterable<Integer> ecns) {
      _ecns = ImmutableSet.copyOf(ecns);
      return this;
    }

    public Builder setIcmpCode(Integer icmpCode) {
      _icmpCode = icmpCode;
      return this;
    }

    public Builder setIcmpType(Integer icmpType) {
      _icmpType = icmpType;
      return this;
    }

    public Builder setName(String name) {
      _name = name;
      return this;
    }

    public Builder setProtocol(IpProtocol protocol) {
      _protocol = protocol;
      return this;
    }

    public Builder setSrcPortRanges(Iterable<SubRange> srcPortRanges) {
      _srcPortRanges = ImmutableList.copyOf(srcPortRanges);
      return this;
    }

    public Builder setStates(Iterable<State> states) {
      _states = ImmutableSet.copyOf(states);
      return this;
    }

    public Builder setTcpFlags(Iterable<TcpFlags> tcpFlags) {
      _tcpFlags = ImmutableList.copyOf(tcpFlags);
      return this;
    }

    public ExtendedAccessListLine build() {
      return new ExtendedAccessListLine(this);
    }
  }

  private static final long serialVersionUID = 1L;

  public static Builder builder() {
    return new Builder();
  }

  private final LineAction _action;

  private final Set<Integer> _dscps;

  private final ExtendedAccessListAddressSpecifier _dstAddressSpecifier;

  private final List<SubRange> _dstPortRanges;

  private final Set<Integer> _ecns;

  private final Integer _icmpCode;

  private final Integer _icmpType;

  private final String _name;

  private final IpProtocol _protocol;

  private final ExtendedAccessListAddressSpecifier _srcAddressSpecifier;

  private final List<SubRange> _srcPortRanges;

  private final Set<State> _states;

  private final List<TcpFlags> _tcpFlags;

  private final ExtendedAccessListServiceSpecifier _serviceSpecifier;

  public ExtendedAccessListServiceSpecifier getServiceSpecifier() {
    return _serviceSpecifier;
  }

  private ExtendedAccessListLine(Builder builder) {
    _action = requireNonNull(builder._action);
    _dscps = builder._dscps;
    _dstAddressSpecifier = builder._dstAddressSpecifier;
    _dstPortRanges = builder._dstPortRanges;
    _ecns = builder._ecns;
    _icmpCode = builder._icmpCode;
    _icmpType = builder._icmpType;
    _name = builder._name;
    _protocol = builder._protocol;
    _serviceSpecifier = builder._serviceSpecifier;
    _srcAddressSpecifier = builder._srcAddressSpecifier;
    _srcPortRanges = builder._srcPortRanges;
    _states = builder._states;
    _tcpFlags = builder._tcpFlags;
  }

  public @Nonnull LineAction getAction() {
    return _action;
  }

  public ExtendedAccessListAddressSpecifier getDestinationAddressSpecifier() {
    return _dstAddressSpecifier;
  }

  public Set<Integer> getDscps() {
    return _dscps;
  }

  public List<SubRange> getDstPorts() {
    return _dstPortRanges;
  }

  public Set<Integer> getEcns() {
    return _ecns;
  }

  public Integer getIcmpCode() {
    return _icmpCode;
  }

  public Integer getIcmpType() {
    return _icmpType;
  }

  public String getName() {
    return _name;
  }

  public IpProtocol getProtocol() {
    return _protocol;
  }

  public ExtendedAccessListAddressSpecifier getSourceAddressSpecifier() {
    return _srcAddressSpecifier;
  }

  public List<SubRange> getSrcPorts() {
    return _srcPortRanges;
  }

  public Set<State> getStates() {
    return _states;
  }

  public List<TcpFlags> getTcpFlags() {
    return _tcpFlags;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass())
        .add("action", _action)
        .add("dscps", _dscps)
        .add("dstAddressSpecicier", _dstAddressSpecifier)
        .add("dstPortRanges", _dstPortRanges)
        .add("ecns", _ecns)
        .add("icmpCode", _icmpCode)
        .add("icmpType", _icmpType)
        .add("name", _name)
        .add("protocol", _protocol)
        .add("serviceSpecifier", _serviceSpecifier)
        .add("srcAddressSpecifier", _srcAddressSpecifier)
        .add("srcPortRanges", _srcPortRanges)
        .add("states", _states)
        .add("tcpFlags", _tcpFlags)
        .toString();
  }
}
