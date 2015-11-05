package org.batfish.nxtnet;

public interface Column {

   Object getItem(int i, EntityTable entityTable, LBValueType valueType);

   ColumnType getType();

}
