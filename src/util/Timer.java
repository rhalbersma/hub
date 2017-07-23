
package util;

public class Timer {

   private boolean p_is_running;
   private double p_start;
   private double p_elapsed;

   public Timer() {
      reset();
   }

   public void reset() {
      p_elapsed = 0.0;
      p_is_running = false;
   }

   public void start() {
      assert !p_is_running;
      p_is_running = true;
      p_start = now();
   }

   public void restart() {
      p_elapsed = 0.0;
      p_is_running = true;
      p_start = now();
   }

   public void stop() {
      assert p_is_running;
      p_elapsed += time();
      p_is_running = false;
   }

   public double elapsed() {

      double time = p_elapsed;
      if (p_is_running) time += time();

      return time;
   }

   private double time() {
      return now() - p_start;
   }

   private static double now() {
      return (double) System.currentTimeMillis() / 1000.0;
   }
}

