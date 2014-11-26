package batfish.representation.juniper;

public class Martian {

   private FilterMatch _filterMatch;
   private String _ipWithMask;
   private boolean _isAllowed;
   private boolean _isIpv6;

   public Martian() {
      _ipWithMask = "";
      _isAllowed = false;
      _isIpv6 = false;
   }

   public FilterMatch getFilterMatch() {
      return _filterMatch;
   }

   public String getIpWithMask() {
      return _ipWithMask;
   }

   public boolean getIsAllowed() {
      return _isAllowed;
   }

   public boolean getIsIpv6() {
      return _isIpv6;
   }

   public void setFilterMatch(FilterMatch filterMatch) {
      _filterMatch = filterMatch;
   }

   public void setIpWithMask(String ipWithMask) {
      _ipWithMask = ipWithMask;
   }

   public void setIsAllowed(boolean isAllowed) {
      _isAllowed = isAllowed;
   }

   public void setIsIpv6(boolean isIpv6) {
      _isIpv6 = isIpv6;
   }

}
