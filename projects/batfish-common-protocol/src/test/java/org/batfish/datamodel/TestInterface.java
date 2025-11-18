package org.batfish.datamodel;

/** Test helper for building {@link Interface} instances with sensible defaults. */
public final class TestInterface {

  /**
   * Returns a new {@link Interface.Builder} with default type of {@link InterfaceType#PHYSICAL}.
   * This is useful for tests that don't care about the specific interface type.
   */
  public static Interface.Builder builder() {
    return Interface.builder().setType(InterfaceType.PHYSICAL);
  }

  private TestInterface() {}
}
