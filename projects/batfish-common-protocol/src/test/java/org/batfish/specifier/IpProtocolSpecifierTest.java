package org.batfish.specifier;

import static org.batfish.specifier.IpProtocolSpecifier.NAME_AND_NUMBER_PATTERN;
import static org.batfish.specifier.IpProtocolSpecifier.expandProtocols;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import org.batfish.common.BatfishException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.answers.AutocompleteSuggestion;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests of {@link IpProtocolSpecifier} */
public final class IpProtocolSpecifierTest {

  @Rule public ExpectedException thrown = ExpectedException.none();

  private ObjectMapper _mapper = BatfishObjectMapper.mapper();

  @Test
  public void testPattern() {
    Matcher matcher = NAME_AND_NUMBER_PATTERN.matcher("89 (OSPF)");
    assertThat(matcher.find(), equalTo(true));
    assertThat(matcher.group(1), equalTo("OSPF"));
  }

  @Test
  public void testAutocomplete() {
    assertThat(
        IpProtocolSpecifier.autoComplete("os").stream()
            .map(AutocompleteSuggestion::getText)
            .collect(ImmutableSet.toImmutableSet()),
        equalTo(ImmutableSet.of("89 (OSPF)", "16 (CHAOS)", "61 (ANY_HOST_INTERNAL_PROTOCOL)")));

    assertThat(
        IpProtocolSpecifier.autoComplete("89").stream()
            .map(AutocompleteSuggestion::getText)
            .collect(ImmutableSet.toImmutableSet()),
        equalTo(ImmutableSet.of("89 (OSPF)", "189"))); // 189 is UNNAMED so shouldn't include name

    // should not suggest any "UNNAMED" protocols
    assertThat(IpProtocolSpecifier.autoComplete("UNNAMED"), equalTo(ImmutableList.of()));

    assertThat(
        IpProtocolSpecifier.autoComplete("89 (o").stream()
            .map(AutocompleteSuggestion::getText)
            .collect(ImmutableSet.toImmutableSet()),
        equalTo(ImmutableSet.of("89 (OSPF)")));

    assertThat(
        IpProtocolSpecifier.autoComplete("89 (ospf), 18 (").stream()
            .map(AutocompleteSuggestion::getText)
            .collect(ImmutableSet.toImmutableSet()),
        equalTo(ImmutableSet.of(" 118 (STP)", " 18 (MUX)")));

    // trailing comma followed by a space should return suggestions for all protocols
    assertThat(
        IpProtocolSpecifier.autoComplete("89 (ospf), ").stream()
            .map(AutocompleteSuggestion::getText)
            .collect(ImmutableSet.toImmutableSet()),
        equalTo(
            IpProtocolSpecifier.COMPLETIONS.stream()
                .map(completion -> " " + completion)
                .collect(ImmutableSet.toImmutableSet())));

    // trailing comma followed by no space should return suggestions for all protocols
    assertThat(
        IpProtocolSpecifier.autoComplete("89 (ospf),").stream()
            .map(AutocompleteSuggestion::getText)
            .collect(ImmutableSet.toImmutableSet()),
        equalTo(
            IpProtocolSpecifier.COMPLETIONS.stream()
                .map(completion -> " " + completion)
                .collect(ImmutableSet.toImmutableSet())));
  }

  @Test
  public void testAutocompleteInsertionIndex() {
    // insertion index should be 0 if no commas present
    List<AutocompleteSuggestion> suggestions = IpProtocolSpecifier.autoComplete("89 (o");
    assertThat(suggestions.size(), greaterThan(0));
    for (AutocompleteSuggestion suggestion : suggestions) {
      assertThat(suggestion.getInsertionIndex(), equalTo(0));
    }

    // insertion index should be 1 past the last comma
    suggestions = IpProtocolSpecifier.autoComplete("89, 61");
    assertThat(suggestions.size(), greaterThan(0));
    for (AutocompleteSuggestion suggestion : suggestions) {
      assertThat(suggestion.getInsertionIndex(), equalTo(3));
    }

    // only the final comma determines the insertion index
    suggestions = IpProtocolSpecifier.autoComplete(",, 89, abc, , , ,,76,");
    assertThat(suggestions.size(), greaterThan(0));
    for (AutocompleteSuggestion suggestion : suggestions) {
      assertThat(suggestion.getInsertionIndex(), equalTo(21));
    }
  }

  @Test
  public void testConstructorValid() {
    assertThat(
        new IpProtocolSpecifier("89 (OSpf), 61, chaos").getProtocols(),
        equalTo(
            ImmutableSet.of(
                IpProtocol.OSPF, IpProtocol.CHAOS, IpProtocol.ANY_HOST_INTERNAL_PROTOCOL)));
  }

  @Test
  public void testConstructorValidJsonNode() throws IOException {
    JsonNode jsonNode = _mapper.readTree("[\"89 (OSpf)\", \"61\", \"chaos\"]");
    assertThat(
        new IpProtocolSpecifier(jsonNode).getProtocols(),
        equalTo(
            ImmutableSet.of(
                IpProtocol.OSPF, IpProtocol.CHAOS, IpProtocol.ANY_HOST_INTERNAL_PROTOCOL)));
  }

  @Test
  public void testConstructorInvalid() {
    thrown.expect(IllegalArgumentException.class);
    assertThat(
        new IpProtocolSpecifier("blah, (ospf) 89, ").getProtocols(), equalTo(ImmutableSet.of()));
  }

  @Test
  public void testConstructorInvalidJsonNode() throws IOException {
    thrown.expect(IllegalArgumentException.class);
    JsonNode jsonNode = _mapper.readTree("[\"blah\", \"(chaos) 61\"]");
    new IpProtocolSpecifier(jsonNode);
  }

  @Test
  public void testCreateEqualsConstructor() {
    String input = "89 (OSpf), 61, chaos";
    assertThat(IpProtocolSpecifier.create(input), equalTo(new IpProtocolSpecifier(input)));
  }

  @Test
  public void testIpProtocolExpansion() {
    assertThat(expandProtocols(null), nullValue());
    assertThat(expandProtocols(""), nullValue());
    assertThat(expandProtocols("TCP"), contains(IpProtocol.TCP));
    assertThat(expandProtocols(" TCP , UDP"), containsInAnyOrder(IpProtocol.TCP, IpProtocol.UDP));
    assertThat(expandProtocols("6,17"), containsInAnyOrder(IpProtocol.TCP, IpProtocol.UDP));
    assertThat(expandProtocols("6,!17"), containsInAnyOrder(IpProtocol.TCP));
    assertThat(expandProtocols("TCP, !UDP"), containsInAnyOrder(IpProtocol.TCP));
    assertThat(
        expandProtocols("!UDP"),
        equalTo(
            Sets.difference(
                ImmutableSet.copyOf(IpProtocol.values()), ImmutableSet.of(IpProtocol.UDP))));
  }

  @Test
  public void testIpProtocolExpansionWrongValues() {
    assertThat(expandProtocols(null), nullValue());
    thrown.expect(IllegalArgumentException.class);
    expandProtocols("TC!P");
  }

  @Test
  public void testIpProtocolExpansionNoComma() {
    thrown.expect(IllegalArgumentException.class);
    expandProtocols("!TCP!UDP");
  }

  @Test
  public void testIpProtocolExpansionInvalidInt() {
    thrown.expect(BatfishException.class);
    expandProtocols("257");
  }
}
