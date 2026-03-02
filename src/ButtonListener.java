import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.*;
import javax.swing.*;
import java.util.ArrayList;

public class ButtonListener implements ActionListener {
    static JButton lastSelected = null;
    static int lastSelectedIndex = -1;
    static ArrayList<Move> lastMoves = new ArrayList<>();

    Bot bot;

    public ButtonListener(Bot bot) {
        this.bot = bot;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int sourceIndex = 0;
        JButton sourceButton = null;
        for (int i = 0; i < 64; i++) {
            JButton tile = GraphicalGame.tiles[i];
            if (tile == e.getSource()) {
                sourceIndex = i;
                sourceButton = tile;
                break;
            }
        }

        // Calculate if the currently selected tile was in the list of last selected tile's moves
        boolean validMove = false;
        for (Move m : lastMoves) {
            if (m.endIndex == sourceIndex) {
                validMove = true;
                break;
            }
        }

        // If the selected tile is valid (not the last selected tile, not an empty tile, not an opponents tile)
        if ((lastSelected != sourceButton && bot.getBoard().board[sourceIndex] != 0 && Piece.color(bot.getBoard().board[sourceIndex]) == bot.getBoard().toMove)) {
            // Undo the coloring from the last selected tile
            if (lastSelected != null) {
                if (((lastSelectedIndex % 8) + (lastSelectedIndex / 8)) % 2 == 0) lastSelected.setBackground(GraphicalGame.evenTileColor);
                else lastSelected.setBackground(GraphicalGame.oddTileColor);
            }
            for (Move m : lastMoves) {
                GraphicalGame.tiles[m.endIndex].setForeground(Color.BLACK);
                if (bot.getBoard().board[m.endIndex] != 0)
                    GraphicalGame.tiles[m.endIndex].setText(Board.getPieceCharMap().get(bot.getBoard().board[m.endIndex]) + "");
                else
                    GraphicalGame.tiles[m.endIndex].setText("");
            }

            // Color the selected tile
            if (((sourceIndex % 8) + (sourceIndex / 8)) % 2 == 0)
                sourceButton.setBackground(GraphicalGame.selectedEvenTileColor);
            else
                sourceButton.setBackground(GraphicalGame.selectedOddTileColor);

            // Color the squares that have valid moves
            ArrayList<Move> moves = bot.generateMoves();

            if (moves.isEmpty()) GraphicalGame.moveStatus.setText("Black's Checkmate!");

            for (Move m : moves) {
                if (m.startIndex == sourceIndex) {
                    if (bot.getBoard().board[m.endIndex] == 0) GraphicalGame.tiles[m.endIndex].setText("•");
                    GraphicalGame.tiles[m.endIndex].setForeground(GraphicalGame.attackColor);
                }
            }

            GraphicalGame.evaluation.setText("Evaluation: " + bot.evaluateBoard());

            // Update the last selections
            lastSelected = sourceButton;
            lastSelectedIndex = sourceIndex;
            lastMoves = moves;
        }
        else { // If nothing (cancel)
            if (lastSelected != null) {
                if (((lastSelectedIndex % 8) + (lastSelectedIndex / 8)) % 2 == 0) lastSelected.setBackground(GraphicalGame.evenTileColor);
                else lastSelected.setBackground(GraphicalGame.oddTileColor);
                for (Move m : lastMoves) {
                    GraphicalGame.tiles[m.endIndex].setForeground(Color.BLACK);
                    if (bot.getBoard().board[m.endIndex] != 0)
                        GraphicalGame.tiles[m.endIndex].setText(Board.getPieceCharMap().get(bot.getBoard().board[m.endIndex]) + "");
                    else
                        GraphicalGame.tiles[m.endIndex].setText("");
                }
            }

            if (validMove) {
                // Undo the coloring from the last selected tile
                if (lastSelected != null) {
                    if (((lastSelectedIndex % 8) + (lastSelectedIndex / 8)) % 2 == 0) lastSelected.setBackground(GraphicalGame.evenTileColor);
                    else lastSelected.setBackground(GraphicalGame.oddTileColor);
                }
                for (Move m : lastMoves) {
                    GraphicalGame.tiles[m.endIndex].setForeground(Color.BLACK);
                    if (bot.getBoard().board[m.endIndex] != 0)
                        GraphicalGame.tiles[m.endIndex].setText(Board.getPieceCharMap().get(bot.getBoard().board[m.endIndex]) + "");
                    else
                        GraphicalGame.tiles[m.endIndex].setText("");
                }

                // Make the move
                bot.getBoard().makeMove(bot.validateMove(lastSelectedIndex, sourceIndex));
                GraphicalGame.displayBoard(bot.getBoard());

                // Perform the bot's move
                Move move = bot.findBestMove(3);
                if (move == null) GraphicalGame.moveStatus.setText("White's checkmate!");
                else {
                    bot.getBoard().makeMove(move);
                    GraphicalGame.displayBoard(bot.getBoard());
                }
            }

            lastSelected = null;
            lastSelectedIndex = -1;
            lastMoves = new ArrayList<>();
        }
    }
}
