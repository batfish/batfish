package org.batfish.datamodel.visitors;

import java.util.function.Function;
import javax.annotation.Nonnull;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.IpIpSpace;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.IpWildcardIpSpace;
import org.batfish.datamodel.IpWildcardSetIpSpace;
import org.batfish.datamodel.PrefixIpSpace;
import org.batfish.datamodel.UniverseIpSpace;

/**
 * Renames all {@link IpSpaceReference} expressions in an input {@link IpSpace} using an input
 * renaming function.
 */
public class IpSpaceRenamer implements Function<IpSpace, IpSpace> {
  private final class Visitor implements GenericIpSpaceVisitor<IpSpace> {

    @Override
    public IpSpace visitAclIpSpace(AclIpSpace aclIpSpace) {
      AclIpSpace.Builder renamedSpace = AclIpSpace.builder();
      aclIpSpace
          .getLines()
          .forEach(
              line -> {
                IpSpace space = line.getIpSpace().accept(this);
                renamedSpace.thenAction(line.getAction(), space);
              });
      return renamedSpace.build();
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
