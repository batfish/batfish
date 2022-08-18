package org.batfish.specifier;

import java.util.List;
import org.batfish.common.CompletionMetadata;
import org.batfish.datamodel.answers.AutocompleteSuggestion;
import org.batfish.referencelibrary.ReferenceLibrary;
import org.batfish.role.NodeRolesData;
import org.batfish.specifier.parboiled.ParboiledAutoComplete;

public final class SpecifierAutoComplete {
  /**
   * The entry point for auto completion. Given the {@code grammar} and {@code query}, this function
   * will produce at most {@code maxSuggestions} suggestions based on other supplied details of the
   * network
   */
  public static List<AutocompleteSuggestion> autoComplete(
      Grammar grammar,
      String network,
      String snapshot,
      String query,
      int maxSuggestions,
      CompletionMetadata completionMetadata,
      NodeRolesData nodeRolesData,
      ReferenceLibrary referenceLibrary) {
    return ParboiledAutoComplete.autoComplete(
        grammar,
        network,
        snapshot,
        query,
        maxSuggestions,
        completionMetadata,
        nodeRolesData,
        referenceLibrary);
  }

  private SpecifierAutoComplete() {} // prevent instantiation of utility class
}
