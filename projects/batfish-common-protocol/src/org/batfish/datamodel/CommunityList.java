package org.batfish.datamodel;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.batfish.common.util.CommonUtil;
import org.batfish.common.util.ComparableStructure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a named access-list whose matching criteria is restricted to
 * regexes on community attributes sent with a bgp advertisement
 */
public class CommunityList extends ComparableStructure<String> {

   private static final String LINES_VAR = "lines";

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private transient Set<Long> _denyCache;

   private boolean _invertMatch;

   /**
    * The list of lines that are checked in order against the community
    * attribute(s) of a bgp advertisement
    */
   private final List<CommunityListLine> _lines;

   private transient Set<Long> _permitCache;

   /**
    * Constructs a CommunityList with the given name for {@link #_name}, and
    * lines for {@link #_lines}
    *
    * @param name
    * @param lines
    */
   @JsonCreator
   public CommunityList(@JsonProperty(NAME_VAR) String name,
         @JsonProperty(LINES_VAR) List<CommunityListLine> lines) {
      super(name);
      _lines = lines;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      CommunityList other = (CommunityList) obj;
      return other._lines.equals(_lines);
   }

   public boolean getInvertMatch() {
      return _invertMatch;
   }

   @JsonProperty(LINES_VAR)
   public List<CommunityListLine> getLines() {
      return _lines;
   }

   @Override
   @JsonProperty(NAME_VAR)
   public String getName() {
      return _key;
   }

   private boolean newPermits(long community) {
      boolean accept = false;
      for (CommunityListLine line : _lines) {
         Pattern p = Pattern.compile(line.getRegex());
         String communityStr = CommonUtil.longToCommunity(community);
         Matcher matcher = p.matcher(communityStr);
         if (matcher.find() ^ _invertMatch) {
            accept = line.getAction() == LineAction.ACCEPT;
            break;
         }
      }
      if (accept) {
         _permitCache.add(community);
      }
      else {
         _denyCache.add(community);
      }
      return accept;
   }

   public boolean permits(long community) {
      if (_permitCache == null) {
         _denyCache = Collections.newSetFromMap(new ConcurrentHashMap<>());
         _permitCache = Collections.newSetFromMap(new ConcurrentHashMap<>());
      }
      else if (_permitCache.contains(community)) {
         return true;
      }
      else if (_denyCache.contains(community)) {
         return false;
      }
      return newPermits(community);
   }

   public void setInvertMatch(boolean invertMatch) {
      _invertMatch = invertMatch;
   }

}
