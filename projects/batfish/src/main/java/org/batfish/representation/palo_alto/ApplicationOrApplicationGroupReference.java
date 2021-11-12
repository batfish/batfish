package org.batfish.representation.palo_alto;

import javax.annotation.Nonnull;

/** Datamodel class that represents a reference to an applicaiton or an applicaiton-group. */
public class ApplicationOrApplicationGroupReference
    implements Comparable<ApplicationOrApplicationGroupReference>, Reference {
  public ApplicationOrApplicationGroupReference(String name) {
    _name = name;
  }

  @Override
  public <T, U> T accept(ReferenceVisitor<T, U> visitor, U arg) {
    return visitor.visitApplicationOrApplicationGroupReference(this, arg);
  }

  @Override
  public int compareTo(@Nonnull ApplicationOrApplicationGroupReference o) {
    return _name.compareTo(o._name);
  }

  /** Return the name of the referenced Application or ApplicationGroup */
  public String getName() {
    return _name;
  }

  private final String _name;
}
