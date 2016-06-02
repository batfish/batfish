package org.batfish.datamodel;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.batfish.common.util.CommonUtil;

public class CommunityListLine implements Serializable {

   private static final long serialVersionUID = 1L;

   private LineAction _action;
   private String _regex;

   public CommunityListLine(LineAction action, String regex) {
      _action = action;
      _regex = regex;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      CommunityListLine other = (CommunityListLine) obj;
      if (other._action != _action) {
         return false;
      }
      else {
         return other._regex.equals(_regex);
      }
   }

   public LineAction getAction() {
      return _action;
   }

   public Set<Long> getExactMatchingCommunities(Set<Long> allCommunities) {
      Pattern p = Pattern.compile(_regex);
      Set<Long> matchingCommunitites = new LinkedHashSet<Long>();
      for (long candidateCommunity : allCommunities) {
         String candidateCommunityStr = CommonUtil
               .longToCommunity(candidateCommunity);
         Matcher matcher = p.matcher(candidateCommunityStr);
         if (matcher.matches()) {
            matchingCommunitites.add(candidateCommunity);
         }
      }
      return matchingCommunitites;
   }

   public Set<Long> getMatchingCommunities(Set<Long> allCommunities) {
      Pattern p = Pattern.compile(_regex);
      Set<Long> matchingCommunitites = new LinkedHashSet<Long>();
      for (long candidateCommunity : allCommunities) {
         String candidateCommunityStr = CommonUtil
               .longToCommunity(candidateCommunity);
         Matcher matcher = p.matcher(candidateCommunityStr);
         if (matcher.find()) {
            matchingCommunitites.add(candidateCommunity);
         }
      }
      return matchingCommunitites;
   }

   public String getRegex() {
      return _regex;
   }

}
