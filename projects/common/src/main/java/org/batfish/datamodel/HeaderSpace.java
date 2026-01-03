package org.batfish.datamodel;

import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Comparators;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Ordering;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;
import javax.annotation.Nullable;

public class HeaderSpace implements Serializable, Comparable<HeaderSpace> {

  /** default packet length */
  public static final int DEFAULT_PACKET_LENGTH = 512;

  private static <C extends Collection<?>> C nullIfEmpty(C collection) {
    return collection == null ? null : collection.isEmpty() ? null : collection;
  }

  public static class Builder {
    private SortedSet<Integer> _dscps;
    private @Nullable IpSpace _dstIps;
    private SortedSet<SubRange> _dstPorts;
    private SortedSet<Integer> _ecns;
    private SortedSet<SubRange> _fragmentOffsets;
    private SortedSet<SubRange> _icmpCodes;
    private SortedSet<SubRange> _icmpTypes;
    private SortedSet<IpProtocol> _ipProtocols;
    private boolean _negate;
    private SortedSet<Integer> _notDscps;
    private @Nullable IpSpace _notDstIps;
    private SortedSet<SubRange> _notDstPorts;
    private SortedSet<Integer> _notEcns;
    private SortedSet<SubRange> _notFragmentOffsets;
    private SortedSet<SubRange> _notIcmpCodes;
    private SortedSet<SubRange> _notIcmpTypes;
    private SortedSet<IpProtocol> _notIpProtocols;
    private SortedSet<SubRange> _notPacketLengths;
    private @Nullable IpSpace _notSrcIps;
    private SortedSet<SubRange> _notSrcPorts;
    private SortedSet<Protocol> _notSrcProtocols;
    private SortedSet<SubRange> _packetLengths;
    private @Nullable IpSpace _srcIps;
    private @Nullable IpSpace _srcOrDstIps;
    private SortedSet<SubRange> _srcOrDstPorts;
    private SortedSet<Protocol> _srcOrDstProtocols;
    private SortedSet<SubRange> _srcPorts;
    private SortedSet<Protocol> _srcProtocols;
    private List<TcpFlagsMatchConditions> _tcpFlags;

    private Builder() {
      _dscps = ImmutableSortedSet.of();
      _dstPorts = ImmutableSortedSet.of();
      _ecns = ImmutableSortedSet.of();
      _fragmentOffsets = ImmutableSortedSet.of();
      _icmpCodes = ImmutableSortedSet.of();
      _icmpTypes = ImmutableSortedSet.of();
      _ipProtocols = ImmutableSortedSet.of();
      _packetLengths = ImmutableSortedSet.of();
      _srcOrDstPorts = ImmutableSortedSet.of();
      _srcOrDstProtocols = ImmutableSortedSet.of();
      _srcPorts = ImmutableSortedSet.of();
      _srcProtocols = ImmutableSortedSet.of();
      _tcpFlags = ImmutableList.of();
      _notDscps = ImmutableSortedSet.of();
      _notDstPorts = ImmutableSortedSet.of();
      _notEcns = ImmutableSortedSet.of();
      _notFragmentOffsets = ImmutableSortedSet.of();
      _notIcmpCodes = ImmutableSortedSet.of();
      _notIcmpTypes = ImmutableSortedSet.of();
      _notIpProtocols = ImmutableSortedSet.of();
      _notPacketLengths = ImmutableSortedSet.of();
      _notSrcPorts = ImmutableSortedSet.of();
      _notSrcProtocols = ImmutableSortedSet.of();
    }

    public void addDstIp(IpSpace dstIp) {
      _dstIps = AclIpSpace.union(_dstIps, dstIp);
    }

    public void addSrcIp(IpSpace srcIp) {
      _srcIps = AclIpSpace.union(_srcIps, srcIp);
    }

    public void addNotDstIp(IpSpace notDstIp) {
      _notDstIps = AclIpSpace.union(_notDstIps, notDstIp);
    }

    public void addNotSrcIp(IpSpace notSrcIp) {
      _notSrcIps = AclIpSpace.union(_notSrcIps, notSrcIp);
    }

    public void addSrcOrDstIp(IpSpace srcOrDstIp) {
      _srcOrDstIps = AclIpSpace.union(_srcOrDstIps, srcOrDstIp);
    }

    public HeaderSpace build() {
      return new HeaderSpace(this);
    }

    public SortedSet<Integer> getDscps() {
      return _dscps;
    }

    public @Nullable IpSpace getDstIps() {
      return _dstIps;
    }

    public SortedSet<SubRange> getDstPorts() {
      return _dstPorts;
    }

    public SortedSet<Integer> getEcns() {
      return _ecns;
    }

    public SortedSet<SubRange> getFragmentOffsets() {
      return _fragmentOffsets;
    }

    public SortedSet<SubRange> getIcmpCodes() {
      return _icmpCodes;
    }

    public SortedSet<SubRange> getIcmpTypes() {
      return _icmpTypes;
    }

    public SortedSet<IpProtocol> getIpProtocols() {
      return _ipProtocols;
    }

    public boolean getNegate() {
      return _negate;
    }

    public SortedSet<Integer> getNotDscps() {
      return _notDscps;
    }

    public @Nullable IpSpace getNotDstIps() {
      return _notDstIps;
    }

    public SortedSet<SubRange> getNotDstPorts() {
      return _notDstPorts;
    }

    public SortedSet<Integer> getNotEcns() {
      return _notEcns;
    }

    public SortedSet<SubRange> getNotFragmentOffsets() {
      return _notFragmentOffsets;
    }

    public SortedSet<SubRange> getNotIcmpCodes() {
      return _notIcmpCodes;
    }

    public SortedSet<SubRange> getNotIcmpTypes() {
      return _notIcmpTypes;
    }

    public SortedSet<IpProtocol> getNotIpProtocols() {
      return _notIpProtocols;
    }

    public SortedSet<SubRange> getNotPacketLengths() {
      return _notPacketLengths;
    }

