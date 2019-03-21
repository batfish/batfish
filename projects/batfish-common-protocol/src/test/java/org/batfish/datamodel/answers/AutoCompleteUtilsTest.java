package org.batfish.datamodel.answers;

import static org.batfish.datamodel.BgpSessionProperties.SessionType.EBGP_MULTIHOP;
import static org.batfish.datamodel.BgpSessionProperties.SessionType.EBGP_SINGLEHOP;
import static org.batfish.datamodel.BgpSessionProperties.SessionType.IBGP;
import static org.batfish.datamodel.FlowDisposition.DELIVERED_TO_SUBNET;
import static org.batfish.datamodel.FlowDisposition.EXITS_NETWORK;
import static org.batfish.datamodel.FlowDisposition.INSUFFICIENT_INFO;
import static org.batfish.datamodel.FlowDisposition.NEIGHBOR_UNREACHABLE_OR_EXITS_NETWORK;
import static org.batfish.datamodel.FlowState.ESTABLISHED;
import static org.batfish.datamodel.FlowState.NEW;
import static org.batfish.datamodel.FlowState.RELATED;
import static org.batfish.datamodel.Protocol.HTTP;
import static org.batfish.datamodel.Protocol.HTTPS;
import static org.batfish.datamodel.Protocol.SSH;
import static org.batfish.datamodel.answers.AutoCompleteUtils.stringAutoComplete;
import static org.batfish.datamodel.answers.AutoCompleteUtils.stringPairAutoComplete;
import static org.batfish.datamodel.questions.BgpPeerPropertySpecifier.IS_PASSIVE;
import static org.batfish.datamodel.questions.BgpPeerPropertySpecifier.LOCAL_AS;
import static org.batfish.datamodel.questions.BgpPeerPropertySpecifier.REMOTE_AS;
import static org.batfish.datamodel.questions.BgpProcessPropertySpecifier.MULTIPATH_EBGP;
import static org.batfish.datamodel.questions.BgpProcessPropertySpecifier.MULTIPATH_EQUIVALENT_AS_PATH_MATCH_MODE;
import static org.batfish.datamodel.questions.BgpProcessPropertySpecifier.MULTIPATH_IBGP;
import static org.batfish.datamodel.questions.ConfiguredSessionStatus.DYNAMIC_MATCH;
import static org.batfish.datamodel.questions.ConfiguredSessionStatus.NO_MATCH_FOUND;
import static org.batfish.datamodel.questions.ConfiguredSessionStatus.UNIQUE_MATCH;
import static org.batfish.datamodel.questions.InterfacePropertySpecifier.ACCESS_VLAN;
import static org.batfish.datamodel.questions.InterfacePropertySpecifier.ALLOWED_VLANS;
import static org.batfish.datamodel.questions.InterfacePropertySpecifier.AUTO_STATE_VLAN;
import static org.batfish.datamodel.questions.InterfacePropertySpecifier.ENCAPSULATION_VLAN;
import static org.batfish.datamodel.questions.InterfacePropertySpecifier.NATIVE_VLAN;
import static org.batfish.datamodel.questions.IpsecSessionStatus.IKE_PHASE1_FAILED;
import static org.batfish.datamodel.questions.IpsecSessionStatus.IKE_PHASE1_KEY_MISMATCH;
import static org.batfish.datamodel.questions.IpsecSessionStatus.IPSEC_PHASE2_FAILED;
import static org.batfish.datamodel.questions.NamedStructureSpecifier.AS_PATH_ACCESS_LIST;
import static org.batfish.datamodel.questions.NamedStructureSpecifier.IP_6_ACCESS_LIST;
import static org.batfish.datamodel.questions.NamedStructureSpecifier.IP_ACCESS_LIST;
import static org.batfish.datamodel.questions.NodePropertySpecifier.DNS_SERVERS;
import static org.batfish.datamodel.questions.NodePropertySpecifier.DNS_SOURCE_INTERFACE;
import static org.batfish.datamodel.questions.OspfPropertySpecifier.AREAS;
import static org.batfish.datamodel.questions.OspfPropertySpecifier.AREA_BORDER_ROUTER;
import static org.batfish.specifier.DispositionSpecifier.SUCCESS;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.batfish.common.CompletionMetadata;
import org.batfish.datamodel.answers.AutoCompleteUtils.StringPair;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.questions.NodePropertySpecifier;
import org.batfish.datamodel.questions.Variable.Type;
import org.batfish.referencelibrary.AddressGroup;
import org.batfish.referencelibrary.ReferenceBook;
import org.batfish.referencelibrary.ReferenceLibrary;
import org.batfish.role.NodeRoleDimension;
import org.batfish.role.NodeRolesData;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class AutoCompleteUtilsTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  private static Set<String> getSuggestionsTextSet(List<AutocompleteSuggestion> suggestions) {
    return suggestions.stream()
        .map(AutocompleteSuggestion::getText)
        .collect(ImmutableSet.toImmutableSet());
  }

  @Test
  public void testBaseAutoComplete() {
    Set<String> properties =
        ImmutableSet.of(
            "abc", NodePropertySpecifier.NTP_SERVERS, NodePropertySpecifier.NTP_SOURCE_INTERFACE);

    // null or empty string should yield all options
    assertThat(
        AutoCompleteUtils.baseAutoComplete(null, properties).stream()
            .map(s -> s.getText())
            .collect(Collectors.toList()),
        equalTo(ImmutableList.builder().addAll(properties).build()));

    // the capital P shouldn't matter and this should autoComplete to two entries
    assertThat(
        new ArrayList<>(AutoCompleteUtils.baseAutoComplete("ntP", properties)),
        equalTo(
            ImmutableList.of(
                new AutocompleteSuggestion(NodePropertySpecifier.NTP_SERVERS, false),
                new AutocompleteSuggestion(NodePropertySpecifier.NTP_SOURCE_INTERFACE, false))));
  }

  @Test
  public void testAddressGroupAndBookAutocomplete() throws IOException {
    ReferenceLibrary library =
        new ReferenceLibrary(
            ImmutableList.of(
                ReferenceBook.builder("b1")
                    .setAddressGroups(
                        ImmutableList.of(
                            new AddressGroup(null, "g1"), new AddressGroup(null, "a1")))
                    .build(),
                ReferenceBook.builder("b2")
                    .setAddressGroups(ImmutableList.of(new AddressGroup(null, "g2")))
                    .build()));

    // empty matches all possibilities
    assertThat(
        AutoCompleteUtils.autoComplete(
                "network", "snapshot", Type.ADDRESS_GROUP_AND_BOOK, " ", 5, null, null, library)
            .stream()
            .map(AutocompleteSuggestion::getText)
            .collect(Collectors.toSet()),
        equalTo(ImmutableSet.of("g1,b1", "a1,b1", "g2,b2")));

    // 'g' matches two pairs
    assertThat(
        AutoCompleteUtils.autoComplete(
                "network", "snapshot", Type.ADDRESS_GROUP_AND_BOOK, " G ", 5, null, null, library)
            .stream()
            .map(AutocompleteSuggestion::getText)
            .collect(Collectors.toSet()),
        equalTo(ImmutableSet.of("g1,b1", "g2,b2")));

    // 'g1' matches one pair
    assertThat(
        AutoCompleteUtils.autoComplete(
                "network",
                "snapshot",
                Type.ADDRESS_GROUP_AND_BOOK,
                " G1 , ",
                5,
                null,
                null,
                library)
            .stream()
            .map(AutocompleteSuggestion::getText)
            .collect(Collectors.toSet()),
        equalTo(ImmutableSet.of("g1,b1")));
  }

  @Test
  public void testBgpPeerPropertySpecAutocomplete() throws IOException {
    assertThat(
        AutoCompleteUtils.autoComplete(Type.BGP_PEER_PROPERTY_SPEC, "as", 5).stream()
            .map(AutocompleteSuggestion::getText)
            .collect(Collectors.toSet()),
        equalTo(ImmutableSet.of(LOCAL_AS, IS_PASSIVE, REMOTE_AS)));
  }

  @Test
  public void testBgpProcessPropertySpecAutocomplete() throws IOException {
    assertThat(
        AutoCompleteUtils.autoComplete(Type.BGP_PROCESS_PROPERTY_SPEC, "multi", 5).stream()
            .map(AutocompleteSuggestion::getText)
            .collect(Collectors.toSet()),
        equalTo(
            ImmutableSet.of(
                MULTIPATH_EQUIVALENT_AS_PATH_MATCH_MODE, MULTIPATH_EBGP, MULTIPATH_IBGP)));
  }

  @Test
  public void testBgpSessionStatusAutocomplete() throws IOException {
    assertThat(
        AutoCompleteUtils.autoComplete(Type.BGP_SESSION_STATUS, "match", 5).stream()
            .map(AutocompleteSuggestion::getText)
            .collect(Collectors.toSet()),
        equalTo(
            ImmutableSet.of(
                DYNAMIC_MATCH.toString(), NO_MATCH_FOUND.toString(), UNIQUE_MATCH.toString())));
  }

  @Test
  public void testBgpSessionTypeAutocomplete() throws IOException {
    assertThat(
        AutoCompleteUtils.autoComplete(Type.BGP_SESSION_TYPE, "bgp", 5).stream()
            .map(AutocompleteSuggestion::getText)
            .collect(Collectors.toSet()),
        equalTo(
            ImmutableSet.of(IBGP.toString(), EBGP_SINGLEHOP.toString(), EBGP_MULTIHOP.toString())));
  }

  @Test
  public void testDispositionSpecAutocomplete() throws IOException {
    assertThat(
        AutoCompleteUtils.autoComplete(Type.DISPOSITION_SPEC, "s", 5).stream()
            .map(AutocompleteSuggestion::getText)
            .collect(Collectors.toSet()),
        equalTo(
            ImmutableSet.of(
                SUCCESS,
                NEIGHBOR_UNREACHABLE_OR_EXITS_NETWORK.name().toLowerCase(),
                INSUFFICIENT_INFO.name().toLowerCase(),
                DELIVERED_TO_SUBNET.name().toLowerCase(),
                EXITS_NETWORK.name().toLowerCase())));
  }

  @Test
  public void testFilterAutocomplete() throws IOException {
    String network = "network";
    String snapshot = "snapshot";

    String suggestion = "someFilter";
    String notSuggestion = "blah";

    CompletionMetadata completionMetadata =
        CompletionMetadata.builder()
            .setFilterNames(ImmutableSet.of(suggestion, notSuggestion))
            .build();

    assertThat(
        AutoCompleteUtils.autoComplete(
                network, snapshot, Type.FILTER, "fil", 5, completionMetadata, null, null)
            .stream()
            .map(AutocompleteSuggestion::getText)
            .collect(Collectors.toSet()),
        equalTo(ImmutableSet.of(suggestion)));
  }

  @Test
  public void testFlowStateAutocomplete() throws IOException {
    assertThat(
        AutoCompleteUtils.autoComplete(Type.FLOW_STATE, "e", 5).stream()
            .map(AutocompleteSuggestion::getText)
            .collect(Collectors.toSet()),
        equalTo(ImmutableSet.of(ESTABLISHED.toString(), RELATED.toString(), NEW.toString())));
  }

  @Test
  public void testInterfaceAutocomplete() throws IOException {
    String network = "network";
    String snapshot = "snapshot";

    NodeInterfacePair suggested = new NodeInterfacePair("hostname", "interface");
    NodeInterfacePair notSuggested = new NodeInterfacePair("blah", "blahhh");

    CompletionMetadata completionMetadata =
        CompletionMetadata.builder()
            .setInterfaces(ImmutableSet.of(suggested, notSuggested))
            .build();

    assertThat(
        AutoCompleteUtils.autoComplete(
                network, snapshot, Type.INTERFACE, "int", 5, completionMetadata, null, null)
            .stream()
            .map(AutocompleteSuggestion::getText)
            .collect(Collectors.toSet()),
        equalTo(ImmutableSet.of(suggested.toString())));
  }

  @Test
  public void testInterfacePropertySpecAutocomplete() throws IOException {
    assertThat(
        AutoCompleteUtils.autoComplete(Type.INTERFACE_PROPERTY_SPEC, "vlan", 5).stream()
            .map(AutocompleteSuggestion::getText)
            .collect(Collectors.toSet()),
        equalTo(
            ImmutableSet.of(
                ACCESS_VLAN, ALLOWED_VLANS, AUTO_STATE_VLAN, ENCAPSULATION_VLAN, NATIVE_VLAN)));
  }

  @Test
  public void testIpAutocomplete() throws IOException {
    String network = "network";
    String snapshot = "snapshot";

    CompletionMetadata completionMetadata =
        CompletionMetadata.builder()
            .setIps(ImmutableSet.of("1.2.3.4", "1.3.2.4", "1.23.4.5"))
            .build();

    assertThat(
        AutoCompleteUtils.autoComplete(
                network, snapshot, Type.IP, "1.2", 5, completionMetadata, null, null)
            .stream()
            .map(AutocompleteSuggestion::getText)
            .collect(Collectors.toSet()),
        equalTo(ImmutableSet.of("1.2.3.4", "1.23.4.5")));
  }

  @Test
  public void testIpProtocolSpecAutocomplete() throws IOException {
    assertThat(
        AutoCompleteUtils.autoComplete(Type.IP_PROTOCOL_SPEC, "OSP", 5).stream()
            .map(AutocompleteSuggestion::getText)
            .collect(Collectors.toSet()),
        equalTo(ImmutableSet.of("OSPF")));
  }

  @Test
  public void testIpsecSessionStatusAutocomplete() throws IOException {
    assertThat(
        AutoCompleteUtils.autoComplete(Type.IPSEC_SESSION_STATUS, "phase", 5).stream()
            .map(AutocompleteSuggestion::getText)
            .collect(Collectors.toSet()),
        equalTo(
            ImmutableSet.of(
                IKE_PHASE1_FAILED.toString(),
                IKE_PHASE1_KEY_MISMATCH.toString(),
                IPSEC_PHASE2_FAILED.toString())));
  }

  @Test
  public void testNamedStructureSpecAutocomplete() throws IOException {
    assertThat(
        AutoCompleteUtils.autoComplete(Type.NAMED_STRUCTURE_SPEC, "access", 5).stream()
            .map(AutocompleteSuggestion::getText)
            .collect(Collectors.toSet()),
        equalTo(ImmutableSet.of(AS_PATH_ACCESS_LIST, IP_ACCESS_LIST, IP_6_ACCESS_LIST)));
  }

  @Test
  public void testNodePropertySpecAutocomplete() throws IOException {
    assertThat(
        AutoCompleteUtils.autoComplete(Type.NODE_PROPERTY_SPEC, "dns", 5).stream()
            .map(AutocompleteSuggestion::getText)
            .collect(Collectors.toSet()),
        equalTo(ImmutableSet.of(DNS_SERVERS, DNS_SOURCE_INTERFACE)));
  }

  @Test
  public void testNodeRoleDimensionAutocomplete() throws IOException {
    String network = "network";

    NodeRoleDimension suggested = NodeRoleDimension.builder().setName("someDimension").build();
    NodeRoleDimension notSuggested = NodeRoleDimension.builder().setName("blah").build();
    NodeRolesData nodeRolesData =
        NodeRolesData.builder()
            .setRoleDimensions(ImmutableSortedSet.of(suggested, notSuggested))
            .build();

    assertThat(
        AutoCompleteUtils.autoComplete(
                network, "snapshot", Type.NODE_ROLE_DIMENSION, "dim", 5, null, nodeRolesData, null)
            .stream()
            .map(AutocompleteSuggestion::getText)
            .collect(Collectors.toSet()),
        equalTo(ImmutableSet.of(suggested.getName())));
  }

  @Test
  public void testNodeSpecAutocomplete() throws IOException {
    String network = "network";
    String snapshot = "snapshot";

    CompletionMetadata completionMetadata =
        CompletionMetadata.builder().setNodes(ImmutableSet.of("a", "b")).build();

    NodeRolesData nodeRolesData = NodeRolesData.builder().build();

    assertThat(
        AutoCompleteUtils.autoComplete(
                network, snapshot, Type.NODE_SPEC, "a", 5, completionMetadata, nodeRolesData, null)
            .stream()
            .map(AutocompleteSuggestion::getText)
            .collect(Collectors.toSet()),
        equalTo(ImmutableSet.of("a", "&", ",", "\\")));
  }

  @Test
  public void testOspfPropertySpecAutocomplete() throws IOException {
    assertThat(
        AutoCompleteUtils.autoComplete(Type.OSPF_PROPERTY_SPEC, "area", 5).stream()
            .map(AutocompleteSuggestion::getText)
            .collect(Collectors.toSet()),
        equalTo(ImmutableSet.of(AREA_BORDER_ROUTER, AREAS)));
  }

  @Test
  public void testPrefixAutocomplete() throws IOException {
    String network = "network";
    String snapshot = "snapshot";

    CompletionMetadata completionMetadata =
        CompletionMetadata.builder()
            .setPrefixes(ImmutableSet.of("1.2.3.4/24", "1.3.2.4/30"))
            .build();

    assertThat(
        AutoCompleteUtils.autoComplete(
                network, snapshot, Type.PREFIX, "1.2", 5, completionMetadata, null, null)
            .stream()
            .map(AutocompleteSuggestion::getText)
            .collect(Collectors.toSet()),
        equalTo(ImmutableSet.of("1.2.3.4/24")));
  }

  @Test
  public void testProtocolAutocomplete() throws IOException {
    assertThat(
        AutoCompleteUtils.autoComplete(Type.PROTOCOL, "h", 5).stream()
            .map(AutocompleteSuggestion::getText)
            .collect(Collectors.toSet()),
        equalTo(ImmutableSet.of(HTTP.toString(), HTTPS.toString(), SSH.toString())));
  }

  @Test
  public void testRoutingPolicyNameAutocomplete() throws IOException {
    String network = "network";
    String snapshot = "snapshot";

    String suggestion = "somePol";
    String notSuggestion = "blah";

    CompletionMetadata completionMetadata =
        CompletionMetadata.builder()
            .setRoutingPolicyNames(ImmutableSet.of(suggestion, notSuggestion))
            .build();

    assertThat(
        AutoCompleteUtils.autoComplete(
                network,
                snapshot,
                Type.ROUTING_POLICY_NAME,
                "som",
                5,
                completionMetadata,
                null,
                null)
            .stream()
            .map(AutocompleteSuggestion::getText)
            .collect(Collectors.toSet()),
        equalTo(ImmutableSet.of(suggestion)));
  }

  @Test
  public void testRoutingProtocolSpecAutocomplete() throws IOException {
    assertThat(
        AutoCompleteUtils.autoComplete(Type.ROUTING_PROTOCOL_SPEC, "bgp", 5).stream()
            .map(AutocompleteSuggestion::getText)
            .collect(Collectors.toSet()),
        equalTo(ImmutableSet.of("bgp", "ibgp", "ebgp")));
  }

  @Test
  public void testStringAutocomplete() {
    Set<String> strings = ImmutableSet.of("abcd", "degf");
    assertThat(getSuggestionsTextSet(stringAutoComplete("d", strings)), equalTo(strings));
    assertThat(
        getSuggestionsTextSet(stringAutoComplete("b", strings)), equalTo(ImmutableSet.of("abcd")));
    // full match and case-insensitive
    assertThat(
        getSuggestionsTextSet(stringAutoComplete("aBCd", strings)),
        equalTo(ImmutableSet.of("abcd")));
  }

  @Test
  public void testStringPairAutocomplete() {
    Set<StringPair> stringPairs =
        ImmutableSet.of(new StringPair("ab", "cd"), new StringPair("de", "fg"));
    assertThat(
        getSuggestionsTextSet(stringPairAutoComplete("d", stringPairs)),
        equalTo(ImmutableSet.of("ab,cd", "de,fg")));
    assertThat(
        getSuggestionsTextSet(stringPairAutoComplete("b, c", stringPairs)),
        equalTo(ImmutableSet.of("ab,cd")));
    // full match and case-insensitive
    assertThat(
        getSuggestionsTextSet(stringPairAutoComplete("ab,    CD", stringPairs)),
        equalTo(ImmutableSet.of("ab,cd")));
  }

  @Test
  public void testStructureNameAutocomplete() throws IOException {
    String network = "network";
    String snapshot = "snapshot";

    String suggested = "someStructure";
    String notSuggested = "blah";

    CompletionMetadata completionMetadata =
        CompletionMetadata.builder()
            .setStructureNames(ImmutableSet.of(suggested, notSuggested))
            .build();

    assertThat(
        AutoCompleteUtils.autoComplete(
                network, snapshot, Type.STRUCTURE_NAME, "str", 5, completionMetadata, null, null)
            .stream()
            .map(AutocompleteSuggestion::getText)
            .collect(Collectors.toSet()),
        equalTo(ImmutableSet.of(suggested)));
  }

  @Test
  public void testVrfAutocomplete() throws IOException {
    String network = "network";
    String snapshot = "snapshot";

    String suggested = "someVrf";
    String notSuggested = "blah";

    CompletionMetadata completionMetadata =
        CompletionMetadata.builder().setVrfs(ImmutableSet.of(suggested, notSuggested)).build();

    assertThat(
        AutoCompleteUtils.autoComplete(
                network, snapshot, Type.VRF, "v", 5, completionMetadata, null, null)
            .stream()
            .map(AutocompleteSuggestion::getText)
            .collect(Collectors.toSet()),
        equalTo(ImmutableSet.of(suggested)));
  }

  @Test
  public void testZoneAutocomplete() throws IOException {
    String network = "network";
    String snapshot = "snapshot";

    String suggested = "someZone";
    String notSuggested = "blah";

    CompletionMetadata completionMetadata =
        CompletionMetadata.builder().setZones(ImmutableSet.of(suggested, notSuggested)).build();

    assertThat(
        AutoCompleteUtils.autoComplete(
                network, snapshot, Type.ZONE, "z", 5, completionMetadata, null, null)
            .stream()
            .map(AutocompleteSuggestion::getText)
            .collect(Collectors.toSet()),
        equalTo(ImmutableSet.of(suggested)));
  }

  @Test
  public void testAutocompleteUnsupportedType() throws IOException {
    Type type = Type.ANSWER_ELEMENT;

    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage("Unsupported completion type: " + type);

    AutoCompleteUtils.autoComplete("network", "snapshot", type, "blah", 5, null, null, null);
  }
}
