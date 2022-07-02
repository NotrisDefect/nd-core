package cabbageroll.notrisdefect.core.tables;

import cabbageroll.notrisdefect.core.GameLogic;
import cabbageroll.notrisdefect.core.Point;

public class PieceTable {

    public static final PieceTable LUMINES = new PieceTable(new int[][][][]{
        {
            {{0, 0, GameLogic.PIECE_1ST}, {1, 0, GameLogic.PIECE_1ST}, {0, 1, GameLogic.PIECE_1ST}, {1, 1, GameLogic.PIECE_1ST}}
        },
        {
            {{0, 0, GameLogic.PIECE_2ND}, {1, 0, GameLogic.PIECE_1ST}, {0, 1, GameLogic.PIECE_1ST}, {1, 1, GameLogic.PIECE_1ST}},
            {{1, 0, GameLogic.PIECE_2ND}, {1, 1, GameLogic.PIECE_1ST}, {0, 0, GameLogic.PIECE_1ST}, {0, 1, GameLogic.PIECE_1ST}},
            {{1, 1, GameLogic.PIECE_2ND}, {0, 1, GameLogic.PIECE_1ST}, {1, 0, GameLogic.PIECE_1ST}, {0, 0, GameLogic.PIECE_1ST}},
            {{0, 1, GameLogic.PIECE_2ND}, {0, 0, GameLogic.PIECE_1ST}, {1, 1, GameLogic.PIECE_1ST}, {1, 0, GameLogic.PIECE_1ST}}
        },
        {
            {{0, 0, GameLogic.PIECE_2ND}, {1, 0, GameLogic.PIECE_2ND}, {0, 1, GameLogic.PIECE_1ST}, {1, 1, GameLogic.PIECE_1ST}},
            {{1, 0, GameLogic.PIECE_2ND}, {1, 1, GameLogic.PIECE_2ND}, {0, 0, GameLogic.PIECE_1ST}, {0, 1, GameLogic.PIECE_1ST}},
            {{1, 1, GameLogic.PIECE_2ND}, {0, 1, GameLogic.PIECE_2ND}, {1, 0, GameLogic.PIECE_1ST}, {0, 0, GameLogic.PIECE_1ST}},
            {{0, 1, GameLogic.PIECE_2ND}, {0, 0, GameLogic.PIECE_2ND}, {1, 1, GameLogic.PIECE_1ST}, {1, 0, GameLogic.PIECE_1ST}}
        },
        {
            {{0, 0, GameLogic.PIECE_2ND}, {1, 0, GameLogic.PIECE_1ST}, {0, 1, GameLogic.PIECE_1ST}, {1, 1, GameLogic.PIECE_2ND}},
            {{1, 0, GameLogic.PIECE_2ND}, {1, 1, GameLogic.PIECE_1ST}, {0, 0, GameLogic.PIECE_1ST}, {0, 1, GameLogic.PIECE_2ND}}
        },
        {
            {{0, 0, GameLogic.PIECE_1ST}, {1, 0, GameLogic.PIECE_2ND}, {0, 1, GameLogic.PIECE_2ND}, {1, 1, GameLogic.PIECE_2ND}},
            {{1, 0, GameLogic.PIECE_1ST}, {1, 1, GameLogic.PIECE_2ND}, {0, 0, GameLogic.PIECE_2ND}, {0, 1, GameLogic.PIECE_2ND}},
            {{1, 1, GameLogic.PIECE_1ST}, {0, 1, GameLogic.PIECE_2ND}, {1, 0, GameLogic.PIECE_2ND}, {0, 0, GameLogic.PIECE_2ND}},
            {{0, 1, GameLogic.PIECE_1ST}, {0, 0, GameLogic.PIECE_2ND}, {1, 1, GameLogic.PIECE_2ND}, {1, 0, GameLogic.PIECE_2ND}}
        },
        {
            {{0, 0, GameLogic.PIECE_2ND}, {1, 0, GameLogic.PIECE_2ND}, {0, 1, GameLogic.PIECE_2ND}, {1, 1, GameLogic.PIECE_2ND}}
        }
    });