    public @Nullable IpSpace getNotSrcIps() {
      return _notSrcIps;
    }

    public SortedSet<SubRange> getNotSrcPorts() {
      return _notSrcPorts;
    }

    public SortedSet<Protocol> getNotSrcProtocols() {
      return _notSrcProtocols;
    }

    public SortedSet<SubRange> getPacketLengths() {
      return _packetLengths;
    }

    public @Nullable IpSpace getSrcIps() {
      return _srcIps;
    }

    public @Nullable IpSpace getSrcOrDstIps() {
      return _srcOrDstIps;
    }

    public SortedSet<SubRange> getSrcOrDstPorts() {
      return _srcOrDstPorts;
    }

    public SortedSet<Protocol> getSrcOrDstProtocols() {
      return _srcOrDstProtocols;
    }

    public SortedSet<SubRange> getSrcPorts() {
      return _srcPorts;
    }

    public SortedSet<Protocol> getSrcProtocols() {
      return _srcProtocols;
    }

    public List<TcpFlagsMatchConditions> getTcpFlags() {
      return _tcpFlags;
    }

    public Builder setDscps(Iterable<Integer> dscps) {
      _dscps = ImmutableSortedSet.copyOf(dscps);
      return this;
    }

    public Builder setDstIps(Iterable<IpWildcard> dstIps) {
      _dstIps = IpWildcardSetIpSpace.builder().including(dstIps).build();
      return this;
    }

    public Builder setDstIps(IpSpace dstIps) {
      _dstIps = dstIps;
      return this;
    }

    public Builder setDstPorts(SubRange... dstPorts) {
      return setDstPorts(Arrays.asList(dstPorts));
    }

    public Builder setDstPorts(Iterable<SubRange> dstPorts) {
      _dstPorts = ImmutableSortedSet.copyOf(dstPorts);
      return this;
    }

    public Builder setEcns(Iterable<Integer> ecns) {
      _ecns = ImmutableSortedSet.copyOf(ecns);
      return this;
    }

    public Builder setFragmentOffsets(Iterable<SubRange> fragmentOffsets) {
      _fragmentOffsets = ImmutableSortedSet.copyOf(fragmentOffsets);
      return this;
    }

    public Builder setIcmpCodes(Iterable<SubRange> icmpCodes) {
      _icmpCodes = ImmutableSortedSet.copyOf(icmpCodes);
      return this;
    }

    public Builder setIcmpCodes(SubRange... icmpCodes) {
      _icmpCodes = ImmutableSortedSet.copyOf(icmpCodes);
      return this;
    }

    public Builder setIcmpCodes(int... icmpCodes) {
      _icmpCodes =
          Arrays.stream(icmpCodes)
              .mapToObj(SubRange::singleton)
              .collect(ImmutableSortedSet.toImmutableSortedSet(Ordering.natural()));
      return this;
    }

    public Builder setIcmpTypes(Iterable<SubRange> icmpTypes) {
      _icmpTypes = ImmutableSortedSet.copyOf(icmpTypes);
      return this;
    }

    public Builder setIcmpTypes(SubRange... icmpTypes) {
      _icmpTypes = ImmutableSortedSet.copyOf(icmpTypes);
      return this;
    }

    public Builder setIcmpTypes(int... icmpTypes) {
      _icmpTypes =
          Arrays.stream(icmpTypes)
              .mapToObj(SubRange::singleton)
              .collect(ImmutableSortedSet.toImmutableSortedSet(Ordering.natural()));
      return this;
    }

    public Builder setIpProtocols(IpProtocol... ipProtocols) {
      return setIpProtocols(Arrays.asList(ipProtocols));
    }

    public Builder setIpProtocols(Iterable<IpProtocol> ipProtocols) {
      _ipProtocols = ImmutableSortedSet.copyOf(ipProtocols);
      return this;
    }

    public Builder setNegate(boolean negate) {
      _negate = negate;
      return this;
    }

    public Builder setNotDscps(Iterable<Integer> notDscps) {
      _notDscps = ImmutableSortedSet.copyOf(notDscps);
      return this;
    }

    public Builder setNotDstIps(Iterable<IpWildcard> notDstIps) {
      _notDstIps = IpWildcardSetIpSpace.builder().including(notDstIps).build();
      return this;
    }

    public Builder setNotDstIps(IpSpace notDstIps) {
      _notDstIps = notDstIps;
      return this;
    }

    public Builder setNotDstPorts(Iterable<SubRange> notDstPorts) {
      _notDstPorts = ImmutableSortedSet.copyOf(notDstPorts);
      return this;
    }

    public Builder setNotEcns(Iterable<Integer> notEcns) {
      _notEcns = ImmutableSortedSet.copyOf(notEcns);
      return this;
    }

    public Builder setNotFragmentOffsets(Iterable<SubRange> notFragmentOffsets) {
      _notFragmentOffsets = ImmutableSortedSet.copyOf(notFragmentOffsets);
      return this;
    }

    public Builder setNotIcmpCodes(Iterable<SubRange> notIcmpCodes) {
      _notIcmpCodes = ImmutableSortedSet.copyOf(notIcmpCodes);
      return this;
    }

    public Builder setNotIcmpTypes(Iterable<SubRange> notIcmpTypes) {
      _notIcmpTypes = ImmutableSortedSet.copyOf(notIcmpTypes);
      return this;
    }

    public Builder setNotIpProtocols(Iterable<IpProtocol> notIpProtocols) {
      _notIpProtocols = ImmutableSortedSet.copyOf(notIpProtocols);
      return this;
    }

    public Builder setNotPacketLengths(Iterable<SubRange> notPacketLengths) {
      _notPacketLengths = ImmutableSortedSet.copyOf(notPacketLengths);
      return this;
    }

    public Builder setNotSrcIps(Iterable<IpWildcard> notSrcIps) {
      _notSrcIps = IpWildcardSetIpSpace.builder().including(notSrcIps).build();
      return this;
    }

    public Builder setNotSrcIps(IpSpace notSrcIps) {
      _notSrcIps = notSrcIps;
      return this;
    }

