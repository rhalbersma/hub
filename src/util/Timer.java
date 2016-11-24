
package util;

public class Timer {

   private boolean p_is_running;
   private long p_start;
   private int p_elapsed;

   public Timer() {
      reset();
   }

   public void reset() {
      p_elapsed = 0;
      p_is_running = false;
   }

   public void start() {
      assert !p_is_running;
      p_is_running = true;
      p_start = now();
   }

   public void restart() {
      p_elapsed = 0;
      p_is_running = true;
      p_start = now();
   }

   public void stop() {
      assert p_is_running;
      p_elapsed += time();
      p_is_running = false;
   }

   public int elapsed() {

      int time = p_elapsed;
      if (p_is_running) time += time();

      assert time >= 0;
      return time;
   }

   private int time() {
      int time = (int) (now() - p_start);
      assert time >= 0;
      return time;
   }

   private static long now() {
      return System.currentTimeMillis();
   }
}

