package batfish.representation;

public class PolicyMapSetOriginTypeLine extends PolicyMapSetLine {

   private static final long serialVersionUID = 1L;
   private OriginType _originType;

   public PolicyMapSetOriginTypeLine(OriginType originType) {
      _originType = originType;
   }

   @Override
   public PolicyMapSetType getType() {
      return PolicyMapSetType.ORIGIN_TYPE;
   }

   public OriginType getOriginType() {
      return _originType;
   }

}
