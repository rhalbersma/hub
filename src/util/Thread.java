
package util;

public class Thread {

   public static void launch(Runnable run) {
      new java.lang.Thread(run).start();
   }

   public static void sleep(int time) {
      try {
         java.lang.Thread.sleep(time);
      } catch(InterruptedException ex) {
         java.lang.Thread.currentThread().interrupt();
      }
   }
}

