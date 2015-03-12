package org.batfish.coordinator;

public interface WorkQueue {   
   
   public enum Type {azure, memory}
   
   long getLength();   
}