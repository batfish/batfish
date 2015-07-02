package org.batfish.question;

import org.batfish.representation.Configuration;
import org.batfish.representation.Interface;

public interface InterfaceSelector {

   public boolean select(Configuration node, Interface iface);

}
