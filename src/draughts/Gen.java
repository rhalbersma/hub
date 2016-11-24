
package draughts;

import java.io.*;

public class Gen {

   public static List gen_moves(Pos pos) {

      List list = new List();

      add_captures(list, pos);
      if (list.size() == 0) add_quiets(list, pos);

      return list;
   }

   private static void add_quiets(List list, Pos pos) {

      int atk = pos.turn();
      long empty = pos.empty();

      // man moves

      for (int dir = 0; dir < 2; dir++) {

         int inc = Square.side_inc[atk][dir];

         for (long froms = pos.man(atk) & Bit.shift(empty, -inc); froms != 0; froms = Bit.rest(froms)) {
            int from = Bit.first(froms);
            list.add(Move.make(from, from + inc));
         }
      }

      // king moves

      for (long froms = pos.king(atk); froms != 0; froms = Bit.rest(froms)) {

         int from = Bit.first(froms);

         for (long tos = Bit.king_moves(from) & empty; tos != 0; tos = Bit.rest(tos)) {

            int to = Bit.first(tos);

            if (Bit.is_incl(Bit.between(from, to), empty)) {
               list.add(Move.make(from, to));
            }
         }
      }
   }

   private static void add_captures(List list, Pos pos) {

      int atk = pos.turn();
      int def = Side.opp(atk);

      long opp = pos.piece(def);
      long empty = pos.empty();

      // man captures

      for (int dir = 0; dir < 4; dir++) {

         int inc = Square.inc[dir];

         for (long froms = pos.man(atk) & Bit.shift(opp, -inc) & Bit.shift(empty, -inc * 2); froms != 0; froms = Bit.rest(froms)) {
            int from = Bit.first(froms);
            add_man_captures(list, from, Bit.bit(from + inc), Bit.clear(opp, from + inc), Bit.set(empty, from), from + inc * 2);
         }
      }

      // king captures

      for (long froms = pos.king(atk); froms != 0; froms = Bit.rest(froms)) {

         int from = Bit.first(froms);

         for (long targets = Bit.king_moves(from) & opp; targets != 0; targets = Bit.rest(targets)) {

            int cap = Bit.first(targets);

            if (Bit.is_incl(Bit.between(from, cap), empty)) {
               add_king_captures(list, from, Bit.bit(cap), Bit.clear(opp, cap), Bit.set(empty, from), cap, Bit.inc(from, cap));
            }
         }
      }
   }

   private static void add_man_captures(List list, int start, long caps, long opp, long empty, int from) {

      assert Square.is_valid(from);

      for (int dir = 0; dir < 4; dir++) {

         int inc = Square.inc[dir];

         if (Bit.is_set(Bit.shift(opp, -inc) & Bit.shift(empty, -inc * 2), from)) {
            add_man_captures(list, start, Bit.set(caps, from + inc), Bit.clear(opp, from + inc), empty, from + inc * 2);
         }
      }

      list.add(Move.make(start, from, caps));
   }

   private static void add_king_captures(List list, int start, long caps, long opp, long empty, int sq, int inc) {

      assert Square.is_valid(sq);

      for (int to = sq + inc; Square.is_valid(to) && Bit.is_set(empty, to); to += inc) {

         for (long targets = Bit.king_moves(to) & opp; targets != 0; targets = Bit.rest(targets)) {

            int cap = Bit.first(targets);

            if (Bit.is_incl(Bit.between(to, cap), empty)) {

               int new_inc = Bit.inc(to, cap);

               if (new_inc != inc || to == sq + inc) {
                  add_king_captures(list, start, Bit.set(caps, cap), Bit.clear(opp, cap), empty, cap, new_inc);
               }
            }
         }

         list.add(Move.make(start, to, caps));
      }
   }
}

