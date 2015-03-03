package org.batfish.coordinator;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.queue.CloudQueue;
import com.microsoft.azure.storage.queue.CloudQueueClient;
import com.microsoft.azure.storage.queue.CloudQueueMessage;

public class AzureQueue implements WorkQueue {
   
   private CloudQueue _queue;

   public AzureQueue(String queueName, String storageConnectionString) {
      try {
         // Retrieve storage account from connection-string.
         CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);

         // Create the queue client.
         CloudQueueClient queueClient = storageAccount.createCloudQueueClient();

         // Retrieve a reference to a queue.
         _queue = queueClient.getQueueReference(queueName);

         // Create the queue if it doesn't already exist.
         _queue.createIfNotExists();
      }
      catch (Exception e) {
         // Output the stack trace.
         e.printStackTrace();
      }
   }
   
   public long getLength() {
      try {
         // Download the approximate message count from the server.
         _queue.downloadAttributes();

         // Retrieve the newly cached approximate message count.
         return _queue.getApproximateMessageCount();
      }
      catch (Exception e) {
         e.printStackTrace();
         return -1;
      }
   }
}