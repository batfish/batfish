package org.batfish.specifier;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.batfish.datamodel.FlowDisposition.FAILURE_DISPOSITIONS;
import static org.batfish.datamodel.FlowDisposition.SUCCESS_DISPOSITIONS;

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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.answers.AutoCompleteUtils;
import org.batfish.datamodel.answers.AutocompleteSuggestion;

/** A way to specify dispositions using a shorthand, such as "success" or "failure". */
@ParametersAreNonnullByDefault
public final class DispositionSpecifier {

  @VisibleForTesting public static final String SUCCESS = "success";
  @VisibleForTesting public static final String FAILURE = "failure";

  private static final Map<String, Set<FlowDisposition>> _expansions =
      ImmutableMap.of(
          SUCCESS, SUCCESS_DISPOSITIONS, //
          FAILURE, FAILURE_DISPOSITIONS);
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
  static DispositionSpecifier create(@Nullable String values) {
    return new DispositionSpecifier(fromString(firstNonNull(values, SUCCESS)));
  }

  private static Set<FlowDisposition> fromString(String s) {
    String[] values = s.trim().split(",");
    return Arrays.stream(values)
        .map(String::trim)
        .map(String::toLowerCase)
        .map(_map::get)
        .filter(Objects::nonNull)
        .flatMap(Set::stream)
        .collect(ImmutableSet.toImmutableSet());
  }

  @JsonValue
  public String value() {
    return String.join(
        ",",
        _dispositions.stream()
            .map(FlowDisposition::name)
            .collect(ImmutableSortedSet.toImmutableSortedSet(Comparator.naturalOrder())));
  }

  public DispositionSpecifier(Set<FlowDisposition> dispositions) {
    _dispositions = dispositions;
  }

  /**
   * Returns a list of suggestions based on the query, based on {@link
   * AutoCompleteUtils#baseAutoComplete}.
   */
  public static List<AutocompleteSuggestion> autoComplete(String query) {
    return AutoCompleteUtils.baseAutoComplete(query, _map.keySet());
  }

  public Set<FlowDisposition> getDispositions() {
    return _dispositions;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof DispositionSpecifier)) {
      return false;
    }
    DispositionSpecifier that = (DispositionSpecifier) o;
    return Objects.equals(_dispositions, that._dispositions);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_dispositions);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("dispositions", _dispositions).toString();
  }
}