    public Builder setNotSrcPorts(Iterable<SubRange> notSrcPorts) {
      _notSrcPorts = ImmutableSortedSet.copyOf(notSrcPorts);
      return this;
    }

    public Builder setNotSrcProtocols(Iterable<Protocol> notSrcProtocols) {
      _notSrcProtocols = ImmutableSortedSet.copyOf(notSrcProtocols);
      return this;
    }

    public Builder setPacketLengths(Iterable<SubRange> packetLengths) {
      _packetLengths = ImmutableSortedSet.copyOf(packetLengths);
      return this;
    }

    public Builder setSrcIps(Iterable<IpWildcard> srcIps) {
      _srcIps = IpWildcardSetIpSpace.builder().including(srcIps).build();
      return this;
    }

    public Builder setSrcIps(IpSpace srcIps) {
      _srcIps = srcIps;
      return this;
    }

    public Builder setSrcOrDstIps(Iterable<IpWildcard> srcOrDstIps) {
      _srcOrDstIps = IpWildcardSetIpSpace.builder().including(srcOrDstIps).build();
      return this;
    }

    public Builder setSrcOrDstIps(IpSpace srcOrDstIps) {
      _srcOrDstIps = srcOrDstIps;
      return this;
    }

    public Builder setSrcOrDstPorts(Iterable<SubRange> srcOrDstPorts) {
      _srcOrDstPorts = ImmutableSortedSet.copyOf(srcOrDstPorts);
      return this;
    }

    public Builder setSrcOrDstProtocols(Iterable<Protocol> srcOrDstProtocols) {
      _srcOrDstProtocols = ImmutableSortedSet.copyOf(srcOrDstProtocols);
      return this;
    }

    public Builder setSrcPorts(SubRange... srcPorts) {
      return setSrcPorts(Arrays.asList(srcPorts));
    }

    public Builder setSrcPorts(Iterable<SubRange> srcPorts) {
      _srcPorts = ImmutableSortedSet.copyOf(srcPorts);
      return this;
    }

    public Builder setSrcProtocols(Iterable<Protocol> srcProtocols) {
      _srcProtocols = ImmutableSortedSet.copyOf(srcProtocols);
      return this;
    }

    public Builder setTcpFlags(Iterable<TcpFlagsMatchConditions> tcpFlags) {
      _tcpFlags = ImmutableList.copyOf(tcpFlags);
      return this;
    }
  }

  private static final Comparator<HeaderSpace> COMPARATOR =
      Comparator.comparing(HeaderSpace::getDscps, Comparators.lexicographical(Ordering.natural()))
          .thenComparing(HeaderSpace::getDstIps, nullsFirst(naturalOrder()))
          .thenComparing(HeaderSpace::getDstPorts, Comparators.lexicographical(Ordering.natural()))
          .thenComparing(HeaderSpace::getEcns, Comparators.lexicographical(Ordering.natural()))
          .thenComparing(
              HeaderSpace::getFragmentOffsets, Comparators.lexicographical(Ordering.natural()))
          .thenComparing(HeaderSpace::getIcmpCodes, Comparators.lexicographical(Ordering.natural()))
          .thenComparing(HeaderSpace::getIcmpTypes, Comparators.lexicographical(Ordering.natural()))
          .thenComparing(
              HeaderSpace::getIpProtocols, Comparators.lexicographical(Ordering.natural()))
          .thenComparing(HeaderSpace::getNegate)
          .thenComparing(HeaderSpace::getNotDscps, Comparators.lexicographical(Ordering.natural()))
          .thenComparing(HeaderSpace::getNotDstIps, nullsFirst(naturalOrder()))
          .thenComparing(
              HeaderSpace::getNotDstPorts, Comparators.lexicographical(Ordering.natural()))
          .thenComparing(HeaderSpace::getNotEcns, Comparators.lexicographical(Ordering.natural()))
          .thenComparing(
              HeaderSpace::getNotFragmentOffsets, Comparators.lexicographical(Ordering.natural()))
          .thenComparing(
              HeaderSpace::getNotIcmpCodes, Comparators.lexicographical(Ordering.natural()))
          .thenComparing(
              HeaderSpace::getNotIcmpTypes, Comparators.lexicographical(Ordering.natural()))
          .thenComparing(
              HeaderSpace::getNotIpProtocols, Comparators.lexicographical(Ordering.natural()))
          .thenComparing(
              HeaderSpace::getNotPacketLengths, Comparators.lexicographical(Ordering.natural()))
          .thenComparing(HeaderSpace::getNotSrcIps, nullsFirst(naturalOrder()))
          .thenComparing(
              HeaderSpace::getNotSrcPorts, Comparators.lexicographical(Ordering.natural()))
          .thenComparing(
              HeaderSpace::getPacketLengths, Comparators.lexicographical(Ordering.natural()))
          .thenComparing(HeaderSpace::getSrcIps, nullsFirst(naturalOrder()))
          .thenComparing(HeaderSpace::getSrcOrDstIps, nullsFirst(naturalOrder()))
          .thenComparing(
              HeaderSpace::getSrcOrDstPorts, Comparators.lexicographical(Ordering.natural()))
          .thenComparing(HeaderSpace::getSrcPorts, Comparators.lexicographical(Ordering.natural()))
          .thenComparing(HeaderSpace::getTcpFlags, Comparators.lexicographical(Ordering.natural()));
  private static final String PROP_DSCPS = "dscps";
  private static final String PROP_DST_IPS = "dstIps";
  private static final String PROP_DST_PORTS = "dstPorts";
  private static final String PROP_ECNS = "ecns";
  private static final String PROP_FRAGMENT_OFFSETS = "fragmentOffsets";
  private static final String PROP_ICMP_CODES = "icmpCodes";
  private static final String PROP_ICMP_TYPES = "icmpTypes";
  private static final String PROP_IP_PROTOCOLS = "ipProtocols";
  private static final String PROP_NEGATE = "negate";
  private static final String PROP_NOT_DSCPS = "notDscps";
  private static final String PROP_NOT_DST_IPS = "notDstIps";
  private static final String PROP_NOT_DST_PORTS = "notDstPorts";
  private static final String PROP_NOT_ECNS = "notEcns";
  private static final String PROP_NOT_FRAGMENT_OFFSETS = "notFragmentOffsets";
  private static final String PROP_NOT_ICMP_CODES = "notIcmpCodes";
  private static final String PROP_NOT_ICMP_TYPES = "notIcmpTypes";
  private static final String PROP_NOT_IP_PROTOCOLS = "notIpProtocols";
  private static final String PROP_NOT_PACKET_LENGTHS = "notPacketLengths";
  private static final String PROP_NOT_SRC_IPS = "notSrcIps";
  private static final String PROP_NOT_SRC_PORTS = "notSrcPorts";
  private static final String PROP_PACKET_LENGTHS = "packetLengths";
  private static final String PROP_SRC_IPS = "srcIps";
  private static final String PROP_SRC_OR_DST_IPS = "srcOrDstIps";
  private static final String PROP_SRC_OR_DST_PORTS = "srcOrDstPorts";
  private static final String PROP_SRC_PORTS = "srcPorts";
  private static final String PROP_TCP_FLAGS_MATCH_CONDITIONS = "tcpFlagsMatchConditions";
  private static final String PROP_DEPRECATED_STATES = "states";

