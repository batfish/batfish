package org.batfish.representation.vyos;

import java.io.Serializable;

import org.batfish.main.Warnings;
import org.batfish.representation.Configuration;
import org.batfish.representation.PolicyMap;
import org.batfish.representation.PolicyMapClause;

public interface RouteMapMatch extends Serializable {

   void applyTo(Configuration c, PolicyMap policyMap, PolicyMapClause clause,
         Warnings w);

}
