package org.batfish.datamodel.transformation;

/** Used to indicate which Port is transformed by a {@link TransformationStep}. */
public enum PortField {
  SOURCE,
  DESTINATION;

  public PortField opposite() {
    switch (this) {
      case SOURCE:
        return DESTINATION;
      case DESTINATION:
        return SOURCE;
      default:
        throw new IllegalArgumentException("Unexpected PortField " + name());
    }
  }
}
