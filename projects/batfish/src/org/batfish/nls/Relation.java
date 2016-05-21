package org.batfish.nls;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.LBValueType;
import org.batfish.datamodel.collections.LBValueTypeList;

import com.google.common.collect.ImmutableList;

public class Relation {

   public static class Builder {

      private String _name;

      public Builder(String name) {
         _name = name;
      }

      public Relation build(PredicateInfo predicateInfo, String nlsText) {
         LBValueTypeList columnValueTypes = predicateInfo
               .getPredicateValueTypes(_name);
         if (columnValueTypes == null) {
            throw new BatfishException("Missing schema for predicate: \""
                  + _name + "\"");
         }
         int numColumns = columnValueTypes.size();
         String[] lines = nlsText.split("\n|\r\n");
         int numLines;
         if (nlsText.length() == 0) {
            numLines = 0;
         }
         else {
            numLines = lines.length;
         }
         String delimiter = Pattern.quote("|");
         Object[][] table = new Object[numLines][numColumns];
         Column[] columns = new Column[numColumns];
         for (int i = 0; i < numLines; i++) {
            String line = lines[i];
            String[] parts = line.split(delimiter);
            int numFields = parts.length;
            if (numFields != numColumns) {
               throw new BatfishException("nls relation: \"" + _name
                     + "\", line " + (i + 1) + ": expected " + numColumns
                     + " fields but got " + numFields);
            }
            for (int j = 0; j < numFields; j++) {
               String part = parts[j];
               LBValueType valueType = columnValueTypes.get(j);
               ColumnType columnType = getColumnType(valueType);
               Object value;
               switch (columnType) {
               case DOUBLE:
                  value = Double.parseDouble(part);
                  break;

               case LONG:
                  value = Long.parseLong(part);
                  break;

               case STRING:
                  value = part;
                  break;

               default:
                  throw new BatfishException("invalid column type");
               }
               table[i][j] = value;
            }
         }
         for (int j = 0; j < numColumns; j++) {
            LBValueType valueType = columnValueTypes.get(j);
            ColumnType columnType = getColumnType(valueType);
            switch (columnType) {
            case DOUBLE:
               Double[] dArray = new Double[numLines];
               for (int i = 0; i < numLines; i++) {
                  dArray[i] = (Double) table[i][j];
               }
               columns[j] = new DoubleColumn(dArray);
               break;

            case LONG:
               Long[] lArray = new Long[numLines];
               for (int i = 0; i < numLines; i++) {
                  lArray[i] = (Long) table[i][j];
               }
               columns[j] = new LongColumn(lArray);
               break;

            case STRING:
               String[] sArray = new String[numLines];
               for (int i = 0; i < numLines; i++) {
                  sArray[i] = (String) table[i][j];
               }
               columns[j] = new StringColumn(sArray);
               break;

            default:
               throw new BatfishException("invalid column type");

            }
         }
         List<Column> columnList = ImmutableList.<Column> builder()
               .addAll(Arrays.asList(columns)).build();
         return new Relation(_name, columnList, numLines);
      }
   }

   private static ColumnType getColumnType(LBValueType valueType) {
      switch (valueType) {
      case ENTITY_INDEX_BGP_ADVERTISEMENT:
      case ENTITY_INDEX_FLOW:
      case ENTITY_INDEX_INT:
      case ENTITY_INDEX_NETWORK:
      case ENTITY_INDEX_ROUTE:
      case ENTITY_REF_AUTONOMOUS_SYSTEM:
      case ENTITY_REF_INT:
      case ENTITY_REF_IP:
      case INT:
         return ColumnType.LONG;

      case ENTITY_REF_ADVERTISEMENT_TYPE:
      case ENTITY_REF_AS_PATH:
      case ENTITY_REF_FLOW_TAG:
      case ENTITY_REF_INTERFACE:
      case ENTITY_REF_NODE:
      case ENTITY_REF_ORIGIN_TYPE:
      case ENTITY_REF_POLICY_MAP:
      case ENTITY_REF_ROUTING_PROTOCOL:
      case ENTITY_REF_STRING:
      case STRING:
         return ColumnType.STRING;

      case FLOAT:
         return ColumnType.DOUBLE;

      default:
         throw new BatfishException("invalid value type");

      }
   }

   private List<Column> _columns;

   private String _name;

   private int _numRows;

   private Relation(String name, List<Column> columns, int numRows) {
      _name = name;
      _columns = columns;
      _numRows = numRows;
   }

   public List<Column> getColumns() {
      return _columns;
   }

   public String getName() {
      return _name;
   }

   public int getNumRows() {
      return _numRows;
   }

}
