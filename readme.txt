
Hub 2.0 Copyright (C) 2015-2017 Fabien Letouzey.
This program is distributed under the GNU General Public License version 3.
See license.txt for more details.

---

Today is 2017-07-11.
Hub is a minimal graphical interface (GUI) for the Scan international (10x10) draughts engine.  It was designed only for operating Scan during the Computer Olypiads and is released "as is" in the hope that you find it useful, especially on Mac or Linux.  Hub is written in Java and is hopefully cross-platform.

In order to run Hub, you will need:
- Scan installed on your machine (version 3.0 or more recent)
- Java (run time) installed on your machine
- edit "hub.ini" to specify where Scan is (engine-dir & engine-command); Unix variants require "./" before the command but Windows might not like it (no idea)
- run "hub.jar"; click on the icon I guess or "java -jar hub.jar" in a terminal
- wait for Scan to be ready, as endgame tables can take a while to load

The changes from Hub 1.0 are:
- support for the new Hub protocol used in Scan 3.x (see protocol.txt for technical details)
- values (such as directory name) in the INI files can now contain spaces
- the 'P' key will set up a position in FEN format; you first need to copy it from an external source such as a web site
- minor fixes

Good luck,

Fabien Letouzey (fabien_letouzey@hotmail.com).

---

Configuration

hub.ini (see details below): main Hub configuration
engine.ini: this is sent to the engine; Hub has no idea what it means
NEW: Spaces are now allowed in the INI files (fixed from version 1.0)!

engine-dir & engine-command: engine path and executable name

gui-square: default size in pixels of a board square
gui-font: font size
gui-oval: whether to use ellipses or circles for pieces
gui-sound: sound file to play when the engine moves; at least the WAV format is known to work

game-moves & game-time & game-inc: time control.
game-moves is the number of moves per period, repeating (0 = whole game)
game-time in minutes
game-inc in seconds; time added before every move (0 for no increment)
popular combinations are moves + time (inc = 0) and time + inc (moves = 0).

game-ponder: enable pondering; you also need to set ponder = true in engine.ini!

log: whether to write a log file (append to "log.txt")

---

Usage.

Apart from using the mouse to input moves, Hub is controlled using the keyboard (no menu).  If you want to analyse a game, you need to copy the PDN text (or move list) to "game.pdn" and then press L inside Hub.  A single FEN tag can be used to load a position.

0-2        -> number of computer players (e.g. 2 = auto-play)
(A)nalyse  -> show engine analysis; you can move through an existing game with the arrow keys
(G)o       -> make the computer play your side
(L)oad     -> load "game.pdn"; note that only the first game is loaded!
(N)ew game -> new game
(O)val     -> toggle oval graphics on/off
(P)os      -> NEW: set up (paste) a FEN position from the system clipboard
(R)everse  -> reverse board
(S)ave     -> save "game.pdn"
escape     -> move now
space      -> play forced move
left/right -> undo/redo one ply
up/down    -> undo/redo full game

