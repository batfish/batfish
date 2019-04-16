package org.batfish.coordinator.resources;

import javax.annotation.ParametersAreNonnullByDefault;

/** Resource for handling requests about a specific ad-hoc or analysis question's answer */
@ParametersAreNonnullByDefault
public final class AnswerResource {

  private final String _analysis;

  private final String _network;

  private final String _questionName;

  private final String _snapshot;

  public AnswerResource(String network, String snapshot, String analysis, String questionName) {
    _analysis = analysis;
    _network = network;
    _questionName = questionName;
    _snapshot = snapshot;
  }
}