  public static Builder builder() {
    return new Builder();
  }

  private SortedSet<Integer> _dscps;
  private IpSpace _dstIps;
  private SortedSet<SubRange> _dstPorts;
  private SortedSet<Integer> _ecns;
  private SortedSet<SubRange> _fragmentOffsets;
  private SortedSet<SubRange> _icmpCodes;
  private SortedSet<SubRange> _icmpTypes;
  private SortedSet<IpProtocol> _ipProtocols;
  private boolean _negate;
  private SortedSet<Integer> _notDscps;
  private IpSpace _notDstIps;
  private SortedSet<SubRange> _notDstPorts;
  private SortedSet<Integer> _notEcns;
  private SortedSet<SubRange> _notFragmentOffsets;
  private SortedSet<SubRange> _notIcmpCodes;
  private SortedSet<SubRange> _notIcmpTypes;
  private SortedSet<IpProtocol> _notIpProtocols;
  private SortedSet<SubRange> _notPacketLengths;
  private IpSpace _notSrcIps;
  private SortedSet<SubRange> _notSrcPorts;
  private SortedSet<SubRange> _packetLengths;
  private IpSpace _srcIps;
  private IpSpace _srcOrDstIps;
  private SortedSet<SubRange> _srcOrDstPorts;
  private SortedSet<SubRange> _srcPorts;
  private List<TcpFlagsMatchConditions> _tcpFlags;

  public HeaderSpace() {
    _dscps = Collections.emptySortedSet();
    _dstPorts = Collections.emptySortedSet();
    _ecns = Collections.emptySortedSet();
    _fragmentOffsets = Collections.emptySortedSet();
    _icmpCodes = Collections.emptySortedSet();
    _icmpTypes = Collections.emptySortedSet();
    _ipProtocols = Collections.emptySortedSet();
    _packetLengths = Collections.emptySortedSet();
    _srcOrDstPorts = Collections.emptySortedSet();
    _srcPorts = Collections.emptySortedSet();
    _tcpFlags = Collections.emptyList();
    _notDscps = Collections.emptySortedSet();
    _notDstPorts = Collections.emptySortedSet();
    _notEcns = Collections.emptySortedSet();
    _notFragmentOffsets = Collections.emptySortedSet();
    _notIcmpCodes = Collections.emptySortedSet();
    _notIcmpTypes = Collections.emptySortedSet();
    _notIpProtocols = Collections.emptySortedSet();
    _notPacketLengths = Collections.emptySortedSet();
    _notSrcPorts = Collections.emptySortedSet();
  }

  private HeaderSpace(Builder builder) {
    _dscps = ImmutableSortedSet.copyOf(builder._dscps);
    _dstIps = builder._dstIps;
    _dstPorts = ImmutableSortedSet.copyOf(builder._dstPorts);
    _ecns = ImmutableSortedSet.copyOf(builder._ecns);
    _fragmentOffsets = ImmutableSortedSet.copyOf(builder._fragmentOffsets);
    _ipProtocols = ImmutableSortedSet.copyOf(builder._ipProtocols);
    _negate = builder._negate;
    _notDscps = ImmutableSortedSet.copyOf(builder._notDscps);
    _notDstIps = builder._notDstIps;
    _notDstPorts = ImmutableSortedSet.copyOf(builder._notDstPorts);
    _notEcns = ImmutableSortedSet.copyOf(builder._notEcns);
    _notFragmentOffsets = ImmutableSortedSet.copyOf(builder._notFragmentOffsets);
    _notIcmpCodes = ImmutableSortedSet.copyOf(builder._notIcmpCodes);
    _notIcmpTypes = ImmutableSortedSet.copyOf(builder._notIcmpTypes);
    _notIpProtocols = ImmutableSortedSet.copyOf(builder._notIpProtocols);
    _notPacketLengths = ImmutableSortedSet.copyOf(builder._notPacketLengths);
    _notSrcIps = builder._notSrcIps;
    _notSrcPorts = ImmutableSortedSet.copyOf(builder._notSrcPorts);
    _packetLengths = ImmutableSortedSet.copyOf(builder._packetLengths);
    _srcIps = builder._srcIps;
    _srcOrDstIps = builder._srcOrDstIps;
    _srcOrDstPorts = ImmutableSortedSet.copyOf(builder._srcOrDstPorts);
    _srcPorts = ImmutableSortedSet.copyOf(builder._srcPorts);
    _icmpTypes = ImmutableSortedSet.copyOf(builder._icmpTypes);
    _icmpCodes = ImmutableSortedSet.copyOf(builder._icmpCodes);
    _tcpFlags = ImmutableList.copyOf(builder._tcpFlags);
  }

