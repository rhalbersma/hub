
Hub 2.1 Copyright (C) 2015-2019 Fabien Letouzey.
This program is distributed under the GNU General Public License version 3.
See license.txt for more details.

---

Today is 2019-07-06.
Hub is a minimal graphical interface (GUI) for the Scan international (10x10) draughts engine (and variants).  It was designed only for operating Scan during the Computer Olypiads and is released "as is" in the hope that you find it useful, especially on Mac or Linux.  Hub is written in Java and is hopefully cross-platform (excluding mobile ones unfortunately).

In order to run Hub, you will need:
- Scan installed on your machine (version 3.0 or more recent)
- Java (run time) installed on your machine
- edit "hub.ini" to specify where Scan is (engine-dir & engine-command); Unix variants require "./" before the command but Windows might not like it (no idea)
- run "hub.jar"; click on the icon I guess or "java -jar hub.jar" in a terminal
- wait for Scan to be ready, as endgame tables can take a while to load

NEW: support for orthogonal captures (Frisian draughts).
Uncomment the following line in hub.ini by removing the '#':
# game-variant = frisian

Note: only the orthogonal captures are supported.  The other Frisian-draughts rules (capture priority, wolf rule, ...) are not.  Scan will follow those rules, however.

Good luck,

Fabien Letouzey (fabien_letouzey@hotmail.com).

---

Configuration

hub.ini (see details below): main Hub configuration
engine.ini: this is sent to the engine; Hub has no idea what it means

engine-dir & engine-command: engine path and executable name

gui-square: default size in pixels of a board square
gui-font: font size
gui-oval: whether to use ellipses or circles for pieces
gui-sound: sound file to play when the engine moves; at least the WAV format is known to work

game-variant: "frisian"; anything else is interpreted as "international draughts"
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
(P)os      -> set up (paste) a FEN position from the system clipboard
(R)everse  -> reverse board
(S)ave     -> save "game.pdn"
escape     -> move now
space      -> play forced move
left/right -> undo/redo one ply
up/down    -> undo/redo full game

