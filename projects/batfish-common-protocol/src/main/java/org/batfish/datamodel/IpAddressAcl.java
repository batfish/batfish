package org.batfish.datamodel;

import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * An ACL-based {@link IpSpace}. An IP is permitted if it is in the space the ACL represents, or
 * denied if it is not.
 */
public class IpAddressAcl implements IpSpace {

  public static class Builder {

    private List<IpAddressAclLine> _lines;

    private Builder() {
      _lines = ImmutableList.of();
    }

    public IpAddressAcl build() {
      return new IpAddressAcl(_lines);
    }

    public Builder setLines(List<IpAddressAclLine> lines) {
      _lines = lines;
      return this;
    }
  }

  public static final IpAddressAcl PERMIT_ALL =
      IpAddressAcl.builder().setLines(ImmutableList.of(IpAddressAclLine.PERMIT_ALL)).build();

  public static final IpAddressAcl DENY_ALL = IpAddressAcl.builder().build();

  public static Builder builder() {
    return new Builder();
  }

  private final List<IpAddressAclLine> _lines;

  private IpAddressAcl(List<IpAddressAclLine> lines) {
    _lines = lines;
  }

  private LineAction action(Ip ip) {
    return _lines
        .stream()
        .filter(line -> line.getIpSpace().contains(ip) ^ line.getMatchComplement())
        .map(IpAddressAclLine::getAction)
        .findFirst()
        .orElse(LineAction.REJECT);
  }

  @Override
  public boolean contains(@Nonnull Ip ip) {
    return action(ip) == LineAction.ACCEPT;
  }
}
