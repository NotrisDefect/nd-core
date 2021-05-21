package tetrcore;

import java.awt.Point;

public class Kicktable {
    public static final Kicktable kicktable_srs_tetrio = new Kicktable(new Point[][][]{
        {//J, L, S, T, Z Tetromino Wall Kick Data

            {new Point(0, 0), new Point(-1, 0), new Point(-1, +1), new Point(0, -2), new Point(-1, -2)},
            {new Point(0, 0), new Point(+1, 0), new Point(+1, -1), new Point(0, +2), new Point(+1, +2)},

            {new Point(0, 0), new Point(+1, 0), new Point(+1, -1), new Point(0, +2), new Point(+1, +2)},
            {new Point(0, 0), new Point(-1, 0), new Point(-1, +1), new Point(0, -2), new Point(-1, -2)},

            {new Point(0, 0), new Point(+1, 0), new Point(+1, +1), new Point(0, -2), new Point(+1, -2)},
            {new Point(0, 0), new Point(-1, 0), new Point(-1, -1), new Point(0, +2), new Point(-1, +2)},

            {new Point(0, 0), new Point(-1, 0), new Point(-1, -1), new Point(0, +2), new Point(-1, +2)},
            {new Point(0, 0), new Point(+1, 0), new Point(+1, +1), new Point(0, -2), new Point(+1, -2)},

            {new Point(0, 0), new Point(0, +1), new Point(+1, +1), new Point(-1, +1), new Point(+1, 0), new Point(-1, 0)},
            {new Point(0, 0), new Point(+1, 0), new Point(+1, +2), new Point(+1, +1), new Point(0, +2), new Point(0, +1)},

            {new Point(0, 0), new Point(0, -1), new Point(-1, -1), new Point(+1, -1), new Point(-1, 0), new Point(+1, 0)},
            {new Point(0, 0), new Point(-1, 0), new Point(-1, +2), new Point(-1, +1), new Point(0, +2), new Point(0, +1)},
        },
        {//I Tetromino Wall Kick Data

            {new Point(0, 0), new Point(+1, 0), new Point(-2, 0), new Point(-2, -1), new Point(+1, +2)},
            {new Point(0, 0), new Point(-1, 0), new Point(+2, 0), new Point(-1, -2), new Point(+2, +1)},

            {new Point(0, 0), new Point(-1, 0), new Point(+2, 0), new Point(-1, +2), new Point(+2, -1)},
            {new Point(0, 0), new Point(-2, 0), new Point(+1, 0), new Point(-2, +1), new Point(+1, -2)},

            {new Point(0, 0), new Point(+2, 0), new Point(-1, 0), new Point(+2, +1), new Point(-1, -2)},
            {new Point(0, 0), new Point(+1, 0), new Point(-2, 0), new Point(+1, +2), new Point(-2, -1)},

            {new Point(0, 0), new Point(+1, 0), new Point(-2, 0), new Point(+1, -2), new Point(-2, +1)},
            {new Point(0, 0), new Point(-1, 0), new Point(+2, 0), new Point(+2, -1), new Point(-1, +2)},

            {new Point(0, 0), new Point(0, +1)},
            {new Point(0, 0), new Point(0, -1)},

            {new Point(0, 0), new Point(+1, 0)},
            {new Point(0, 0), new Point(-1, 0)},
        }
    });
    public static final Kicktable kicktable_srs_guideline = new Kicktable(new Point[][][]{
        {//J, L, S, T, Z Tetromino Wall Kick Data

            {new Point(0, 0), new Point(-1, 0), new Point(-1, +1), new Point(0, -2), new Point(-1, -2)},
            {new Point(0, 0), new Point(+1, 0), new Point(+1, -1), new Point(0, +2), new Point(+1, +2)},

            {new Point(0, 0), new Point(+1, 0), new Point(+1, -1), new Point(0, +2), new Point(+1, +2)},
            {new Point(0, 0), new Point(-1, 0), new Point(-1, +1), new Point(0, -2), new Point(-1, -2)},

            {new Point(0, 0), new Point(+1, 0), new Point(+1, +1), new Point(0, -2), new Point(+1, -2)},
            {new Point(0, 0), new Point(-1, 0), new Point(-1, -1), new Point(0, +2), new Point(-1, +2)},

            {new Point(0, 0), new Point(-1, 0), new Point(-1, -1), new Point(0, +2), new Point(-1, +2)},
            {new Point(0, 0), new Point(+1, 0), new Point(+1, +1), new Point(0, -2), new Point(+1, -2)},

            {},
            {},

            {},
            {},
        },
        {//I Tetromino Wall Kick Data

            {new Point(0, 0), new Point(-2, 0), new Point(+1, 0), new Point(-2, -1), new Point(+1, +2)},
            {new Point(0, 0), new Point(+2, 0), new Point(-1, 0), new Point(+2, +1), new Point(-1, -2)},

            {new Point(0, 0), new Point(-1, 0), new Point(+2, 0), new Point(-1, +2), new Point(+2, -1)},
            {new Point(0, 0), new Point(+1, 0), new Point(-2, 0), new Point(+1, -2), new Point(-2, +1)},

            {new Point(0, 0), new Point(+2, 0), new Point(-1, 0), new Point(+2, +1), new Point(-1, -2)},
            {new Point(0, 0), new Point(-2, 0), new Point(+1, 0), new Point(-2, -1), new Point(+1, +2)},

            {new Point(0, 0), new Point(+1, 0), new Point(-2, 0), new Point(+1, -2), new Point(-2, +1)},
            {new Point(0, 0), new Point(-1, 0), new Point(+2, 0), new Point(-1, +2), new Point(+2, -1)},

            {},
            {},

            {},
            {},
        }
    });
    public static final Kicktable kicktable_srs_guideline_180 = new Kicktable(new Point[][][]{
        {//J, L, S, T, Z Tetromino Wall Kick Data

            {new Point(0, 0), new Point(-1, 0), new Point(-1, +1), new Point(0, -2), new Point(-1, -2)},
            {new Point(0, 0), new Point(+1, 0), new Point(+1, -1), new Point(0, +2), new Point(+1, +2)},

            {new Point(0, 0), new Point(+1, 0), new Point(+1, -1), new Point(0, +2), new Point(+1, +2)},
            {new Point(0, 0), new Point(-1, 0), new Point(-1, +1), new Point(0, -2), new Point(-1, -2)},

            {new Point(0, 0), new Point(+1, 0), new Point(+1, +1), new Point(0, -2), new Point(+1, -2)},
            {new Point(0, 0), new Point(-1, 0), new Point(-1, -1), new Point(0, +2), new Point(-1, +2)},

            {new Point(0, 0), new Point(-1, 0), new Point(-1, -1), new Point(0, +2), new Point(-1, +2)},
            {new Point(0, 0), new Point(+1, 0), new Point(+1, +1), new Point(0, -2), new Point(+1, -2)},

            {new Point(0, 0)},
            {new Point(0, 0)},

            {new Point(0, 0)},
            {new Point(0, 0)},
        },
        {//I Tetromino Wall Kick Data

            {new Point(0, 0), new Point(-2, 0), new Point(+1, 0), new Point(-2, -1), new Point(+1, +2)},
            {new Point(0, 0), new Point(+2, 0), new Point(-1, 0), new Point(+2, +1), new Point(-1, -2)},

            {new Point(0, 0), new Point(-1, 0), new Point(+2, 0), new Point(-1, +2), new Point(+2, -1)},
            {new Point(0, 0), new Point(+1, 0), new Point(-2, 0), new Point(+1, -2), new Point(-2, +1)},

            {new Point(0, 0), new Point(+2, 0), new Point(-1, 0), new Point(+2, +1), new Point(-1, -2)},
            {new Point(0, 0), new Point(-2, 0), new Point(+1, 0), new Point(-2, -1), new Point(+1, +2)},

            {new Point(0, 0), new Point(+1, 0), new Point(-2, 0), new Point(+1, -2), new Point(-2, +1)},
            {new Point(0, 0), new Point(-1, 0), new Point(+2, 0), new Point(-1, +2), new Point(+2, -1)},

            {new Point(0, 0)},
            {new Point(0, 0)},

            {new Point(0, 0)},
            {new Point(0, 0)},
        }
    });

    /*
    Order:
    0R,R0,R2,2R,2L,L2,L0,0L
    02,20,RL,LR
    To disable specific rotation (most commonly 180 rotations),
    don't put any tests and the rotation will fail (compare srs_guideline and srs_guideline_180).
    */
    private final Point[][][] kicks;

    public Kicktable(Point[][][] kicks) {
        this.kicks = kicks;
    }

    public int getX(int piece, int state, int tries) {
        return kicks[piece == 4 ? 1 : 0][state][tries].x;
    }

    public int getY(int piece, int state, int tries) {
        return kicks[piece == 4 ? 1 : 0][state][tries].y;
    }

    public int maxTries(int piece, int state) {
        return kicks[piece == 4 ? 1 : 0][state].length;
    }

}
