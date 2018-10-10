package org.batfish.question.specifiers;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.batfish.datamodel.FlowDisposition.ACCEPTED;
import static org.batfish.datamodel.FlowDisposition.DENIED_IN;
import static org.batfish.datamodel.FlowDisposition.DENIED_OUT;
import static org.batfish.datamodel.FlowDisposition.LOOP;
import static org.batfish.datamodel.FlowDisposition.NEIGHBOR_UNREACHABLE_OR_EXITS_NETWORK;
import static org.batfish.datamodel.FlowDisposition.NO_ROUTE;
import static org.batfish.datamodel.FlowDisposition.NULL_ROUTED;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;
import org.batfish.datamodel.FlowDisposition;

/** A way to specify dispositions using a shorthand, such as "success" or "failure". */
public class DispositionSpecifier {

  private static final String SUCCESS = "success";
  private static final String FAILURE = "failure";

  @VisibleForTesting
  static final Set<FlowDisposition> FAILURE_DISPOSITIONS =
      ImmutableSet.<FlowDisposition>builder()
          .add(DENIED_IN)
          .add(DENIED_OUT)
          .add(LOOP)
          .add(NEIGHBOR_UNREACHABLE_OR_EXITS_NETWORK)
          .add(NO_ROUTE)
          .add(NULL_ROUTED)
          .build();

  private static final Map<String, Set<FlowDisposition>> _expansions =
      ImmutableMap.<String, Set<FlowDisposition>>builder()
          .put(SUCCESS, ImmutableSet.of(ACCEPTED))
          .put(FAILURE, FAILURE_DISPOSITIONS)
          .build();
  private static final Map<String, Set<FlowDisposition>> _map = getMap();

  private static Map<String, Set<FlowDisposition>> getMap() {
    Builder<String, Set<FlowDisposition>> builder = ImmutableMap.builder();
    builder.putAll(_expansions);
    Arrays.asList(FlowDisposition.values())
        .forEach(
            disposition ->
                builder.put(disposition.name().toLowerCase(), ImmutableSet.of(disposition)));
    return builder.build();
  }

  /** A specifier that expands to all successful dispositions */
  public static final DispositionSpecifier SUCCESS_SPECIFIER =
      new DispositionSpecifier(fromString(SUCCESS));

  /** A specifier that expands to all failure dispositions */
  public static final DispositionSpecifier FAILURE_SPECIFIER =
      new DispositionSpecifier(fromString(FAILURE));

  private final Set<FlowDisposition> _dispositions;

  @JsonCreator
  @VisibleForTesting
  static DispositionSpecifier create(@Nullable Set<String> values) {
    values = firstNonNull(values, ImmutableSet.of());
    if (values.isEmpty()) {
      return new DispositionSpecifier(fromString(SUCCESS));
    }
    return new DispositionSpecifier(fromIterable(values));
  }

  private static Set<FlowDisposition> fromIterable(Iterable<String> values) {
    ImmutableSet.Builder<FlowDisposition> builder = ImmutableSet.builder();
    values.forEach(s -> builder.addAll(fromString(s)));
    return builder.build();
  }

  private static Set<FlowDisposition> fromString(String s) {
    return _map.get(s.toLowerCase());
  }

  @JsonValue
  private Set<String> value() {
    return _dispositions
        .stream()
        .map(FlowDisposition::name)
        .collect(ImmutableSortedSet.toImmutableSortedSet(Comparator.naturalOrder()));
  }

  public DispositionSpecifier(Set<FlowDisposition> dispositions) {
    _dispositions = dispositions;
  }

  public Set<FlowDisposition> getDispositions() {
    return _dispositions;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DispositionSpecifier that = (DispositionSpecifier) o;
    return Objects.equals(_dispositions, that._dispositions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_dispositions);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("dispositions", _dispositions).toString();
  }
}
