package org.batfish.datamodel;

import com.google.common.base.MoreObjects;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;
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

  private final Supplier<Integer> _hash;

  private final List<AclIpSpaceLine> _lines;

  private AclIpSpace(List<AclIpSpaceLine> lines) {
    _lines = lines;
    _hash = Suppliers.memoize(() -> Objects.hash(_lines));
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || !(o instanceof AclIpSpace)) {
      return false;
    }
    return Objects.equals(_lines, ((AclIpSpace) o)._lines);
  }

  public List<AclIpSpaceLine> getLines() {
    return _lines;
  }

  @Override
  public int hashCode() {
    return _hash.get();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass()).add("lines", _lines).toString();
  }
}
