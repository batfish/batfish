package org.batfish.representation.palo_alto;

import javax.annotation.Nonnull;

/** Datamodel class that represents a reference to an application or an application-group. */
public final class ApplicationOrApplicationGroupReference
    implements Comparable<ApplicationOrApplicationGroupReference>, Reference {
  public ApplicationOrApplicationGroupReference(String name) {
    _name = name;
  }

  @Override
  public <T, U> T accept(ReferenceVisitor<T, U> visitor, U arg) {
    return visitor.visitApplicationOrApplicationGroupReference(this, arg);
  }

  @Override
  public int compareTo(ApplicationOrApplicationGroupReference o) {
    return _name.compareTo(o._name);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ApplicationOrApplicationGroupReference)) {
      return false;
    }
    return _name.equals(((ApplicationOrApplicationGroupReference) o).getName());
  }

  @Override
  public int hashCode() {
    return _name.hashCode();
  }

  /** Return the name of the referenced Application or ApplicationGroup */
  @Override
  @Nonnull
  public String getName() {
    return _name;
  }

  @Nonnull private final String _name;
}
