package org.batfish.representation.cisco;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpWildcard;

public class SimpleServiceSpecifier implements ExtendedAccessListServiceSpecifier {

  public static class Builder {

    private Set<Integer> _dscps;

    private IpWildcard _dstIpWildcard;

    private Set<Integer> _ecns;

    private IpProtocol _protocol;

    private IpWildcard _srcIpWildcard;

    public SimpleServiceSpecifier build() {
      return new SimpleServiceSpecifier(this);
    }

    public Builder setDscps(Iterable<Integer> dscps) {
      _dscps = ImmutableSet.copyOf(dscps);
      return this;
    }

    public Builder setDstIpWildcard(IpWildcard dstIpWildcard) {
      _dstIpWildcard = dstIpWildcard;
      return this;
    }

    public Builder setEcns(Iterable<Integer> ecns) {
      _ecns = ImmutableSet.copyOf(ecns);
      return this;
    }

    public Builder setProtocol(IpProtocol protocol) {
      _protocol = protocol;
      return this;
    }

    public Builder setSrcIpWildcard(IpWildcard srcIpWildcard) {
      _srcIpWildcard = srcIpWildcard;
      return this;
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  private final Set<Integer> _dscps;

  private final IpWildcard _dstIpWildcard;

  private final Set<Integer> _ecns;

  private final IpProtocol _protocol;

  private final IpWildcard _srcIpWildcard;

  private SimpleServiceSpecifier(Builder builder) {
    _dscps = builder._dscps;
    _dstIpWildcard = builder._dstIpWildcard;
    _ecns = builder._ecns;
    _protocol = builder._protocol;
    _srcIpWildcard = builder._srcIpWildcard;
  }

  public Set<Integer> getDscps() {
    return _dscps;
  }

  public IpWildcard getDstIpWildcard() {
    return _dstIpWildcard;
  }

  public Set<Integer> getEcns() {
    return _ecns;
  }

  public IpProtocol getProtocol() {
    return _protocol;
  }

  public IpWildcard getSrcIpWildcard() {
    return _srcIpWildcard;
  }

  @Override
  public HeaderSpace.Builder toHeaderSpace() {
    return HeaderSpace.builder()
        .setDscps(_dscps)
        .setDstIps(_dstIpWildcard.toIpSpace())
        .setEcns(_ecns)
        .setIpProtocols(ImmutableSet.of(_protocol))
        .setSrcIps(_srcIpWildcard.toIpSpace());
  }
}
