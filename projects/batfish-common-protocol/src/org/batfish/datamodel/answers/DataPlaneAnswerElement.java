package org.batfish.datamodel.answers;

import java.io.Serializable;

public interface DataPlaneAnswerElement extends AnswerElement, Serializable {

   String getVersion();

}
