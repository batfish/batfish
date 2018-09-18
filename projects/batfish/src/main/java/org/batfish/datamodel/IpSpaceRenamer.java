package org.batfish.datamodel;

import com.google.common.collect.ImmutableList;
import java.util.function.Function;
import javax.annotation.Nonnull;
import org.batfish.datamodel.visitors.GenericIpSpaceVisitor;

/**
 * Renames all {@link IpSpaceReference} expressions in an input {@link IpSpace} using an input
 * renaming function.
 */
public class IpSpaceRenamer implements Function<IpSpace, IpSpace> {
  private final class Visitor implements GenericIpSpaceVisitor<IpSpace> {

    @Override
    public IpSpace castToGenericIpSpaceVisitorReturnType(Object o) {
      return (IpSpace) o;
    }

    @Override
    public IpSpace visitAclIpSpace(AclIpSpace aclIpSpace) {
      return AclIpSpace.builder()
          .setLines(
              aclIpSpace
                  .getLines()
                  .stream()
                  .map(ln -> ln.toBuilder().setIpSpace(ln.getIpSpace().accept(this)).build())
                  .collect(ImmutableList.toImmutableList()))
          .build();
    }

    @Override
    public IpSpace visitEmptyIpSpace(EmptyIpSpace emptyIpSpace) {
      return emptyIpSpace;
    }

    @Override
    public IpSpace visitIpIpSpace(IpIpSpace ipIpSpace) {
      return ipIpSpace;
    }

    @Override
    public IpSpace visitIpSpaceReference(IpSpaceReference ipSpaceReference) {
      return new IpSpaceReference(
          _renamer.apply(ipSpaceReference.getName()), ipSpaceReference.getDescription());
    }

    @Override
    public IpSpace visitIpWildcardIpSpace(IpWildcardIpSpace ipWildcardIpSpace) {
      return ipWildcardIpSpace;
    }

    @Override
    public IpSpace visitIpWildcardSetIpSpace(IpWildcardSetIpSpace ipWildcardSetIpSpace) {
      return ipWildcardSetIpSpace;
    }

    @Override
    public IpSpace visitPrefixIpSpace(PrefixIpSpace prefixIpSpace) {
      return prefixIpSpace;
    }

    @Override
    public IpSpace visitUniverseIpSpace(UniverseIpSpace universeIpSpace) {
      return universeIpSpace;
    }
  }

  private final Visitor _visitor;

  private final Function<String, String> _renamer;

  public IpSpaceRenamer(Function<String, String> renamer) {
    _renamer = renamer;
    _visitor = new Visitor();
  }

  @Override
  public IpSpace apply(@Nonnull IpSpace ipSpace) {
    return _visitor.visit(ipSpace);
  }
}
