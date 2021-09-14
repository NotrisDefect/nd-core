package cabbageroll.notrisdefect.core.tables;

import java.awt.Point;

public class PieceTable {
    public static final PieceTable GUIDELINE = new PieceTable(new Point[][][]{
        //Rotations: 0,R,2,L
        //Z
        {
            {new Point(0, 0), new Point(1, 0), new Point(1, 1), new Point(2, 1)},
            {new Point(2, 0), new Point(1, 1), new Point(2, 1), new Point(1, 2)},
            {new Point(0, 1), new Point(1, 1), new Point(1, 2), new Point(2, 2)},
            {new Point(1, 0), new Point(0, 1), new Point(1, 1), new Point(0, 2)}
        },
        //L
        {
            {new Point(2, 0), new Point(0, 1), new Point(1, 1), new Point(2, 1)},
            {new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(2, 2)},
            {new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(0, 2)},
            {new Point(0, 0), new Point(1, 0), new Point(1, 1), new Point(1, 2)}
        },
        //O
        {
            {new Point(1, 0), new Point(2, 0), new Point(1, 1), new Point(2, 1)},
            {new Point(1, 0), new Point(2, 0), new Point(1, 1), new Point(2, 1)},
            {new Point(1, 0), new Point(2, 0), new Point(1, 1), new Point(2, 1)},
            {new Point(1, 0), new Point(2, 0), new Point(1, 1), new Point(2, 1)},
        },
        //S
        {

            {new Point(1, 0), new Point(2, 0), new Point(0, 1), new Point(1, 1)},
            {new Point(1, 0), new Point(1, 1), new Point(2, 1), new Point(2, 2)},
            {new Point(1, 1), new Point(2, 1), new Point(0, 2), new Point(1, 2)},
            {new Point(0, 0), new Point(0, 1), new Point(1, 1), new Point(1, 2)}
        },
        //I
        {
            {new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(3, 1)},
            {new Point(2, 0), new Point(2, 1), new Point(2, 2), new Point(2, 3)},
            {new Point(0, 2), new Point(1, 2), new Point(2, 2), new Point(3, 2)},
            {new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(1, 3)}
        },
        //J
        {
            {new Point(0, 0), new Point(0, 1), new Point(1, 1), new Point(2, 1)},
            {new Point(1, 0), new Point(2, 0), new Point(1, 1), new Point(1, 2)},
            {new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(2, 2)},
            {new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(0, 2)}
        },
        //T
        {
            {new Point(1, 0), new Point(0, 1), new Point(1, 1), new Point(2, 1)},
            {new Point(1, 0), new Point(1, 1), new Point(2, 1), new Point(1, 2)},
            {new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(1, 2)},
            {new Point(1, 0), new Point(0, 1), new Point(1, 1), new Point(1, 2)}
        }
    });

    private static final int PIECES = 7;
    private static final int ROTATIONS = 4;
    private static final int POINTS = 4;
    private final Point[][][] pieces;

    public PieceTable(Point[][][] pieces) {
        this.pieces = pieces;
        if (!isValid()) {
            throw new IllegalArgumentException();
        }
    }

    public Point[] getPiece(int piece, int rotation) {
        return pieces[piece][rotation];
    }

    private boolean isValid() {
        if (pieces.length == PIECES) {
            for (int i = 0; i < PIECES; i++) {
                if (pieces[i].length == ROTATIONS) {
                    for (int j = 0; j < ROTATIONS; j++) {
                        if (pieces[i][j].length == POINTS) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
