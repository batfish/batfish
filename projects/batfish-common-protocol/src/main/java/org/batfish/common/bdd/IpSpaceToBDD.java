package org.batfish.common.bdd;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.common.util.CollectionUtil.toImmutableMap;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.common.BatfishException;
import org.batfish.common.util.NonRecursiveSupplier;
import org.batfish.common.util.NonRecursiveSupplier.NonRecursiveSupplierException;
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
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixIpSpace;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.visitors.GenericIpSpaceVisitor;

/**
 * Visitor that converts an {@link IpSpace} to a {@link BDD}. Its constructor takes a {@link
 * BDDInteger} that should will be constrained to be in the space.
 *
 * <p>For all implementations, it must be the case that all returned {@link BDD} objects are owned
 * by the caller and may be freed.
 */
public class IpSpaceToBDD implements GenericIpSpaceVisitor<BDD> {

  private final BDDInteger _bddInteger;

  private final BDDOps _bddOps;

  private final BDDFactory _factory;

  private final Map<String, Supplier<BDD>> _namedIpSpaceBDDs;

  public IpSpaceToBDD(BDDInteger var) {
    _bddInteger = var;
    _factory = var.getFactory();
    _bddOps = new BDDOps(_factory);
    _namedIpSpaceBDDs = ImmutableMap.of();
  }

  public IpSpaceToBDD(BDDInteger var, Map<String, IpSpace> namedIpSpaces) {
    _bddInteger = var;
    _factory = var.getFactory();
    _bddOps = new BDDOps(_factory);
    _namedIpSpaceBDDs =
        toImmutableMap(
            namedIpSpaces,
            Entry::getKey,
            entry -> Suppliers.memoize(new NonRecursiveSupplier<>(() -> visit(entry.getValue()))));
  }

  public BDDInteger getBDDInteger() {
    return _bddInteger;
  }

  public BDD toBDD(Ip ip) {
    return _bddInteger.toBDD(ip);
  }

  public BDD toBDD(IpWildcard ipWildcard) {
    return _bddInteger.toBDD(ipWildcard);
  }

  public BDD toBDD(Prefix prefix) {
    return _bddInteger.toBDD(prefix);
  }

  @Override
  public BDD visitAclIpSpace(AclIpSpace aclIpSpace) {
    int size = aclIpSpace.getLines().size();
    List<BDD> lineBdds = new ArrayList<>(size);
    List<LineAction> lineActions = new ArrayList<>(size);
    for (AclIpSpaceLine line : aclIpSpace.getLines()) {
      lineActions.add(line.getAction());
      lineBdds.add(visit(line.getIpSpace()));
    }
    return _bddOps.bddAclLines(lineBdds, lineActions);
  }

  @Override
  public BDD visitEmptyIpSpace(EmptyIpSpace emptyIpSpace) {
    return _factory.zero();
  }

  @Override
  public BDD visitIpIpSpace(IpIpSpace ipIpSpace) {
    return toBDD(ipIpSpace.getIp());
  }

  @Override
  public BDD visitIpWildcardIpSpace(IpWildcardIpSpace ipWildcardIpSpace) {
    return toBDD(ipWildcardIpSpace.getIpWildcard());
  }

  @Override
  public BDD visitIpSpaceReference(IpSpaceReference ipSpaceReference) {
    String name = ipSpaceReference.getName();
    checkArgument(_namedIpSpaceBDDs.containsKey(name), "Undefined IpSpace reference: %s", name);
    try {
      return _namedIpSpaceBDDs.get(name).get().id();
    } catch (NonRecursiveSupplierException e) {
      throw new BatfishException("Circular IpSpaceReference: " + name);
    }
  }

  @Override
  public BDD visitIpWildcardSetIpSpace(IpWildcardSetIpSpace ipWildcardSetIpSpace) {
    BDD whitelist =
        _bddOps.or(
            ipWildcardSetIpSpace.getWhitelist().stream()
                .map((IpWildcard wc) -> visit(wc.toIpSpace()))
                .collect(Collectors.toList()));

    BDD blacklist =
        _bddOps.or(
            ipWildcardSetIpSpace.getBlacklist().stream()
                .map((IpWildcard wc) -> visit(wc.toIpSpace()))
                .collect(Collectors.toList()));

    return whitelist.diffWith(blacklist);
  }

  @Override
  public BDD visitPrefixIpSpace(PrefixIpSpace prefixIpSpace) {
    return toBDD(prefixIpSpace.getPrefix());
  }

  @Override
  public BDD visitUniverseIpSpace(UniverseIpSpace universeIpSpace) {
    return _factory.one();
  }
}
