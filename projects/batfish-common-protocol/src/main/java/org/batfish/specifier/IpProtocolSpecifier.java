package org.batfish.specifier;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.answers.AutocompleteSuggestion;
import org.batfish.datamodel.questions.PropertySpecifier;

@ParametersAreNonnullByDefault
public class IpProtocolSpecifier {

  @VisibleForTesting
  static final Pattern NAME_AND_NUMBER_PATTERN =
      Pattern.compile("[0-9]+ \\((.+)\\)", Pattern.CASE_INSENSITIVE);

  private static final Pattern UNNAMED_PATTERN = Pattern.compile("UNNAMED.*");

  private static final Set<String> COMPLETIONS;

  static {
    COMPLETIONS =
        Stream.of(IpProtocol.values())
            .map(
                ipProtocol -> {
                  if (UNNAMED_PATTERN.matcher(ipProtocol.name()).find()) {
                    // don't include UNNAMED... protocol names
                    return String.format("%d", ipProtocol.number());
                  }
                  return String.format("%d (%s)", ipProtocol.number(), ipProtocol.name());
                })
            .collect(ImmutableSet.toImmutableSet());
  }

  private final String _text;

  private final Set<IpProtocol> _protocols;

  @JsonCreator
  @VisibleForTesting
  static IpProtocolSpecifier create(@Nullable String text) {
    return new IpProtocolSpecifier(text);
  }

  private static IpProtocol fromString(String text) {
    Matcher matcher = IpProtocolSpecifier.NAME_AND_NUMBER_PATTERN.matcher(text);
    if (matcher.find()) {
      return IpProtocol.fromString(matcher.group(1));
    }
    return IpProtocol.fromString(text);
  }

  /**
   * Parse IP protocols fields in backwards-compatible way, accepting either a list strings or a
   * comma-separated string.
   *
   * @param node {@link JsonNode} to parse
   * @return valid string representation to be used in {@link #expandProtocols(String)}
   * @throws IllegalArgumentException if the value is not valid
   */
  @Nullable
  private static String parseIpProtocols(@Nullable JsonNode node) {
    if (node == null || node.isNull()) {
      return null;
    } else if (node.isTextual()) {
      return node.textValue();
    } else if (node.isArray()) {
      return String.join(
          ",",
          Streams.stream(node.elements())
              .map(JsonNode::textValue)
              .collect(ImmutableSet.toImmutableSet()));
    } else {
      throw new IllegalArgumentException(
          String.format("Invalid value %s for Ip protocols", node.asText()));
    }
  }

  @Nullable
  @VisibleForTesting
  static Set<IpProtocol> expandProtocols(@Nullable String ipProtocols) {
    if (Strings.isNullOrEmpty(ipProtocols)) {
      return null;
    }
    String[] atoms = ipProtocols.trim().split(",");
    ImmutableSet.Builder<IpProtocol> including = ImmutableSet.builder();
    ImmutableSet.Builder<IpProtocol> excluding = ImmutableSet.builder();
    for (String atom : atoms) {
      String trimmed = atom.trim();
      if (trimmed.startsWith("!")) {
        excluding.add(fromString(trimmed.replaceFirst("!", "")));
      } else {
        including.add(fromString(trimmed));
      }
    }

    if (including.build().isEmpty()) {
      including.addAll(Arrays.asList(IpProtocol.values()));
    }

    return ImmutableSet.copyOf(Sets.difference(including.build(), excluding.build()));
  }

  @JsonValue
  public @Nonnull String value() {
    return _text;
  }

  public IpProtocolSpecifier(@Nullable JsonNode node) {
    _text = parseIpProtocols(node);
    _protocols = expandProtocols(_text);
  }

  public IpProtocolSpecifier(@Nullable String text) {
    _text = text;
    _protocols = expandProtocols(text);
  }

  /**
   * Returns a list of suggestions based on the query, based on {@link
   * PropertySpecifier#baseAutoComplete}. Since this specifier accepts comma-separated protocols,
   * only the partial query after the final comma will be considered for matching against. Each
   * suggestion returned will contain everything before the final comma followed by an
   * autocompletion that matches the partial query after the final comma.
   *
   * <pre>
   * Example:
   *    query: "89 (OSPF), 18 (MUX), arp"
   *    suggestions: ["89 (OSPF), 18 (MUX), 91 (LARP)", "89 (OSPF), 18 (MUX), 54 (NARP)"]
   * </pre>
   */
  public static List<AutocompleteSuggestion> autoComplete(String query) {
    // take out any commas
    String[] atoms = query.split(",");

    // only want autocompletions for the query after the final comma
    String currentQuery = atoms[atoms.length - 1].trim();

    // escape any parentheses
    String escaped = currentQuery.replace("(", "\\(").replace(")", "\\)");

    // save exclamation point if query starts with one
    String startsWith = escaped.startsWith("!") ? "!" : "";

    // need to remove exclamation point before matching
    String noExclamation = escaped.replaceFirst("!", "");

    return PropertySpecifier.baseAutoComplete(noExclamation.trim(), COMPLETIONS).stream()
        .map(
            suggestion ->
                new AutocompleteSuggestion(
                    createSuggestionText(atoms, startsWith + suggestion.getText()),
                    suggestion.getIsPartial(),
                    suggestion.getDescription(),
                    suggestion.getRank()))
        .collect(ImmutableList.toImmutableList());
  }

  // create suggestion text from original array of individual protocol strings and the
  // autocompletion text for the partial query after the final comma
  private static String createSuggestionText(String[] atoms, String newSuggestion) {
    // replace last entry with new suggestion
    atoms[atoms.length - 1] = (atoms.length > 1 ? " " : "") + newSuggestion;
    return String.join(",", Stream.of(atoms).collect(Collectors.toList()));
  }

  public Set<IpProtocol> getProtocols() {
    return _protocols;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof IpProtocolSpecifier)) {
      return false;
    }
    IpProtocolSpecifier that = (IpProtocolSpecifier) o;
    return Objects.equals(_protocols, that._protocols) && _text.equals(that._text);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_protocols, _text);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("stringValue", _text)
        .add("protocols", _protocols)
        .omitNullValues()
        .toString();
  }
}
