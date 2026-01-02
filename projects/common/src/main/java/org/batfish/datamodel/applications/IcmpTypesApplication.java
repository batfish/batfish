package org.batfish.datamodel.applications;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;

/**
 * An {@link IcmpApplication} that has only types specified (i.e., all codes within the specified
 * types)
 */
@ParametersAreNonnullByDefault
public final class IcmpTypesApplication extends IcmpApplication {

  public static final IcmpTypesApplication ALL =
      new IcmpTypesApplication(ImmutableList.of(new SubRange(0, MAX_TYPE)));

  private final @Nonnull List<SubRange> _types;

  public IcmpTypesApplication(int type) {
    this(ImmutableList.of(SubRange.singleton(type)));
  }

  public IcmpTypesApplication(List<SubRange> types) {
    _types = ImmutableList.copyOf(types);
  }

  public @Nonnull List<SubRange> getTypes() {
    return _types;
  }

  @Override
  public AclLineMatchExpr toAclLineMatchExpr() {
    return new MatchHeaderSpace(
        HeaderSpace.builder().setIpProtocols(IpProtocol.ICMP).setIcmpTypes(_types).build());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof IcmpTypesApplication)) {
      return false;
    }
    IcmpTypesApplication that = (IcmpTypesApplication) o;
    return _types.equals(that._types);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_types);
  }

  @Override
  public String toString() {
    if (ALL.equals(this)) {
      return "icmp";
    }
    return "icmp/" + stringifySubRanges(_types);
  }

  @Override
  public <T> T accept(ApplicationVisitor<T> visitor) {
    return visitor.visitIcmpTypesApplication(this);
  }
}
