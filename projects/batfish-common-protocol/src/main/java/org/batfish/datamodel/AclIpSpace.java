package org.batfish.datamodel;

import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.datamodel.visitors.GenericIpSpaceVisitor;

/**
 * An ACL-based {@link IpSpace}. An IP is permitted if it is in the space the ACL represents, or
 * denied if it is not.
 */
public class AclIpSpace implements IpSpace {

  public static class Builder {

    private List<AclIpSpaceLine> _lines;

    private Builder() {
      _lines = ImmutableList.of();
    }

    public AclIpSpace build() {
      return new AclIpSpace(_lines);
    }

    public Builder setLines(List<AclIpSpaceLine> lines) {
      _lines = lines;
      return this;
    }
  }

  public static final AclIpSpace DENY_ALL = AclIpSpace.builder().build();

  public static final AclIpSpace PERMIT_ALL =
      AclIpSpace.builder().setLines(ImmutableList.of(AclIpSpaceLine.PERMIT_ALL)).build();

  public static Builder builder() {
    return new Builder();
  }

  private final List<AclIpSpaceLine> _lines;

  private AclIpSpace(List<AclIpSpaceLine> lines) {
    _lines = lines;
  }

  @Override
  public <R> R accept(GenericIpSpaceVisitor<R> ipSpaceVisitor) {
    return ipSpaceVisitor.visitAclIpSpace(this);
  }

  private LineAction action(Ip ip) {
    return _lines
        .stream()
        .filter(line -> line.getIpSpace().contains(ip) ^ line.getMatchComplement())
        .map(AclIpSpaceLine::getAction)
        .findFirst()
        .orElse(LineAction.REJECT);
  }

  @Override
  public boolean contains(@Nonnull Ip ip) {
    return action(ip) == LineAction.ACCEPT;
  }

  public List<AclIpSpaceLine> getLines() {
    return _lines;
  }
}
