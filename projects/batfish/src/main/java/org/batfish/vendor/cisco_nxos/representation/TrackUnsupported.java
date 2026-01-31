package org.batfish.vendor.cisco_nxos.representation;

/**
 * Placeholder tracking configuration for something that is not yet supported in the VS model. Used
 * to populate the VS model to avoid incorrectly rejecting lines for undefined references.
 */
public class TrackUnsupported implements Track {
  public static final TrackUnsupported INSTANCE = new TrackUnsupported();

  private TrackUnsupported() {}
}