  @Override
  public int compareTo(HeaderSpace o) {
    return COMPARATOR.compare(this, o);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof HeaderSpace)) {
      return false;
    }
    HeaderSpace other = (HeaderSpace) o;
    return _dscps.equals(other._dscps)
        && Objects.equals(_dstIps, other._dstIps)
        && _dstPorts.equals(other._dstPorts)
        && _ecns.equals(other._ecns)
        && _fragmentOffsets.equals(other._fragmentOffsets)
        && _icmpCodes.equals(other._icmpCodes)
        && _icmpTypes.equals(other._icmpTypes)
        && _ipProtocols.equals(other._ipProtocols)
        && _negate == other._negate
        && _notDscps.equals(other._notDscps)
        && Objects.equals(_notDstIps, other._notDstIps)
        && _notDstPorts.equals(other._notDstPorts)
        && _notEcns.equals(other._notEcns)
        && _notFragmentOffsets.equals(other._notFragmentOffsets)
        && _notIcmpCodes.equals(other._notIcmpCodes)
        && _notIcmpTypes.equals(other._notIcmpTypes)
        && _notIpProtocols.equals(other._notIpProtocols)
        && _notPacketLengths.equals(other._notPacketLengths)
        && Objects.equals(_notSrcIps, other._notSrcIps)
        && _notSrcPorts.equals(other._notSrcPorts)
        && _packetLengths.equals(other._packetLengths)
        && Objects.equals(_srcIps, other._srcIps)
        && Objects.equals(_srcOrDstIps, other._srcOrDstIps)
        && _srcOrDstPorts.equals(other._srcOrDstPorts)
        && _srcPorts.equals(other._srcPorts)
        && _tcpFlags.equals(other._tcpFlags);
  }

  /** A set of acceptable DSCP values for a packet. */
  @JsonProperty(PROP_DSCPS)
  public SortedSet<Integer> getDscps() {
    return _dscps;
  }

  /**
   * A space of acceptable destination IP addresses for a packet.
   *
   * <p>The empty set of dstIps is interpreted as no constraint, or all IPs
   */
  @JsonProperty(PROP_DST_IPS)
  public IpSpace getDstIps() {
    return _dstIps;
  }

  /** A set of acceptable destination port ranges for a TCP/UDP packet. */
  @JsonProperty(PROP_DST_PORTS)
  public SortedSet<SubRange> getDstPorts() {
    return _dstPorts;
  }

  /** A set of acceptable ECN values for a packet. */
  @JsonProperty(PROP_ECNS)
  public SortedSet<Integer> getEcns() {
    return _ecns;
  }

  /** A set of acceptable fragment offsets for a UDP packet. */
  @JsonProperty(PROP_FRAGMENT_OFFSETS)
  public SortedSet<SubRange> getFragmentOffsets() {
    return _fragmentOffsets;
  }

  /** A set of acceptable ICMP code ranges for an ICMP packet. */
  @JsonProperty(PROP_ICMP_CODES)
  public SortedSet<SubRange> getIcmpCodes() {
    return _icmpCodes;
  }

  /** A set of acceptable ICMP type ranges for an ICMP packet. */
  @JsonProperty(PROP_ICMP_TYPES)
  public SortedSet<SubRange> getIcmpTypes() {
    return _icmpTypes;
  }

  /** A set of acceptable IP protocols for a packet. */
  @JsonProperty(PROP_IP_PROTOCOLS)
  public SortedSet<IpProtocol> getIpProtocols() {
    return _ipProtocols;
  }

  /** Determines whether to match the complement of the stated criteria of this header space. */
  @JsonProperty(PROP_NEGATE)
  public boolean getNegate() {
    return _negate;
  }

  /** A set of unacceptable DSCP values for a packet. */
  @JsonProperty(PROP_NOT_DSCPS)
  public SortedSet<Integer> getNotDscps() {
    return _notDscps;
  }

  /** A space of unacceptable destination IP addresses for a packet. */
  @JsonProperty(PROP_NOT_DST_IPS)
  public IpSpace getNotDstIps() {
    return _notDstIps;
  }

  /** A set of unacceptable destination port ranges for a TCP/UDP packet. */
  @JsonProperty(PROP_NOT_DST_PORTS)
  public SortedSet<SubRange> getNotDstPorts() {
    return _notDstPorts;
  }

  /** A set of unacceptable ECN values for a packet. */
  @JsonProperty(PROP_NOT_ECNS)
  public SortedSet<Integer> getNotEcns() {
    return _notEcns;
  }

  /** A set of unacceptable fragment offsets for a UDP packet. */
  @JsonProperty(PROP_NOT_FRAGMENT_OFFSETS)
  public SortedSet<SubRange> getNotFragmentOffsets() {
    return _notFragmentOffsets;
  }

  /** A set of unacceptable ICMP code ranges for an ICMP packet. */
  @JsonProperty(PROP_NOT_ICMP_CODES)
  public SortedSet<SubRange> getNotIcmpCodes() {
    return _notIcmpCodes;
  }

  /** A set of unacceptable ICMP type ranges for an ICMP packet. */
  @JsonProperty(PROP_NOT_ICMP_TYPES)
  public SortedSet<SubRange> getNotIcmpTypes() {
    return _notIcmpTypes;
  }

  /** A set of unacceptable IP protocols for a packet. */
  @JsonProperty(PROP_NOT_IP_PROTOCOLS)
  public SortedSet<IpProtocol> getNotIpProtocols() {
    return _notIpProtocols;
  }

  @JsonProperty(PROP_NOT_PACKET_LENGTHS)
  public SortedSet<SubRange> getNotPacketLengths() {
    return _notPacketLengths;
  }

  /** A space of unacceptable source IP addresses for a packet. */
  @JsonProperty(PROP_NOT_SRC_IPS)
  public IpSpace getNotSrcIps() {
    return _notSrcIps;
  }

  /** A set of unacceptable source port ranges for a TCP/UDP packet. */
  @JsonProperty(PROP_NOT_SRC_PORTS)
  public SortedSet<SubRange> getNotSrcPorts() {
    return _notSrcPorts;
  }

  @JsonProperty(PROP_PACKET_LENGTHS)
  public SortedSet<SubRange> getPacketLengths() {
    return _packetLengths;
  }

  /** A space of acceptable source IP addresses for a packet. */
  @JsonProperty(PROP_SRC_IPS)
  public IpSpace getSrcIps() {
    return _srcIps;
  }

  /**
   * A space of IP addresses within which either the source or the destination IP of a packet must
   * fall for acceptance.
   */
  @JsonProperty(PROP_SRC_OR_DST_IPS)
  public IpSpace getSrcOrDstIps() {
    return _srcOrDstIps;
  }

  /**
   * A set of ranges within which either the source or the destination port of a TCP/UDP packet must
   * fall for acceptance.
   */
  @JsonProperty(PROP_SRC_OR_DST_PORTS)
  public SortedSet<SubRange> getSrcOrDstPorts() {
    return _srcOrDstPorts;
  }

  /** A set of acceptable source port ranges for a TCP/UDP packet. */
  @JsonProperty(PROP_SRC_PORTS)
  public SortedSet<SubRange> getSrcPorts() {
    return _srcPorts;
  }

  /** A set of acceptable TCP flag bitmasks for a TCP packet to match. */
  @JsonProperty(PROP_TCP_FLAGS_MATCH_CONDITIONS)
  public List<TcpFlagsMatchConditions> getTcpFlags() {
    return _tcpFlags;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _dscps,
        _dstIps,
        _dstPorts,
        _ecns,
        _fragmentOffsets,
        _icmpCodes,
        _icmpTypes,
        _ipProtocols,
        _negate,
        _notDscps,
        _notDstIps,
        _notDstPorts,
        _notEcns,
        _notFragmentOffsets,
        _notIcmpCodes,
        _notIcmpTypes,
        _notIpProtocols,
        _notPacketLengths,
        _notSrcIps,
        _notSrcPorts,
        _packetLengths,
        _srcIps,
        _srcOrDstIps,
        _srcOrDstPorts,
        _srcPorts,
        _tcpFlags);
  }

  public boolean matches(Flow flow, Map<String, IpSpace> namedIpSpaces) {
    if (!_dscps.isEmpty() && !_dscps.contains(flow.getDscp())) {
      return false;
    }
    if (_notDscps.contains(flow.getDscp())) {
      return false;
    }
    if (_dstIps != null && !_dstIps.containsIp(flow.getDstIp(), namedIpSpaces)) {
      return false;
    }
    if (_notDstIps != null && _notDstIps.containsIp(flow.getDstIp(), namedIpSpaces)) {
      return false;
    }
    if (!_dstPorts.isEmpty()
        && _dstPorts.stream().noneMatch(sr -> sr.includes(flow.getDstPort()))) {
      return false;
    }
    if (!_notDstPorts.isEmpty()
        && _notDstPorts.stream().anyMatch(sr -> sr.includes(flow.getDstPort()))) {
      return false;
    }
    if (!_ecns.isEmpty() && !_ecns.contains(flow.getEcn())) {
      return false;
    }
    if (_notEcns.contains(flow.getEcn())) {
      return false;
    }
    if (!_fragmentOffsets.isEmpty()
        && _fragmentOffsets.stream().noneMatch(sr -> sr.includes(flow.getFragmentOffset()))) {
      return false;
    }
    if (!_notFragmentOffsets.isEmpty()
        && _notFragmentOffsets.stream().anyMatch(sr -> sr.includes(flow.getFragmentOffset()))) {
      return false;
    }
    if (!_icmpCodes.isEmpty()
        && flow.getIcmpCode() != null
        && _icmpCodes.stream().noneMatch(sr -> sr.includes(flow.getIcmpCode()))) {
      return false;
    }
    if (!_notIcmpCodes.isEmpty()
        && _notIcmpCodes.stream().anyMatch(sr -> sr.includes(flow.getFragmentOffset()))) {
      return false;
    }
    if (!_icmpTypes.isEmpty()
        && flow.getIcmpType() != null
        && _icmpTypes.stream().noneMatch(sr -> sr.includes(flow.getIcmpType()))) {
      return false;
    }
    if (!_notIcmpTypes.isEmpty()
        && _notIcmpTypes.stream().anyMatch(sr -> sr.includes(flow.getFragmentOffset()))) {
      return false;
    }
    if (!_ipProtocols.isEmpty() && !_ipProtocols.contains(flow.getIpProtocol())) {
      return false;
    }
    if (_notIpProtocols.contains(flow.getIpProtocol())) {
      return false;
    }
    if (!_packetLengths.isEmpty()
        && _packetLengths.stream().noneMatch(sr -> sr.includes(flow.getPacketLength()))) {
      return false;
    }
    if (!_notPacketLengths.isEmpty()
        && _notPacketLengths.stream().anyMatch(sr -> sr.includes(flow.getPacketLength()))) {
      return false;
    }
    if (_srcOrDstIps != null
        && !(_srcOrDstIps.containsIp(flow.getSrcIp(), namedIpSpaces)
            || _srcOrDstIps.containsIp(flow.getDstIp(), namedIpSpaces))) {
      return false;
    }
    if (!_srcOrDstPorts.isEmpty()
        && _srcOrDstPorts.stream()
            .noneMatch(sr -> sr.includes(flow.getSrcPort()) || sr.includes(flow.getDstPort()))) {
      return false;
    }
    if (_srcIps != null && !_srcIps.containsIp(flow.getSrcIp(), namedIpSpaces)) {
      return false;
    }
    if (_notSrcIps != null && _notSrcIps.containsIp(flow.getSrcIp(), namedIpSpaces)) {
      return false;
    }
    if (!_srcPorts.isEmpty()
        && _srcPorts.stream().noneMatch(sr -> sr.includes(flow.getSrcPort()))) {
      return false;
    }
    if (!_notSrcPorts.isEmpty()
        && _notSrcPorts.stream().anyMatch(sr -> sr.includes(flow.getSrcPort()))) {
      return false;
    }
    if (!_tcpFlags.isEmpty() && _tcpFlags.stream().noneMatch(tcpFlags -> tcpFlags.match(flow))) {
      return false;
    }
    return true;
  }

  @JsonProperty(PROP_DSCPS)
  public void setDscps(Iterable<Integer> dscps) {
    _dscps = ImmutableSortedSet.copyOf(dscps);
  }

  @JsonProperty(PROP_DST_IPS)
  public void setDstIps(IpSpace dstIps) {
    _dstIps = dstIps;
  }

  public void setDstIps(Iterable<IpWildcard> dstIps) {
    _dstIps = IpWildcardSetIpSpace.builder().including(dstIps).build();
  }

  @JsonProperty(PROP_DST_PORTS)
  public void setDstPorts(Iterable<SubRange> dstPorts) {
    _dstPorts = ImmutableSortedSet.copyOf(dstPorts);
  }

  @JsonProperty(PROP_ECNS)
  public void setEcns(Iterable<Integer> ecns) {
    _ecns = ImmutableSortedSet.copyOf(ecns);
  }

  @JsonProperty(PROP_FRAGMENT_OFFSETS)
  public void setFragmentOffsets(Iterable<SubRange> fragmentOffsets) {
    _fragmentOffsets = ImmutableSortedSet.copyOf(fragmentOffsets);
  }

  @JsonProperty(PROP_ICMP_CODES)
  public void setIcmpCodes(Iterable<SubRange> icmpCodes) {
    _icmpCodes = ImmutableSortedSet.copyOf(icmpCodes);
  }

  @JsonProperty(PROP_ICMP_TYPES)
  public void setIcmpTypes(Iterable<SubRange> icmpTypes) {
    _icmpTypes = ImmutableSortedSet.copyOf(icmpTypes);
  }

  @JsonProperty(PROP_IP_PROTOCOLS)
  public void setIpProtocols(Iterable<IpProtocol> ipProtocols) {
    _ipProtocols = ImmutableSortedSet.copyOf(ipProtocols);
  }

  @JsonProperty(PROP_NEGATE)
  public void setNegate(boolean negate) {
    _negate = negate;
  }

  @JsonProperty(PROP_NOT_DSCPS)
  public void setNotDscps(Iterable<Integer> notDscps) {
    _notDscps = ImmutableSortedSet.copyOf(notDscps);
  }

  @JsonProperty(PROP_NOT_DST_IPS)
  public void setNotDstIps(IpSpace notDstIps) {
    _notDstIps = notDstIps;
  }

  public void setNotDstIps(Iterable<IpWildcard> notDstIps) {
    _notDstIps = IpWildcardSetIpSpace.builder().including(notDstIps).build();
  }

  @JsonProperty(PROP_NOT_DST_PORTS)
  public void setNotDstPorts(Iterable<SubRange> notDstPorts) {
    _notDstPorts = ImmutableSortedSet.copyOf(notDstPorts);
  }

  @JsonProperty(PROP_NOT_ECNS)
  public void setNotEcns(Iterable<Integer> notEcns) {
    _notEcns = ImmutableSortedSet.copyOf(notEcns);
  }

  @JsonProperty(PROP_NOT_FRAGMENT_OFFSETS)
  public void setNotFragmentOffsets(Iterable<SubRange> notFragmentOffsets) {
    _notFragmentOffsets = ImmutableSortedSet.copyOf(notFragmentOffsets);
  }

  @JsonProperty(PROP_NOT_ICMP_CODES)
  public void setNotIcmpCodes(Iterable<SubRange> notIcmpCodes) {
    _notIcmpCodes = ImmutableSortedSet.copyOf(notIcmpCodes);
  }

  @JsonProperty(PROP_NOT_ICMP_TYPES)
  public void setNotIcmpTypes(Iterable<SubRange> notIcmpTypes) {
    _notIcmpTypes = ImmutableSortedSet.copyOf(notIcmpTypes);
  }

  @JsonProperty(PROP_NOT_IP_PROTOCOLS)
  public void setNotIpProtocols(Iterable<IpProtocol> notIpProtocols) {
    _notIpProtocols = ImmutableSortedSet.copyOf(notIpProtocols);
  }

  @JsonProperty(PROP_NOT_PACKET_LENGTHS)
  public void setNotPacketLengths(Iterable<SubRange> notPacketLengths) {
    _notPacketLengths = ImmutableSortedSet.copyOf(notPacketLengths);
  }

  @JsonProperty(PROP_NOT_SRC_IPS)
  public void setNotSrcIps(IpSpace notSrcIps) {
    _notSrcIps = notSrcIps;
  }

  public void setNotSrcIps(Iterable<IpWildcard> notSrcIps) {
    _notSrcIps = IpWildcardSetIpSpace.builder().including(notSrcIps).build();
  }

  @JsonProperty(PROP_NOT_SRC_PORTS)
  public void setNotSrcPorts(Iterable<SubRange> notSrcPorts) {
    _notSrcPorts = ImmutableSortedSet.copyOf(notSrcPorts);
  }

  @JsonProperty(PROP_PACKET_LENGTHS)
  public void setPacketLengths(Iterable<SubRange> packetLengths) {
    _packetLengths = ImmutableSortedSet.copyOf(packetLengths);
  }

  @JsonProperty(PROP_SRC_IPS)
  public void setSrcIps(IpSpace srcIps) {
    _srcIps = srcIps;
  }

  public void setSrcIps(Iterable<IpWildcard> srcIps) {
    _srcIps = IpWildcardSetIpSpace.builder().including(srcIps).build();
  }

  @JsonProperty(PROP_SRC_OR_DST_IPS)
  public void setSrcOrDstIps(IpSpace srcOrDstIps) {
    _srcOrDstIps = srcOrDstIps;
  }

  public void setSrcOrDstIps(Iterable<IpWildcard> srcOrDstIps) {
    _srcOrDstIps = IpWildcardSetIpSpace.builder().including(srcOrDstIps).build();
  }

  @JsonProperty(PROP_SRC_OR_DST_PORTS)
  public void setSrcOrDstPorts(Iterable<SubRange> srcOrDstPorts) {
    _srcOrDstPorts = ImmutableSortedSet.copyOf(srcOrDstPorts);
  }

  @JsonProperty(PROP_SRC_PORTS)
  public void setSrcPorts(Iterable<SubRange> srcPorts) {
    _srcPorts = ImmutableSortedSet.copyOf(srcPorts);
  }

  @Deprecated
  @JsonProperty(PROP_DEPRECATED_STATES)
  private void setStates(Object ignored) {}

  @JsonProperty(PROP_TCP_FLAGS_MATCH_CONDITIONS)
  public void setTcpFlags(Iterable<TcpFlagsMatchConditions> tcpFlags) {
    _tcpFlags = ImmutableList.copyOf(tcpFlags);
  }

  public Builder toBuilder() {
    return builder()
        .setDscps(_dscps)
        .setDstIps(_dstIps)
        .setDstPorts(_dstPorts)
        .setEcns(_ecns)
        .setFragmentOffsets(_fragmentOffsets)
        .setIcmpCodes(_icmpCodes)
        .setIcmpTypes(_icmpTypes)
        .setIpProtocols(_ipProtocols)
        .setNegate(_negate)
        .setNotDscps(_notDscps)
        .setNotDstIps(_notDstIps)
        .setNotDstPorts(_notDstPorts)
        .setNotEcns(_notEcns)
        .setNotFragmentOffsets(_notFragmentOffsets)
        .setNotIcmpCodes(_notIcmpCodes)
        .setNotIcmpTypes(_notIcmpTypes)
        .setNotIpProtocols(_notIpProtocols)
        .setNotPacketLengths(_notPacketLengths)
        .setNotSrcIps(_notSrcIps)
        .setNotSrcPorts(_notSrcPorts)
        .setPacketLengths(_packetLengths)
        .setSrcIps(_srcIps)
        .setSrcOrDstIps(_srcOrDstIps)
        .setSrcOrDstPorts(_srcOrDstPorts)
        .setSrcPorts(_srcPorts)
        .setTcpFlags(_tcpFlags);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass())
        .omitNullValues()
        .add(PROP_DSCPS, nullIfEmpty(_dscps))
        .add(PROP_DST_IPS, _dstIps)
        .add(PROP_DST_PORTS, nullIfEmpty(_dstPorts))
        .add(PROP_ECNS, nullIfEmpty(_ecns))
        .add(PROP_FRAGMENT_OFFSETS, nullIfEmpty(_fragmentOffsets))
        .add(PROP_ICMP_CODES, nullIfEmpty(_icmpCodes))
        .add(PROP_ICMP_TYPES, nullIfEmpty(_icmpTypes))
        .add(PROP_IP_PROTOCOLS, nullIfEmpty(_ipProtocols))
        .add(PROP_NEGATE, _negate ? _negate : null)
        .add(PROP_NOT_DSCPS, nullIfEmpty(_notDscps))
        .add(PROP_NOT_DST_IPS, _notDstIps)
        .add(PROP_NOT_DST_PORTS, nullIfEmpty(_notDstPorts))
        .add(PROP_NOT_ECNS, nullIfEmpty(_notEcns))
        .add(PROP_NOT_FRAGMENT_OFFSETS, nullIfEmpty(_notFragmentOffsets))
        .add(PROP_NOT_ICMP_CODES, nullIfEmpty(_notIcmpCodes))
        .add(PROP_NOT_ICMP_TYPES, nullIfEmpty(_notIcmpTypes))
        .add(PROP_NOT_IP_PROTOCOLS, nullIfEmpty(_notIpProtocols))
        .add(PROP_NOT_PACKET_LENGTHS, nullIfEmpty(_notPacketLengths))
        .add(PROP_NOT_SRC_IPS, _notSrcIps)
        .add(PROP_NOT_SRC_PORTS, nullIfEmpty(_notSrcPorts))
        .add(PROP_PACKET_LENGTHS, nullIfEmpty(_packetLengths))
        .add(PROP_SRC_IPS, _srcIps)
        .add(PROP_SRC_OR_DST_IPS, _srcOrDstIps)
        .add(PROP_SRC_OR_DST_PORTS, nullIfEmpty(_srcOrDstPorts))
        .add(PROP_SRC_PORTS, nullIfEmpty(_srcPorts))
        .add(PROP_TCP_FLAGS_MATCH_CONDITIONS, nullIfEmpty(_tcpFlags))
        .toString();
  }

  public final boolean unrestricted() {
    boolean ret =
        _dscps.isEmpty()
            && _notDscps.isEmpty()
            && _dstIps instanceof UniverseIpSpace
            && _notDstIps instanceof EmptyIpSpace
            && _dstPorts.isEmpty()
            && _notDstPorts.isEmpty()
            && _ecns.isEmpty()
            && _notEcns.isEmpty()
            && _fragmentOffsets.isEmpty()
            && _notFragmentOffsets.isEmpty()
            && _icmpCodes.isEmpty()
            && _notIcmpCodes.isEmpty()
            && _icmpTypes.isEmpty()
            && _notIcmpTypes.isEmpty()
            && _ipProtocols.isEmpty()
            && _notIpProtocols.isEmpty()
            && _packetLengths.isEmpty()
            && _notPacketLengths.isEmpty()
            && _srcIps instanceof UniverseIpSpace
            && _notSrcIps instanceof EmptyIpSpace
            && _srcOrDstIps instanceof UniverseIpSpace
            && _srcOrDstPorts.isEmpty()
            && _srcPorts.isEmpty()
            && _notSrcPorts.isEmpty()
            && _tcpFlags.isEmpty();
    return ret;
  }
}
