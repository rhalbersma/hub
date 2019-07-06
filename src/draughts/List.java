
package draughts;

import java.io.*;

public class List {

   private long[] p_list;
   private int p_size;
   private int p_cap_size;

   List() {
      p_list = new long[128];
      p_size = 0;
      p_cap_size = 0;
   }

   void add_move(int from, int to) {

      assert p_cap_size == 0;

      long mv = Move.make(from, to);

      assert !has(mv);
      p_list[p_size++] = mv;
   }

   void add_capture(int from, int to, long caps) {

      assert caps != 0;

      int cap_size = Bit.count(caps);

      if (cap_size < p_cap_size) return;

      if (cap_size > p_cap_size) { // new largest capture
         p_size = 0;
         p_cap_size = cap_size;
      }

      long mv = Move.make(from, to, caps);

      if (cap_size >= 2 && has(mv)) return; // duplicate move

      assert !has(mv);
      p_list[p_size++] = mv;
   }

   public int size() {
      return p_size;
   }

   int cap_size() {
      return p_cap_size;
   }

   public long move(int i) {
      assert i < p_size;
      return p_list[i];
   }

   boolean has(long mv) {

      for (int i = 0; i < p_size; i++) {
         if (p_list[i] == mv) return true;
      }

      return false;
   }

   public long find(long bit) {

      if (bit == 0) return Move.None;

      long move = Move.None;
      int size = 0;

      for (int i = 0; i < p_size; i++) {

         long mv = p_list[i];

         if (Bit.is_incl(bit, Move.bit(mv))) {
            move = mv;
            size++;
         }
      }

      if (size == 0) {
         return Move.None;
      } else if (size == 1) {
         return move;
      } else {
         return Move.Amb;
      }
   }

   public void disp(Pos pos) {

      for (int i = 0; i < p_size; i++) {
         long mv = p_list[i];
         System.out.print(Move.to_string(mv, pos) + " ");
      }

      System.out.println();
   }
}

