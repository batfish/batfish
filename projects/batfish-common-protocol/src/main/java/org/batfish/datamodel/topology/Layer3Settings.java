package org.batfish.datamodel.topology;

import java.io.Serializable;

/** Configuration for the layer-3 aspect of an {@link org.batfish.datamodel.Interface}. */
public interface Layer3Settings extends Serializable {

  <T> T accept(Layer3SettingsVisitor<T> visitor);

  <T, U> T accept(Layer3SettingsArgVisitor<T, U> visitor, U arg);
}
