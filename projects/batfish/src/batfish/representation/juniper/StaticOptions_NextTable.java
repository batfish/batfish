package batfish.representation.juniper;

public class StaticOptions_NextTable extends StaticOptions {

   private String _nextTable;

   public StaticOptions_NextTable(String s) {
      _nextTable = s;
   }

   public String getNextTable() {
      return _nextTable;
   }

   @Override
   public StaticOptionsType getType() {
      return StaticOptionsType.NEXT_TABLE;
   }

}
