import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.*;
import javax.swing.*;
import java.util.ArrayList;

public class ButtonListener implements ActionListener {
    static JButton lastSelected = null;
    static int lastSelectedIndex = -1;
    static ArrayList<Move> lastMoves = new ArrayList<>();

    Board board;

    public ButtonListener(Board board) {
        this.board = board;
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
        if ((lastSelected != sourceButton && board.board[sourceIndex] != 0 && Piece.color(board.board[sourceIndex]) == board.toMove)) {
            // Undo the coloring from the last selected tile
            if (lastSelected != null) {
                if (((lastSelectedIndex % 8) + (lastSelectedIndex / 8)) % 2 == 0) lastSelected.setBackground(GraphicalGame.evenTileColor);
                else lastSelected.setBackground(GraphicalGame.oddTileColor);
            }
            for (Move m : lastMoves) {
                GraphicalGame.tiles[m.endIndex].setForeground(Color.BLACK);
                if (board.board[m.endIndex] != 0)
                    GraphicalGame.tiles[m.endIndex].setText(Board.getPieceCharMap().get(board.board[m.endIndex]) + "");
                else
                    GraphicalGame.tiles[m.endIndex].setText("");
            }

            // Color the selected tile
            if (((sourceIndex % 8) + (sourceIndex / 8)) % 2 == 0)
                sourceButton.setBackground(GraphicalGame.selectedEvenTileColor);
            else
                sourceButton.setBackground(GraphicalGame.selectedOddTileColor);

            // Color the squares that have valid moves
            ArrayList<Move> moves = Bot.generateMoves(board);

            if (moves.size() == 0) GraphicalGame.moveStatus.setText("Black's Checkmate!");

            for (Move m : moves) {
                if (m.startIndex == sourceIndex) {
                    if (board.board[m.endIndex] == 0) GraphicalGame.tiles[m.endIndex].setText("•");
                    GraphicalGame.tiles[m.endIndex].setForeground(GraphicalGame.attackColor);
                }
            }

            GraphicalGame.evaluation.setText("Evaluation: " + Bot.evaluateBoard(board));

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
                    if (board.board[m.endIndex] != 0)
                        GraphicalGame.tiles[m.endIndex].setText(Board.getPieceCharMap().get(board.board[m.endIndex]) + "");
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
                    if (board.board[m.endIndex] != 0)
                        GraphicalGame.tiles[m.endIndex].setText(Board.getPieceCharMap().get(board.board[m.endIndex]) + "");
                    else
                        GraphicalGame.tiles[m.endIndex].setText("");
                }

                // Make the move
                board.makeMove(Bot.validateMove(board, lastSelectedIndex, sourceIndex));
                GraphicalGame.displayBoard(board);

                // Perform the bot's move
                Move move = Bot.findBestMove(board, 3);
                if (move == null) GraphicalGame.moveStatus.setText("White's checkmate!");
                else {
                    board.makeMove(move);
                    GraphicalGame.displayBoard(board);
                }
            }

            lastSelected = null;
            lastSelectedIndex = -1;
            lastMoves = new ArrayList<>();
        }
    }
}