    public static final PieceTable GUIDELINE = new PieceTable(new int[][][][]{
        {
            {{0, 0}, {1, 0}, {1, 1}, {2, 1}},
            {{2, 0}, {2, 1}, {1, 1}, {1, 2}},
            {{2, 2}, {1, 2}, {1, 1}, {0, 1}},
            {{0, 2}, {0, 1}, {1, 1}, {1, 0}}
        },
        {
            {{2, 0}, {0, 1}, {1, 1}, {2, 1}},
            {{1, 0}, {1, 2}, {1, 1}, {2, 2}},
            {{0, 2}, {2, 1}, {1, 1}, {0, 1}},
            {{1, 2}, {1, 0}, {1, 1}, {0, 0}}
        },
        {
            {{1, 0}, {2, 0}, {1, 1}, {2, 1}},
            {{1, 0}, {1, 1}, {2, 0}, {2, 1}},
            {{2, 1}, {1, 1}, {2, 0}, {1, 0}},
            {{2, 1}, {2, 0}, {1, 1}, {1, 0}},
        },
        {
            {{1, 0}, {2, 0}, {0, 1}, {1, 1}},
            {{1, 0}, {2, 1}, {1, 1}, {2, 2}},
            {{1, 2}, {0, 2}, {2, 1}, {1, 1}},
            {{1, 2}, {0, 1}, {1, 1}, {0, 0}}
        },
        {
            {{0, 1}, {1, 1}, {2, 1}, {3, 1}},
            {{2, 0}, {2, 2}, {2, 1}, {2, 3}},
            {{3, 2}, {2, 2}, {1, 2}, {0, 2}},
            {{1, 3}, {1, 2}, {1, 1}, {1, 0}}
        },
        {
            {{0, 0}, {0, 1}, {1, 1}, {2, 1}},
            {{1, 0}, {1, 1}, {2, 0}, {1, 2}},
            {{2, 2}, {2, 1}, {1, 1}, {0, 1}},
            {{0, 2}, {1, 2}, {1, 1}, {1, 0}}
        },
        {
            {{1, 0}, {0, 1}, {1, 1}, {2, 1}},
            {{1, 0}, {2, 1}, {1, 1}, {1, 2}},
            {{1, 2}, {2, 1}, {1, 1}, {0, 1}},
            {{1, 2}, {0, 1}, {1, 1}, {1, 0}}
        }
    }, new int[]{
        GameLogic.PIECE_Z,
        GameLogic.PIECE_L,
        GameLogic.PIECE_O,
        GameLogic.PIECE_S,
        GameLogic.PIECE_I,
        GameLogic.PIECE_J,
        GameLogic.PIECE_T
    });

    private final Piece[][] pieces;

    public PieceTable(int[][][][] table) {
        pieces = new Piece[table.length][];
        for (int i = 0; i < table.length; i++) {
            int[][][] piece = table[i];
            pieces[i] = new Piece[piece.length];

            for (int j = 0; j < piece.length; j++) {
                int[][] rotation = piece[j];
                Point[] points = new Point[rotation.length];
                int[] colors = new int[rotation.length];

                for (int k = 0; k < rotation.length; k++) {
                    int[] mino = rotation[k];
                    points[k] = new Point(mino[0], mino[1]);
                    colors[k] = mino[2];
                }

                pieces[i][j] = new Piece(points, colors);
            }
        }
    }

    public PieceTable(int[][][][] table, int[] colors) {
        pieces = new Piece[table.length][];
        for (int i = 0; i < table.length; i++) {
            int[][][] piece = table[i];
            pieces[i] = new Piece[piece.length];

            for (int j = 0; j < piece.length; j++) {
                int[][] rotation = piece[j];
                Point[] points = new Point[rotation.length];
                int[] color = new int[rotation.length];

                for (int k = 0; k < rotation.length; k++) {
                    int[] mino = rotation[k];
                    points[k] = new Point(mino[0], mino[1]);
                    color[k] = colors[i];
                }

                pieces[i][j] = new Piece(points, color);
            }
        }
    }

    public int amount() {
        return pieces.length;
    }

    public Piece getPiece(int piece, int rotation) {
        return pieces[piece][rotation];
    }

    public int mostPiecePoints() {
        int n = 0;
        for (int i = 0; i < pieces.length; i++) {
            Piece[] piece = pieces[i];
            for (int j = 0; j < piece.length; j++) {
                Point[] points = piece[j].getPoints();
                for (int k = 0; k < points.length; k++) {
                    n = Math.max(n, points[k].x);
                    n = Math.max(n, points[k].y);
                }
            }
        }
        return n;
    }

    public int rotations(int ordinal) {
        return pieces[ordinal].length;
    }

    public static class Piece {
        private final Point[] points;
        private final int[] colors;

        private Piece(Point[] points, int[] colors) {
            this.points = points;
            this.colors = colors;
        }

        public int[] getColors() {
            return colors;
        }

        public Point[] getPoints() {
            return points;
        }
    }
}
