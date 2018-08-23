package org.batfish.z3;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.AclIpSpaceLine;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpIpSpace;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.IpWildcardIpSpace;
import org.batfish.datamodel.IpWildcardSetIpSpace;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.PrefixIpSpace;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.visitors.GenericIpSpaceVisitor;

/**
 * Abstract class for specializing an {@link IpSpace} to some other {@link IpSpace}. The result of
 * specialization is never larger than the original space. The universe of specialized spaces is the
 * space we're specializing to, not the full IP space universe.
 *
 * <p>A subclass need only implement methods to specialize {@link Ip}s and {@link IpWildcard}s.
 */
public abstract class IpSpaceSpecializer implements GenericIpSpaceVisitor<IpSpace> {
  private final IpSpaceSimplifier _simplifier;
  protected final Map<String, IpSpace> _namedIpSpaces;
  private final Map<String, IpSpace> _specializedNamedIpSpaces;

  protected IpSpaceSpecializer(@Nonnull Map<String, IpSpace> namedIpSpaces) {
    _simplifier = new IpSpaceSimplifier(namedIpSpaces);
    _namedIpSpaces = ImmutableMap.copyOf(namedIpSpaces);
    _specializedNamedIpSpaces = new HashMap<>();
  }

  @Override
  public IpSpace castToGenericIpSpaceVisitorReturnType(Object o) {
    return (IpSpace) o;
  }

  protected abstract Optional<IpSpaceSpecializer> restrictSpecializerToBlacklist(
      Set<IpWildcard> blacklist);

  public IpSpace specialize(IpSpace ipSpace) {
    return _simplifier.simplify(ipSpace.accept(this));
  }

  protected abstract IpSpace specialize(Ip ip);

  protected abstract IpSpace specialize(IpWildcard ipWildcard);

  @Override
  public IpSpace visitAclIpSpace(AclIpSpace aclIpSpace) {
    /* Just specialize the IpSpace of each acl line. */
    List<AclIpSpaceLine> specializedLines =
        aclIpSpace
            .getLines()
            .stream()
            .map(line -> line.toBuilder().setIpSpace(line.getIpSpace().accept(this)).build())
            .filter(line -> line.getIpSpace() != EmptyIpSpace.INSTANCE)
            .collect(ImmutableList.toImmutableList());

    if (specializedLines.isEmpty()) {
      return EmptyIpSpace.INSTANCE;
    }

    if (specializedLines
        .stream()
        .allMatch(aclIpSpaceLine -> aclIpSpaceLine.getAction() == LineAction.DENY)) {
      return EmptyIpSpace.INSTANCE;
    }

    return AclIpSpace.builder().setLines(specializedLines).build();
  }

  @Override
  public IpSpace visitEmptyIpSpace(EmptyIpSpace emptyIpSpace) {
    return emptyIpSpace;
  }

  @Override
  public IpSpace visitIpIpSpace(IpIpSpace ipIpSpace) {
    return specialize(ipIpSpace.getIp());
  }

  @Override
  public IpSpace visitIpSpaceReference(IpSpaceReference ipSpaceReference) {
    String name = ipSpaceReference.getName();
    return _specializedNamedIpSpaces.computeIfAbsent(
        name, k -> _namedIpSpaces.get(name).accept(this));
  }

  @Override
  public IpSpace visitIpWildcardSetIpSpace(IpWildcardSetIpSpace ipWildcardSetIpSpace) {
    Set<IpSpace> blacklistIpSpace =
        ipWildcardSetIpSpace
            .getBlacklist()
            .stream()
            .map(this::specialize)
            .filter(ipSpace -> ipSpace != EmptyIpSpace.INSTANCE)
            .collect(ImmutableSet.toImmutableSet());

    if (blacklistIpSpace.contains(UniverseIpSpace.INSTANCE)) {
      return EmptyIpSpace.INSTANCE;
    }

    /*
     * A specialized IpWildcard is one of EmptyIpSpace, UniverseIpSpace, or IpWildcard.
     * We've already removed EmptyIpSpace and checked for UniverseIpSpace, so everything left
     * is an IpWildcard.
     */
    Set<IpWildcard> blacklist =
        blacklistIpSpace
            .stream()
            .map(IpWildcardIpSpace.class::cast)
            .map(IpWildcardIpSpace::getIpWildcard)
            .collect(ImmutableSet.toImmutableSet());

    Optional<IpSpaceSpecializer> optionalRefinedSpecializer =
        blacklist.isEmpty() ? Optional.of(this) : restrictSpecializerToBlacklist(blacklist);

    if (!optionalRefinedSpecializer.isPresent()) {
      /*
       * The refined specializer matches no IPs, so we can short-circuit and return empty here.
       */
      return EmptyIpSpace.INSTANCE;
    }

    IpSpaceSpecializer refinedSpecializer = optionalRefinedSpecializer.get();

    Set<IpSpace> ipSpaceWhitelist =
        ipWildcardSetIpSpace
            .getWhitelist()
            .stream()
            .map(refinedSpecializer::specialize)
            .filter(ipSpace -> ipSpace != EmptyIpSpace.INSTANCE)
            .collect(ImmutableSet.toImmutableSet());

    if (ipSpaceWhitelist.isEmpty()) {
      return EmptyIpSpace.INSTANCE;
    } else if (ipSpaceWhitelist.contains(UniverseIpSpace.INSTANCE)) {
      if (blacklist.isEmpty()) {
        return UniverseIpSpace.INSTANCE;
      } else {
        return IpWildcardSetIpSpace.builder()
            .including(IpWildcard.ANY)
            .excluding(blacklist)
            .build();
      }
    } else {
      Set<IpWildcard> ipWildcardWhitelist =
          ipSpaceWhitelist
              .stream()
              .map(IpWildcardIpSpace.class::cast)
              .map(IpWildcardIpSpace::getIpWildcard)
              .collect(Collectors.toSet());
      return IpWildcardSetIpSpace.builder()
          .including(ipWildcardWhitelist)
          .excluding(blacklist)
          .build();
    }
  }

  @Override
  public IpSpace visitIpWildcardIpSpace(IpWildcardIpSpace ipWildcardIpSpace) {
    return specialize(ipWildcardIpSpace.getIpWildcard());
  }

  @Override
  public IpSpace visitPrefixIpSpace(PrefixIpSpace prefixIpSpace) {
    IpSpace specialized = specialize(new IpWildcard(prefixIpSpace.getPrefix()));
    /*
     * visitIpWildcard can return EmptyIpSpace, UniverseIpSpace, or IpWildcard
     * If it returns IpWildcard, it will be unchanged, so return prefixIpSpace instead.
     */
    if (specialized instanceof IpWildcardIpSpace) {
      return prefixIpSpace;
    } else {
      return specialized;
    }
  }

  @Override
  public IpSpace visitUniverseIpSpace(UniverseIpSpace universeIpSpace) {
    return universeIpSpace;
  }
}
