package org.batfish.specifier.parboiled;

import static org.batfish.specifier.parboiled.Anchor.Type.INTERFACE_NAME;
import static org.batfish.specifier.parboiled.Anchor.Type.NODE_AND_INTERFACE;
import static org.batfish.specifier.parboiled.Anchor.Type.NODE_AND_INTERFACE_TAIL;
import static org.batfish.specifier.parboiled.Anchor.Type.NODE_NAME;
import static org.batfish.specifier.parboiled.Anchor.Type.UNKNOWN;
import static org.batfish.specifier.parboiled.ParboiledAutoComplete.updateSuggestions;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertFalse;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import org.batfish.common.CompletionMetadata;
import org.batfish.common.autocomplete.NodeCompletionMetadata;
import org.batfish.datamodel.answers.AutocompleteSuggestion;
import org.batfish.datamodel.answers.AutocompleteSuggestion.SuggestionType;
import org.batfish.referencelibrary.ReferenceLibrary;
import org.batfish.role.NodeRolesData;
import org.batfish.specifier.Grammar;
import org.batfish.specifier.parboiled.Anchor.Type;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests for {@link ParboiledAutoComplete} */
public class ParboiledAutoCompleteTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  // These helpers build a ParboiledAutoComplete over the node specifier grammar. They are used only
  // by the helper-method tests below; the end-to-end run behavior is covered by the per-grammar
  // Parser*Test autocompletion tests.
  private static ParboiledAutoComplete getTestPAC(String query) {
    return getTestPAC(query, CompletionMetadata.builder().build());
  }

  private static ParboiledAutoComplete getTestPAC(
      String query, CompletionMetadata completionMetadata) {
    return new ParboiledAutoComplete(
        Grammar.NODE_SPECIFIER,
        "network",
        "snapshot",
        query,
        Integer.MAX_VALUE,
        completionMetadata,
        NodeRolesData.builder().build(),
        new ReferenceLibrary(null));
  }

  @Test
  public void testAutoCompletePotentialMatchStringLiteral() {
    PathElement anchor = new PathElement(Type.STRING_LITERAL, "\"pfxcomp\"", 0, 0);
    PotentialMatch pm = new PotentialMatch(anchor, "pfx", ImmutableList.of(anchor));
    assertThat(
        getTestPAC("pfx").autoCompletePotentialMatch(pm),
        containsInAnyOrder(new ParboiledAutoCompleteSuggestion("pfxcomp", 0, UNKNOWN)));
  }

  /** The suggestion should have the case in the grammar token independent of user input */
  @Test
  public void testAutoCompletePotentialMatchStringLiteralCasePreserve() {
    PathElement anchor = new PathElement(Type.STRING_LITERAL, "\"pfxcomp\"", 0, 0);
    PotentialMatch pm = new PotentialMatch(anchor, "PfX", ImmutableList.of(anchor));
    assertThat(
        getTestPAC("PfX").autoCompletePotentialMatch(pm),
        containsInAnyOrder(new ParboiledAutoCompleteSuggestion("pfxcomp", 0, UNKNOWN)));
  }

  @Test
  public void testAutoCompletePotentialMatchSkipLabel() {
    PotentialMatch pm =
        new PotentialMatch(
            new PathElement(Type.IP_ADDRESS_MASK, "label", 0, 0), "pfx", ImmutableList.of());
    assertThat(getTestPAC(null).autoCompletePotentialMatch(pm), equalTo(ImmutableSet.of()));
  }

  @Test
  public void testFindFirstMatchingPathElement() {
    PathElement anchor = new PathElement(Type.INTERFACE_NAME, null, 1, 42);
    PathElement tailStart = new PathElement(Type.NODE_AND_INTERFACE_TAIL, null, 0, 42);
    PathElement nodeStart = new PathElement(Type.NODE_AND_INTERFACE, null, 0, 42);
    PathElement iname2 = new PathElement(Type.INTERFACE_NAME, null, 1, 42);
    PathElement tail2 = new PathElement(Type.NODE_AND_INTERFACE_TAIL, null, 0, 42);
    PotentialMatch pm =
        new PotentialMatch(
            anchor, "", ImmutableList.of(tail2, iname2, nodeStart, tailStart, anchor));

    // should return the tail close to the anchor
    assertThat(
        ParboiledAutoComplete.findFirstMatchingPathElement(pm, 2, NODE_AND_INTERFACE_TAIL),
        equalTo(Optional.of(tailStart)));

    // non-existent anchor type
    assertThat(
        ParboiledAutoComplete.findFirstMatchingPathElement(pm, 2, NODE_NAME),
        equalTo(Optional.empty()));

    // should return the other interface_name
    assertThat(
        ParboiledAutoComplete.findFirstMatchingPathElement(pm, 2, INTERFACE_NAME),
        equalTo(Optional.of(iname2)));
  }

  /** Throw an exception if anchor is not present in the path */
  @Test
  public void testFindPrecedingInputMissingAnchor() {
    _thrown.expect(IllegalArgumentException.class);
    ParboiledAutoComplete.findPrecedingInput(
        new PotentialMatch(
            new PathElement(Type.INTERFACE_NAME, null, 0, 0), "", ImmutableList.of()),
        "@specifier(g1,",
        NODE_AND_INTERFACE,
        NODE_AND_INTERFACE_TAIL);
  }

  /** Return empty optional if head anchor type is not present */
  @Test
  public void testFindPrecedingInpuMissingHead() {
    PathElement anchor = new PathElement(Type.INTERFACE_NAME, null, 1, 42);
    PotentialMatch pm = new PotentialMatch(anchor, "", ImmutableList.of(anchor));
    assertFalse(
        ParboiledAutoComplete.findPrecedingInput(
                pm, "dummy", NODE_AND_INTERFACE, NODE_AND_INTERFACE_TAIL)
            .isPresent());
  }

  /** Throw an exception if tail anchor type is missing */
  @Test
  public void testFindPrecedingInputMissingTail() {
    PathElement anchor = new PathElement(Type.INTERFACE_NAME, null, 1, 42);
    PathElement nodeStart = new PathElement(Type.NODE_AND_INTERFACE, null, 0, 0);
    PotentialMatch pm = new PotentialMatch(anchor, "", ImmutableList.of(nodeStart, anchor));

    _thrown.expect(IllegalArgumentException.class);
    ParboiledAutoComplete.findPrecedingInput(
        pm, "dummy", NODE_AND_INTERFACE, NODE_AND_INTERFACE_TAIL);
  }

  /** Get the proper input if both head and tail are present */
  @Test
  public void testFindPrecedingInput() {
    String query = "n1a[";
    PathElement anchor = new PathElement(Type.INTERFACE_NAME, null, 1, query.length());
    PathElement tailStart = new PathElement(Type.NODE_AND_INTERFACE_TAIL, null, 0, 3);
    PathElement nodeStart = new PathElement(Type.NODE_AND_INTERFACE, null, 0, 0);
    PotentialMatch pm =
        new PotentialMatch(anchor, "", ImmutableList.of(nodeStart, tailStart, anchor));

    assertThat(
        ParboiledAutoComplete.findPrecedingInput(
            pm, query, NODE_AND_INTERFACE, NODE_AND_INTERFACE_TAIL),
        Matchers.equalTo(Optional.of("n1a")));
  }

  @Test
  public void testCompleteNodeNameWithHumanNameHint() {
    String query = "node";
    // these should be the same as empty input ones
    String node1 = "node1";
    String node1HumanName = "humanName";
    String node2 = "node2";
    CompletionMetadata metadata =
        CompletionMetadata.builder()
            .setNodes(
                ImmutableMap.of(
                    node1,
                    new NodeCompletionMetadata(node1HumanName),
                    node2,
                    new NodeCompletionMetadata(null)))
            .build();
    assertThat(
        getTestPAC(query, metadata).run(),
        allOf(
            hasItem(
                new ParboiledAutoCompleteSuggestion(
                    node1, NODE_NAME.getHint(), 0, NODE_NAME, node1HumanName)),
            hasItem(
                new ParboiledAutoCompleteSuggestion(node2, NODE_NAME.getHint(), 0, NODE_NAME))));
  }

  @Test
  public void testCompleteHumanName() {
    String query = "human";
    // these should be the same as empty input ones
    String node1 = "node1";
    String node1HumanName = "humanName1";
    String node2 = "node2";
    CompletionMetadata metadata =
        CompletionMetadata.builder()
            .setNodes(
                ImmutableMap.of(
                    node1,
                    new NodeCompletionMetadata(node1HumanName),
                    node2,
                    new NodeCompletionMetadata(null)))
            .build();
    assertThat(
        getTestPAC(query, metadata).run(),
        allOf(
            hasItem(
                new ParboiledAutoCompleteSuggestion(node1, null, 0, NODE_NAME, node1HumanName))));
  }

  /**
   * Tests that relevants fields are preserved when we covert {@link AutocompleteSuggestion} to
   * {@link ParboiledAutoCompleteSuggestion}.
   */
  @Test
  public void testUpdateSuggestions() {
    AutocompleteSuggestion autocompleteSuggestion =
        new AutocompleteSuggestion("text", SuggestionType.UNKNOWN, true, "desc", 42, 3, "hint");
    assertThat(
        updateSuggestions(ImmutableList.of(autocompleteSuggestion), false, NODE_NAME, 0),
        equalTo(
            ImmutableSet.of(
                new ParboiledAutoCompleteSuggestion("text", "hint", 0, NODE_NAME, "desc"))));
  }
}
