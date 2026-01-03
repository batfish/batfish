package org.batfish.datamodel.applications;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.acl.AclLineMatchExpr;

/**
 * An abstract class that represents an application, which is an IP protocol and
 * application-speficic details covered in child classes
 */
@ParametersAreNonnullByDefault
public abstract class Application {

  private final @Nonnull IpProtocol _ipProtocol;

  protected Application(IpProtocol ipProtocol) {
    _ipProtocol = ipProtocol;
  }

  public @Nonnull IpProtocol getIpProtocol() {
    return _ipProtocol;
  }

  protected static String stringifySubRanges(List<SubRange> subranges) {
    return subranges.stream()
        .map(
            subrange ->
                subrange.isSingleValue()
                    ? Objects.toString(subrange.getStart())
                    : String.format("%d-%d", subrange.getStart(), subrange.getEnd()))
        .collect(Collectors.joining(","));
  }

  public abstract AclLineMatchExpr toAclLineMatchExpr();

  public abstract <T> T accept(ApplicationVisitor<T> visitor);
}
