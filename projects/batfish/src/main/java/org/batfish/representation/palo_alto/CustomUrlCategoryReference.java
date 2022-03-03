package org.batfish.representation.palo_alto;

import javax.annotation.Nonnull;

/** Represents a reference to a custom-url-category. */
public final class CustomUrlCategoryReference
    implements Comparable<CustomUrlCategoryReference>, Reference {

  @Override
  public <T, U> T accept(ReferenceVisitor<T, U> visitor, U arg) {
    return visitor.visitCustomUrlCategoryReference(this, arg);
  }

  public CustomUrlCategoryReference(String name) {
    _name = name;
  }

  @Override
  public int compareTo(CustomUrlCategoryReference o) {
    return _name.compareTo(o._name);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof CustomUrlCategoryReference)) {
      return false;
    }
    return _name.equals(((CustomUrlCategoryReference) o).getName());
  }

  @Override
  public int hashCode() {
    return _name.hashCode();
  }

  /** Return the name of the referenced Category */
  @Override
  @Nonnull
  public String getName() {
    return _name;
  }

  @Nonnull private final String _name;
}
