package org.batfish.specifier.parboiled;

import java.util.List;
import org.batfish.common.CompletionMetadata;
import org.batfish.referencelibrary.ReferenceLibrary;
import org.batfish.role.NodeRolesData;

/**
 * Interface implemented by classes that generate messages when some specifier does not match
 * anything.
 *
 * <p>E.g., if a node-regex does not match any node in the network, a message to that effect will be
 * generated.
 *
 * <p>Multiple messages will be generated if multiple sub-parts of a complete combination do not
 * match. E.g., two messages will be generated if both sub-parts of a union do not match.
 */
interface NoMatchMessages {

  /** This function returns the messages */
  List<String> get(
      CompletionMetadata completionMetadata,
      NodeRolesData nodeRolesData,
      ReferenceLibrary referenceLibrary);
}
