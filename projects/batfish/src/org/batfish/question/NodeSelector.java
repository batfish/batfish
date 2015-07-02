package org.batfish.question;

import org.batfish.representation.Configuration;

public interface NodeSelector {

   public boolean select(Configuration node);

}
