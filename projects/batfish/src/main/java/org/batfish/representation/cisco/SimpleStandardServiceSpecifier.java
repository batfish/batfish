package org.batfish.representation.cisco;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;

public class SimpleStandardServiceSpecifier implements ExtendedAccessListServiceSpecifier {

  /** */
  private static final long serialVersionUID = 1L;

  public static class Builder {

    private Set<Integer> _dscps;

    private IpWildcard _dstIpWildcard;

    private Set<Integer> _ecns;

    private IpProtocol _protocol;

    private IpWildcard _srcIpWildcard;

    public SimpleStandardServiceSpecifier build() {
      return new SimpleStandardServiceSpecifier(this);
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

  private SimpleStandardServiceSpecifier(Builder builder) {
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
  public AclLineMatchExpr toAclLineMatchExpr() {
    return new MatchHeaderSpace(
        HeaderSpace.builder()
            .setDscps(_dscps)
            .setDstIps(_dstIpWildcard.toIpSpace())
            .setEcns(_ecns)
            .setIpProtocols(ImmutableSet.of(_protocol))
            .setSrcIps(_srcIpWildcard.toIpSpace())
            .build());
  }
}
