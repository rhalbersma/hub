
package draughts;

import java.io.*;

public class Pos {

   public static Pos Start;

   private final long[] p_piece;
   private final long[] p_side;
   private final int p_turn;

   static void init() {
      Start = new Pos();
   }

   private Pos() {

      p_piece = new long[Piece.Size];
      p_side  = new long[Side.Size];

      int rank_size = (Square.Rank_Size - 1) / 2;

      for (int dense = 0; dense < Square.Dense_Size; dense++) {

         int sq = Square.sparse(dense);

         if (Square.rank(sq, Side.White) < rank_size) {
            p_side[Side.White] = Bit.set(p_side[Side.White], sq);
         } else if (Square.rank(sq, Side.Black) < rank_size) {
            p_side[Side.Black] = Bit.set(p_side[Side.Black], sq);
         }
      }

      p_piece[Piece.Man] = p_side[Side.White] | p_side[Side.Black];
      p_piece[Piece.King] = 0;

      p_turn = Side.White;
   }

   public Pos(int turn, long wm, long bm, long wk, long bk) {

      assert Bit.count(wm | bm | wk | bk) == Bit.count(wm) + Bit.count(bm) + Bit.count(wk) + Bit.count(bk); // all disjoint?

      p_piece = new long[] { wm | bm, wk | bk };
      p_side  = new long[] { wm | wk, bm | bk };
      p_turn  = turn;
   }

   private Pos(long[] piece, long[] side, int turn) {
      p_piece = piece;
      p_side  = side;
      p_turn  = turn;
   }

   public Pos succ(long mv) {

      assert Move.is_legal(mv, this);

      int atk = p_turn;
      int def = Side.opp(atk);

      long froms = mv & side(atk);
      long tos   = mv & empty();
      long caps  = mv & side(def);

      if (tos == 0) tos = froms; // to = from
      int to = Bit.first(tos);

      long[] piece = { p_piece[Piece.Man], p_piece[Piece.King] };
      long[] side  = { p_side[Side.White], p_side[Side.Black] };

      long delta = froms ^ tos;

      side[atk] ^= delta;

      if (Bit.has_common(froms, piece(Piece.King))) { // king move
         piece[Piece.King] ^= delta;
      } else if (Square.is_promotion(to, atk)) { // promotion
         piece[Piece.Man] ^= froms;
         piece[Piece.King] ^= tos;
      } else { // man move
         piece[Piece.Man] ^= delta;
      }

      piece[Piece.Man]  &= ~caps;
      piece[Piece.King] &= ~caps;
      side[def]         &= ~caps;

      return new Pos(piece, side, def);
   }

   public boolean is_end() {
      List list = Gen.gen_moves(this);
      return list.size() == 0;
   }

   public boolean equals(Pos pos) { // for repetition detection
      return p_piece[Piece.Man]  == pos.p_piece[Piece.Man]
          && p_piece[Piece.King] == pos.p_piece[Piece.King]
          && p_side[Side.White]  == pos.p_side[Side.White]
          && p_side[Side.Black]  == pos.p_side[Side.Black]
          && p_turn              == pos.p_turn;
   }

   public String toString() {

      String s = "";

      s += (turn() == Side.White) ? 'W' : 'B';

      for (int dense = 0; dense < Square.Dense_Size; dense++) {

         int sq = Square.sparse(dense);

         switch (piece_side(sq)) {
            case White_Man :  s += 'w'; break;
            case Black_Man :  s += 'b'; break;
            case White_King : s += 'W'; break;
            case Black_King : s += 'B'; break;
            case Empty :      s += 'e'; break;
         }
      }

      return s;
   }

   public int turn() {
      return p_turn;
   }

   public long piece(int pc) {
      return p_piece[pc];
   }

   public long side(int sd) {
      return p_side[sd];
   }

   public long piece_side(int pc, int sd) {
      return piece(pc) & side(sd);
   }

   public long empty() {
      return Bit.Squares & ~(p_side[Side.White] | p_side[Side.Black]);
   }

   public boolean is_piece(int sq, int pc) {
      return Bit.has(piece(pc), sq);
   }

   public boolean is_side(int sq, int sd) {
      return Bit.has(side(sd), sq);
   }

   public boolean is_piece_side(int sq, int pc, int sd) {
      return Bit.has(piece_side(pc, sd), sq);
   }

   public boolean is_empty(int sq) {
      return Bit.has(empty(), sq);
   }

   public Piece_Side piece_side(int sq) {

      if (is_piece_side(sq, Piece.Man, Side.White)) {
         return Piece_Side.White_Man;
      } else if (is_piece_side(sq, Piece.Man, Side.Black)) {
         return Piece_Side.Black_Man;
      } else if (is_piece_side(sq, Piece.King, Side.White)) {
         return Piece_Side.White_King;
      } else if (is_piece_side(sq, Piece.King, Side.Black)) {
         return Piece_Side.Black_King;
      } else {
         return Piece_Side.Empty;
      }
   }

   public void disp() {

      for (int rk = 0; rk < Square.Rank_Size; rk++) {

         for (int fl = 0; fl < Square.File_Size; fl++) {

            if (Square.is_light(fl, rk)) {

               System.out.print("  ");

            } else {

               int sq = Square.make(fl, rk);

               switch (piece_side(sq)) {
                  case White_Man :  System.out.print("O "); break;
                  case Black_Man :  System.out.print("* "); break;
                  case White_King : System.out.print("@ "); break;
                  case Black_King : System.out.print("# "); break;
                  case Empty :      System.out.print("- "); break;
               }
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

