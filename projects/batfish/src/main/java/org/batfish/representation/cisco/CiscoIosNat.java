package org.batfish.representation.cisco;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.transformation.IpField;
import org.batfish.datamodel.transformation.Transformation;

public abstract class CiscoIosNat implements Comparable<CiscoIosNat>, Serializable {
  private static final long serialVersionUID = 1L;

  private RuleAction _action;

  @Nonnull
  static Optional<Transformation> toOutgoingTransformationChain(
      Map<CiscoIosNat, Optional<Transformation.Builder>> convertedNats) {
    return toTransformationChain(convertedNats, true);
  }

  @Nonnull
  static Optional<Transformation> toIncomingTransformationChain(
      Map<CiscoIosNat, Optional<Transformation.Builder>> convertedNats) {
    return toTransformationChain(convertedNats, false);
  }

  @Nonnull
  private static Optional<Transformation> toTransformationChain(
      Map<CiscoIosNat, Optional<Transformation.Builder>> convertedNats, boolean outgoing) {

    Map<IpField, List<Transformation.Builder>> transformationsByField =
        convertedNats
            .keySet()
            .stream()
            .filter(nat -> convertedNats.get(nat).isPresent())
            .sorted()
            .collect(
                Collectors.groupingBy(
                    nat -> nat.whatChanges(outgoing),
                    Collectors.mapping(
                        nat -> convertedNats.get(nat).orElse(null), Collectors.toList())));

    if (!Sets.difference(
            transformationsByField.keySet(), ImmutableSet.of(IpField.SOURCE, IpField.DESTINATION))
        .isEmpty()) {
      throw new BatfishException("Invalid transformation field");
    }
    if (transformationsByField.isEmpty()) {
      return Optional.empty();
    }

    /*
    * transformationsByField contains non-empty lists of non-null transformations, sorted by the
    * order in which they are to be evaluated.

    * There are currently only two possible field transformations (source and destination).
    */
    List<Transformation.Builder> source = transformationsByField.get(IpField.SOURCE);
    List<Transformation.Builder> destination = transformationsByField.get(IpField.DESTINATION);

    // Doesn't matter if SOURCE or DESTINATION is transformed first, pick a non-empty list
    // If there is only one field modified, chain all transformations with orElse
    Optional<Transformation> onlyOrDestinationTransform =
        chain(firstNonNull(destination, source), null);
    if (transformationsByField.keySet().size() == 1 || !onlyOrDestinationTransform.isPresent()) {
      return onlyOrDestinationTransform;
    }

    // If there is more than one field, chain each list with orElse and subsequent lists with
    // andThen
    return chain(source, onlyOrDestinationTransform.get());
  }

  private static Optional<Transformation> chain(
      List<Transformation.Builder> nonEmptySortedList, @Nullable Transformation andThen) {
    // reduce is safe here because the operation is associative
    return Lists.reverse(nonEmptySortedList)
        .stream()
        .map(t -> t.setAndThen(andThen))
        .reduce((t1, t2) -> t2.setOrElseBuilder(t1))
        .map(Transformation.Builder::build);
  }

  private IpField whatChanges(boolean outgoing) {
    if (_action == RuleAction.SOURCE_INSIDE) {
      return outgoing ? IpField.SOURCE : IpField.DESTINATION;
    }
    return outgoing ? IpField.DESTINATION : IpField.SOURCE;
  }

  public RuleAction getAction() {
    return _action;
  }

  public void setAction(RuleAction action) {
    _action = action;
  }

  public abstract Optional<Transformation.Builder> toOutgoingTransformation(
      Map<String, IpAccessList> ipAccessLists,
      Map<String, NatPool> natPools,
      @Nullable Set<String> insideInterfaces,
      Configuration c);

  public abstract Optional<Transformation.Builder> toIncomingTransformation(
      Map<String, NatPool> natPools);

  @Override
  public abstract boolean equals(Object o);

  @Override
  public abstract int hashCode();

  private int typeCompare(CiscoIosNat other) {
    if (other.getClass() == this.getClass()) {
      return 0;
    }
    if (this instanceof CiscoIosStaticNat) {
      return -1;
    }
    return 0;
  }

  public int natCompare(CiscoIosNat other) {
    return 0;
  }

  @Override
  public int compareTo(@Nonnull CiscoIosNat other) {
    return Comparator.comparing(this::typeCompare)
        .thenComparing(this::natCompare)
        .compare(this, other);
  }

  public enum RuleAction {
    SOURCE_INSIDE,
    SOURCE_OUTSIDE,
    DESTINATION_INSIDE
  }
}
