package org.batfish.datamodel.transformation;

/** Used to indicate which Port is transformed by a {@link TransformationStep}. */
public enum PortField {
  SOURCE,
  DESTINATION;

  public PortField opposite() {
    return switch (this) {
      case SOURCE -> DESTINATION;
      case DESTINATION -> SOURCE;
    };
  }
}
