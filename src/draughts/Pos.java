
package draughts;

import java.io.*;

public class Pos {

   private long[] p_piece;
   private long p_king;
   private int p_turn;

   public Pos() {

      p_piece = new long[Side.Size];

      for (int i = 30; i < 50; i++) {
         int sq = Square.from_50(i);
         p_piece[Side.White] = Bit.set(p_piece[Side.White], sq);
      }

      for (int i = 0; i < 20; i++) {
         int sq = Square.from_50(i);
         p_piece[Side.Black] = Bit.set(p_piece[Side.Black], sq);
      }

      p_king = 0;

      p_turn = Side.White;
   }

   public Pos(int turn, long wm, long bm, long wk, long bk) {

      p_piece = new long[Side.Size];
      p_piece[Side.White] = wm | wk;
      p_piece[Side.Black] = bm | bk;

      p_king = wk | bk;

      p_turn = turn;
   }

   public Pos(Pos pos, long mv) {

      p_piece = new long[Side.Size];
      p_piece[Side.White] = pos.p_piece[Side.White];
      p_piece[Side.Black] = pos.p_piece[Side.Black];
      p_king = pos.p_king;
      p_turn = pos.p_turn;

      int atk = pos.p_turn;
      int def = Side.opp(atk);

      int  from = Move.from(mv);
      int  to   = Move.to(mv);
      long caps = Move.caps(mv);

      assert Bit.is_set(pos.piece(atk), from);
      assert to == from || Bit.is_set(pos.empty(), to);
      assert Bit.is_incl(caps, pos.piece(def));

      p_piece[atk] = Bit.clear(p_piece[atk], from);
      p_piece[atk] = Bit.set(p_piece[atk], to);

      if (Bit.is_set(pos.p_king, from)) {
         p_king = Bit.clear(p_king, from);
         p_king = Bit.set(p_king, to);
      } else if (Square.is_promotion(to, atk)) {
         p_king = Bit.set(p_king, to);
      }

      p_piece[def] = p_piece[def] & ~caps;
      p_king = p_king & ~caps;

      p_turn = def;
   }

   public boolean is_end() {
      List list = Gen.gen_moves(this);
      return list.size() == 0;
   }

   public boolean equals(Pos pos) { // for repetition detection
      return p_piece[Side.White] == pos.p_piece[Side.White]
          && p_piece[Side.Black] == pos.p_piece[Side.Black]
          && p_king == pos.p_king
          && p_turn == pos.p_turn;
   }

   public String toString() {

      String s = "";

      s += (turn() == Side.White) ? 'w' : 'b';

      for (int i = 0; i < 50; i++) {

         int sq = Square.from_50(i);

         switch (square(sq)) {
         case Piece.WM :    s += 'w'; break;
         case Piece.BM :    s += 'b'; break;
         case Piece.WK :    s += 'W'; break;
         case Piece.BK :    s += 'B'; break;
         case Piece.Empty : s += 'e'; break;
         default :          s += '?'; break;
         }
      }

      return s;
   }

   public int turn() {
      return p_turn;
   }

   long man(int sd) {
      return p_piece[sd] & ~p_king;
   }

   long king(int sd) {
      return p_piece[sd] & p_king;
   }

   public long piece(int sd) {
      return p_piece[sd];
   }

   long all() {
      return p_piece[Side.White] | p_piece[Side.Black];
   }

   public long empty() {
      return Bit.Squares & ~all();
   }

   public boolean is_empty(int sq) {
      return Bit.is_set(empty(), sq);
   }

   public boolean is_side(int sq, int sd) {
      return Bit.is_set(piece(sd), sq);
   }

   public int square(int sq) {

      if (Bit.is_set(man(Side.White), sq)) {
         return Piece.WM;
      } else if (Bit.is_set(man(Side.Black), sq)) {
         return Piece.BM;
      } else if (Bit.is_set(king(Side.White), sq)) {
         return Piece.WK;
      } else if (Bit.is_set(king(Side.Black), sq)) {
         return Piece.BK;
      } else {
         return Piece.Empty;
      }
   }

   public void disp() {

      for (int y = 0; y < 10; y++) {

         if (y % 2 == 0) {
            System.out.print("  ");
         }

         for (int x = 0; x < 5; x++) {

            int sq = Square.from_50(y * 5 + x);

            switch (square(sq)) {
            case Piece.WM :    System.out.print("O   "); break;
            case Piece.BM :    System.out.print("*   "); break;
            case Piece.WK :    System.out.print("@   "); break;
            case Piece.BK :    System.out.print("#   "); break;
            case Piece.Empty : System.out.print("-   "); break;
            default :          System.out.print("?   "); break;
            }
         }

         System.out.println();
      }

      System.out.println();

      if (p_turn == Side.White) {
         System.out.println("white to play");
         System.out.println();
      } else {
         System.out.println("black to play");
         System.out.println();
      }
   }
}

