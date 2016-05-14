package org.batfish.representation.vyos;

import java.io.Serializable;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.PolicyMap;
import org.batfish.datamodel.PolicyMapClause;
import org.batfish.main.Warnings;

public interface RouteMapMatch extends Serializable {

   void applyTo(Configuration c, PolicyMap policyMap, PolicyMapClause clause,
         Warnings w);

}
