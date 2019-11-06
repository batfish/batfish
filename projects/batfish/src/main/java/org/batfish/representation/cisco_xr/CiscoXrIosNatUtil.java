package org.batfish.representation.cisco_xr;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.transformation.IpField;
import org.batfish.datamodel.transformation.Transformation;
import org.batfish.datamodel.transformation.Transformation.Builder;

/** Utility methods related to {@link CiscoXrIosNat}. */
@ParametersAreNonnullByDefault
final class CiscoXrIosNatUtil {
  /*
   * Initialize the type precedence for the supported NAT types.
   * Currently, the only rule is that Static NATs take precedence over dynamic NATs
   */
  private static final ImmutableMap<String, Integer> typePrecedence =
      ImmutableMap.of(
          CiscoXrIosDynamicNat.class.getSimpleName(), 1,
          CiscoXrIosStaticNat.class.getSimpleName(), 0);

  private CiscoXrIosNatUtil() {}

  /** The relative order of precedence for this NAT's type */
  static int getTypePrecedence(CiscoXrIosNat nat) {
    String name = nat.getClass().getSimpleName();
    if (!typePrecedence.containsKey(name)) {
      throw new BatfishException("Unsupported NAT type");
    }
    return typePrecedence.get(name);
  }

  @Nonnull
  static Transformation toOutgoingTransformationChain(Map<CiscoXrIosNat, Builder> convertedNats) {
    return toTransformationChain(convertedNats, true);
  }

  @Nonnull
  static Transformation toIncomingTransformationChain(
      Map<CiscoXrIosNat, Transformation.Builder> convertedNats) {
    return toTransformationChain(convertedNats, false);
  }

  @Nonnull
  private static Transformation toTransformationChain(
      Map<CiscoXrIosNat, Transformation.Builder> convertedNats, boolean outgoing) {

    Map<IpField, List<Builder>> transformationsByField =
        convertedNats.keySet().stream()
            .sorted()
            .collect(
                Collectors.groupingBy(
                    nat -> nat.getAction().whatChanges(outgoing),
                    Collectors.mapping(convertedNats::get, Collectors.toList())));

    if (!Sets.difference(
                transformationsByField.keySet(),
                ImmutableSet.of(IpField.SOURCE, IpField.DESTINATION))
            .isEmpty()
        || transformationsByField.isEmpty()) {
      throw new BatfishException("Invalid transformations");
    }

    /*
    * transformationsByField contains non-empty lists of non-null transformations, sorted by the
    * order in which they are to be evaluated.

    * There are currently only two possible field transformations (source and destination).
    * Doesn't matter if SOURCE or DESTINATION is transformed first, pick a non-empty list
    * This is true so far for IOS NATs.
    */
    List<Transformation.Builder> source = transformationsByField.get(IpField.SOURCE);
    List<Transformation.Builder> destination = transformationsByField.get(IpField.DESTINATION);

    // If there is only one field modified, chain all transformations with orElse
    Transformation onlyOrDestinationTransform = chain(firstNonNull(destination, source), null);
    if (transformationsByField.keySet().size() == 1) {
      return onlyOrDestinationTransform;
    }

    // If there is more than one field, chain each list with orElse and subsequent lists with
    // andThen
    return chain(source, onlyOrDestinationTransform);
  }

  private static Transformation chain(
      List<Transformation.Builder> nonEmptySortedList, @Nullable Transformation andThen) {
    // Start at the end of the chain and go backwards. The end of the chain should have
    // t.andThen == t.orElse
    Transformation previous = andThen;
    for (Transformation.Builder t : Lists.reverse(nonEmptySortedList)) {
      previous = t.setAndThen(andThen).setOrElse(previous).build();
    }
    return previous;
  }
}
