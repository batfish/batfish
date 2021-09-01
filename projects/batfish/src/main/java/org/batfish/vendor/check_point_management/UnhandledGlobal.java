package org.batfish.vendor.check_point_management;

/** An object of type {@link Global} whose name is unrecognized or otherwise unhandled. */
public final class UnhandledGlobal extends Global {
  @Override
  public <T> T accept(NatTranslatedServiceVisitor<T> visitor) {
    return visitor.visitUnhandledGlobal(this);
  }

  @Override
  public <T> T accept(NatTranslatedSrcOrDstVisitor<T> visitor) {
    return visitor.visitUnhandledGlobal(this);
  }

  UnhandledGlobal(String name, Uid uid) {
    super(name, uid);
  }
}
