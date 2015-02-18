package org.batfish.representation;

import java.io.Serializable;
import java.util.List;

/**
 * Represents a named access-list whose matching criteria is restricted to
 * regexes on community attributes sent with a bgp advertisement
 */
public class CommunityList implements Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   /**
    * The list of lines that are checked in order against the community
    * attribute(s) of a bgp advertisement
    */
   private List<CommunityListLine> _lines;

   /**
    * The name associated with this CommunityList. Ordinarily this should be
    * extracted verbatim from a vendor-configuration file.
    */
   private String _name;

   /**
    * Constructs a CommunityList with the given name for {@link #_name}, and
    * lines for {@link #_lines}
    *
    * @param name
    * @param lines
    */
   public CommunityList(String name, List<CommunityListLine> lines) {
      _name = name;
      _lines = lines;
   }

   public List<CommunityListLine> getLines() {
      return _lines;
   }

   public String getName() {
      return _name;
   }

}
