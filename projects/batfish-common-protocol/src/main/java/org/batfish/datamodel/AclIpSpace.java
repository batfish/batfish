package org.batfish.datamodel;

import com.google.common.base.MoreObjects;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.batfish.datamodel.visitors.GenericIpSpaceVisitor;

/**
 * An ACL-based {@link IpSpace}. An IP is permitted if it is in the space the ACL represents, or
 * denied if it is not.
 */
public class AclIpSpace implements IpSpace {

  public static class Builder {

    private ImmutableList.Builder<AclIpSpaceLine> _lines;

    private Builder() {
      _lines = ImmutableList.builder();
    }

    public AclIpSpace build() {
      return new AclIpSpace(_lines.build());
    }

    public Builder setLines(List<AclIpSpaceLine> lines) {
      _lines = ImmutableList.<AclIpSpaceLine>builder().addAll(lines);
      return this;
    }

    public Builder thenPermitting(IpSpace... ipSpaces) {
      return thenPermitting(Arrays.stream(ipSpaces));
    }

    public Builder thenPermitting(Iterable<IpSpace> ipSpaces) {
      return thenPermitting(Streams.stream(ipSpaces));
    }

    public Builder thenPermitting(Stream<IpSpace> ipSpaces) {
      ipSpaces.map(AclIpSpaceLine::permit).forEach(_lines::add);
      return this;
    }

    public Builder thenRejecting(IpSpace... ipSpaces) {
      return thenRejecting(Arrays.stream(ipSpaces));
    }

    public Builder thenRejecting(Iterable<IpSpace> ipSpaces) {
      return thenRejecting(Streams.stream(ipSpaces));
    }

    private Builder thenRejecting(Stream<IpSpace> ipSpaces) {
      ipSpaces.map(AclIpSpaceLine::reject).forEach(_lines::add);
      return this;
    }
  }

  public static final AclIpSpace DENY_ALL = AclIpSpace.builder().build();

  public static final AclIpSpace PERMIT_ALL =
      AclIpSpace.builder().setLines(ImmutableList.of(AclIpSpaceLine.PERMIT_ALL)).build();

  public static Builder builder() {
    return new Builder();
  }

  public static Builder permitting(IpSpace... ipSpaces) {
    return new Builder().thenPermitting(ipSpaces);
  }

  public static Builder permitting(Iterable<IpSpace> ipSpaces) {
    return new Builder().thenPermitting(ipSpaces);
  }

  public static Builder permitting(Stream<IpSpace> ipSpaces) {
    return new Builder().thenPermitting(ipSpaces);
  }

  public static Builder rejecting(IpSpace... ipSpaces) {
    return new Builder().thenRejecting(ipSpaces);
  }

  public static Builder rejecting(Iterable<IpSpace> ipSpaces) {
    return new Builder().thenRejecting(ipSpaces);
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
        .filter(line -> line.getIpSpace().containsIp(ip))
        .map(AclIpSpaceLine::getAction)
        .findFirst()
        .orElse(LineAction.REJECT);
  }

  @Override
  public boolean containsIp(@Nonnull Ip ip) {
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
