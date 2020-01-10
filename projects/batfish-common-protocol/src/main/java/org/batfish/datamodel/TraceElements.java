package org.batfish.datamodel;

/** {@link TraceElement} utilities. */
public final class TraceElements {
  private TraceElements() {}

  /**
   * Temporary transitional helper method. Used to preserve behavior while transitioning from
   * IpSpaceMetadata to TraceElement.
   */
  public static TraceElement namedStructure(String name, String type) {
    return TraceElement.builder().add(String.format("'%s' named '%s'", type, name)).build();
  }
}
