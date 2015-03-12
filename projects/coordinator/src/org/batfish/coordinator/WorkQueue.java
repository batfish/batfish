package org.batfish.coordinator;

public interface WorkQueue {   
   
   public enum Type {Azure, Memory}
   
   long getLength();   
}