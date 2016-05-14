package org.batfish.datamodel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class PolicyMap implements Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   /**
    * Clauses in this list are checked in order against a candidate route until
    * one matches. If the matching clause is a permit clause, the route is
    * permitted, and modified according to the clause's transformation policy.
    * If the matching clause is a deny clause, or if there is no matching
    * clause, then the policy denies the route.
    */
   private final List<PolicyMapClause> _clauses;

   /**
    * The configuration-local name identifying this policy.
    */
   private String _mapName;

   private transient PrefixSpace _prefixSpace;

   /**
    * Constructs a PolicyMap with the given name for {@link #_mapName} and list
    * of clauses for {@link #_clauses}.
    *
    * @param name
    */
   public PolicyMap(String name) {
      _mapName = name;
      _clauses = new ArrayList<PolicyMapClause>();
   }

   /**
    * @return {@link #_clauses}
    */
   public List<PolicyMapClause> getClauses() {
      return _clauses;
   }

   /**
    * @return {@link #_mapName}
    */
   public String getMapName() {
      return _mapName;
   }

   @JsonIgnore
   public PrefixSpace getPrefixSpace() {
      initPrefixSpace();
      return _prefixSpace;
   }

   private void initPrefixSpace() {
      if (_prefixSpace == null) {
         _prefixSpace = new PrefixSpace();
         Set<PrefixRange> prefixRanges = new HashSet<PrefixRange>();
         for (PolicyMapClause clause : _clauses) {
            if (clause.getAction() == PolicyMapAction.PERMIT) {
               boolean foundMatchRouteFilter = false;
               for (PolicyMapMatchLine matchLine : clause.getMatchLines()) {
                  if (matchLine.getType() == PolicyMapMatchType.ROUTE_FILTER_LIST) {
                     foundMatchRouteFilter = true;
                     PolicyMapMatchRouteFilterListLine matchRouteFilterLine = (PolicyMapMatchRouteFilterListLine) matchLine;
                     for (RouteFilterList list : matchRouteFilterLine
                           .getLists()) {
                        for (RouteFilterLine line : list.getLines()) {
                           Prefix prefix = line.getPrefix();
                           SubRange lengthRange = line.getLengthRange();
                           if (line.getAction() == LineAction.ACCEPT) {
                              prefixRanges.add(new PrefixRange(prefix,
                                    lengthRange));
                           }
                        }
                     }
                  }
               }
               if (!foundMatchRouteFilter) {
                  prefixRanges.add(new PrefixRange(Prefix.ZERO, new SubRange(0,
                        32)));
               }
            }
         }
         for (PrefixRange prefixRange : prefixRanges) {
            _prefixSpace.addPrefixRange(prefixRange);
         }
      }
   }

}
