package org.batfish.datamodel.answers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import org.batfish.common.util.SkipForEqualityCheck;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class NamedStructureEquivalenceClass<T> {
   
   // Jackson cannot serialize generics correctly.
   @JsonIgnore
   private final T _namedStructure; 
   
   private final Set<String> _nodes;
   
   public NamedStructureEquivalenceClass(String node, T namedStructure) {
      _namedStructure = namedStructure;
      _nodes = new HashSet<String>();
      _nodes.add(node);
      addNode(node);
   }

   public void addNode(String node) {
      _nodes.add(node);
   }
   
   public boolean CompareStructure(T s)
   {
      return checkEqual(_namedStructure, s);
     // return _namedStructure.equals(s);
   }
  
   public Set<String> getNodes() {
      return _nodes;
   }
   
   private static boolean isWrapperType(Class<?> clazz) {
      return clazz.equals(Boolean.class) ||
         clazz.equals(Integer.class) ||
         clazz.equals(Character.class) ||
         clazz.equals(Byte.class) ||
         clazz.equals(Short.class) ||
         clazz.equals(Double.class) ||
         clazz.equals(Long.class) ||
         clazz.equals(Float.class);
   }

   private static boolean isBatfishClass(Class<?> aClass) {
      String aClassName = aClass.getName();
      if (aClassName.startsWith("org.batfish")) {
         return true;
      }
      else {
         return false;
      }
   }
   private static boolean checkEqual(Object a, Object b)
   {
      Class<? extends Object> aClass = a.getClass();
      Class<? extends Object> bClass = b.getClass();

      if (!aClass.equals(bClass)) {
         return false;
      }

      if (!isBatfishClass(aClass)) {
         return (a.equals(b));
      }

      Field[] fields = aClass.getDeclaredFields();

      for(Field field: fields){
         field.setAccessible(true);
         try {
            Annotation skipForEqualityCheck = field.getAnnotation(SkipForEqualityCheck.class);
            if (skipForEqualityCheck == null) {
               Object aValue = field.get(a);
               Object bValue = field.get(b);
               if (!checkEqual(aValue, bValue)) {
                  return false;
               }
            }
         }
         catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
         catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
     }
     return true;
   }



}
