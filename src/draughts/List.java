
package draughts;

import java.io.*;

public class List {

   private long[] p_list;
   private int p_size;
   private int p_cap_size;

   public List() {
      p_list = new long[128];
      p_size = 0;
      p_cap_size = 0;
   }

   void add(long mv) {

      int cap_size = Bit.count(Move.caps(mv));

      if (cap_size < p_cap_size) {
         return;
      } else if (cap_size > p_cap_size) {
         p_size = 0;
         p_cap_size = cap_size;
      }

      if (cap_size >= 4 && has(mv)) { // duplicate move
         return;
      }

      assert !has(mv);
      p_list[p_size++] = mv;
   }

   public int size() {
      return p_size;
   }

   public int cap_size() {
      return p_cap_size;
   }

   public long move(int i) {
      assert i < p_size;
      return p_list[i];
   }

   public boolean has(long mv) {

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

