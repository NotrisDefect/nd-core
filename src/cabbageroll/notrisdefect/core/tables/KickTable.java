package cabbageroll.notrisdefect.core.tables;

import cabbageroll.notrisdefect.core.Point;

public class KickTable {

    // JLSTZ
    // I
    // 0R,R0,R2,2R,2L,L2,L0,0L,02,20,RL,LR,00

    public static final KickTable SRS_PLUS = new KickTable(new int[][][][]{
        {
            {{0, 0}, {-1, 0}, {-1, +1}, {0, -2}, {-1, -2}},
            {{0, 0}, {+1, 0}, {+1, -1}, {0, +2}, {+1, +2}},

            {{0, 0}, {+1, 0}, {+1, -1}, {0, +2}, {+1, +2}},
            {{0, 0}, {-1, 0}, {-1, +1}, {0, -2}, {-1, -2}},

            {{0, 0}, {+1, 0}, {+1, +1}, {0, -2}, {+1, -2}},
            {{0, 0}, {-1, 0}, {-1, -1}, {0, +2}, {-1, +2}},

            {{0, 0}, {-1, 0}, {-1, -1}, {0, +2}, {-1, +2}},
            {{0, 0}, {+1, 0}, {+1, +1}, {0, -2}, {+1, -2}},

            {{0, 0}, {0, +1}, {+1, +1}, {-1, +1}, {+1, 0}, {-1, 0}},
            {{0, 0}, {0, -1}, {-1, -1}, {+1, -1}, {-1, 0}, {+1, 0}},

            {{0, 0}, {+1, 0}, {+1, +2}, {+1, +1}, {0, +2}, {0, +1}},
            {{0, 0}, {-1, 0}, {-1, +2}, {-1, +1}, {0, +2}, {0, +1}},

            {{0, 0}}
        },
        {
            {{0, 0}, {+1, 0}, {-2, 0}, {-2, -1}, {+1, +2}},
            {{0, 0}, {-1, 0}, {+2, 0}, {-1, -2}, {+2, +1}},

            {{0, 0}, {-1, 0}, {+2, 0}, {-1, +2}, {+2, -1}},
            {{0, 0}, {-2, 0}, {+1, 0}, {-2, +1}, {+1, -2}},

            {{0, 0}, {+2, 0}, {-1, 0}, {+2, +1}, {-1, -2}},
            {{0, 0}, {+1, 0}, {-2, 0}, {+1, +2}, {-2, -1}},

            {{0, 0}, {+1, 0}, {-2, 0}, {+1, -2}, {-2, +1}},
            {{0, 0}, {-1, 0}, {+2, 0}, {+2, -1}, {-1, +2}},

            {{0, 0}, {0, +1}},
            {{0, 0}, {0, -1}},

            {{0, 0}, {+1, 0}},
            {{0, 0}, {-1, 0}},

            {{0, 0}}
        }
    });

    public static final KickTable SRS = new KickTable(new int[][][][]{
        {
            {{0, 0}, {-1, 0}, {-1, +1}, {0, -2}, {-1, -2}},
            {{0, 0}, {+1, 0}, {+1, -1}, {0, +2}, {+1, +2}},

            {{0, 0}, {+1, 0}, {+1, -1}, {0, +2}, {+1, +2}},
            {{0, 0}, {-1, 0}, {-1, +1}, {0, -2}, {-1, -2}},

            {{0, 0}, {+1, 0}, {+1, +1}, {0, -2}, {+1, -2}},
            {{0, 0}, {-1, 0}, {-1, -1}, {0, +2}, {-1, +2}},

            {{0, 0}, {-1, 0}, {-1, -1}, {0, +2}, {-1, +2}},
            {{0, 0}, {+1, 0}, {+1, +1}, {0, -2}, {+1, -2}},

            {},
            {},

            {},
            {},

            {{0, 0}}
        },
        {
            {{0, 0}, {-2, 0}, {+1, 0}, {-2, -1}, {+1, +2}},
            {{0, 0}, {+2, 0}, {-1, 0}, {+2, +1}, {-1, -2}},

            {{0, 0}, {-1, 0}, {+2, 0}, {-1, +2}, {+2, -1}},
            {{0, 0}, {+1, 0}, {-2, 0}, {+1, -2}, {-2, +1}},

            {{0, 0}, {+2, 0}, {-1, 0}, {+2, +1}, {-1, -2}},
            {{0, 0}, {-2, 0}, {+1, 0}, {-2, -1}, {+1, +2}},

            {{0, 0}, {+1, 0}, {-2, 0}, {+1, -2}, {-2, +1}},
            {{0, 0}, {-1, 0}, {+2, 0}, {-1, +2}, {+2, -1}},

            {},
            {},

            {},
            {},

            {{0, 0}}
        }
    });

    public static final KickTable SRS_180 = new KickTable(new int[][][][]{
        {
            {{0, 0}, {-1, 0}, {-1, +1}, {0, -2}, {-1, -2}},
            {{0, 0}, {+1, 0}, {+1, -1}, {0, +2}, {+1, +2}},

            {{0, 0}, {+1, 0}, {+1, -1}, {0, +2}, {+1, +2}},
            {{0, 0}, {-1, 0}, {-1, +1}, {0, -2}, {-1, -2}},

            {{0, 0}, {+1, 0}, {+1, +1}, {0, -2}, {+1, -2}},
            {{0, 0}, {-1, 0}, {-1, -1}, {0, +2}, {-1, +2}},

            {{0, 0}, {-1, 0}, {-1, -1}, {0, +2}, {-1, +2}},
            {{0, 0}, {+1, 0}, {+1, +1}, {0, -2}, {+1, -2}},

            {{0, 0}},
            {{0, 0}},

            {{0, 0}},
            {{0, 0}},

            {{0, 0}}
        },
        {
            {{0, 0}, {-2, 0}, {+1, 0}, {-2, -1}, {+1, +2}},
            {{0, 0}, {+2, 0}, {-1, 0}, {+2, +1}, {-1, -2}},

            {{0, 0}, {-1, 0}, {+2, 0}, {-1, +2}, {+2, -1}},
            {{0, 0}, {+1, 0}, {-2, 0}, {+1, -2}, {-2, +1}},

            {{0, 0}, {+2, 0}, {-1, 0}, {+2, +1}, {-1, -2}},
            {{0, 0}, {-2, 0}, {+1, 0}, {-2, -1}, {+1, +2}},

            {{0, 0}, {+1, 0}, {-2, 0}, {+1, -2}, {-2, +1}},
            {{0, 0}, {-1, 0}, {+2, 0}, {-1, +2}, {+2, -1}},

            {{0, 0}},
            {{0, 0}},

            {{0, 0}},
            {{0, 0}},

            {{0, 0}}
        }
    });

    public static final KickTable LUMINES = new KickTable(new int[][][][]{
        {
            {{0, 0}},
            {{0, 0}},

            {{0, 0}},
            {{0, 0}},

            {{0, 0}},
            {{0, 0}},

            {{0, 0}},
            {{0, 0}},

            {},
            {},

            {},
            {},

            {{0, 0}}
        },
        {
            {{0, 0}},
            {{0, 0}},

            {{0, 0}},
            {{0, 0}},

            {{0, 0}},
            {{0, 0}},

            {{0, 0}},
            {{0, 0}},

            {},
            {},

            {},
            {},

            {{0, 0}}
        }
    });

    public static final KickTable NO_ROTATION = new KickTable(new int[][][][]{
        {
            {},
            {},

            {},
            {},

            {},
            {},

            {},
            {},

            {},
            {},

            {},
            {},

            {}
        },
        {
            {},
            {},

            {},
            {},

            {},
            {},

            {},
            {},

            {},
            {},

            {},
            {},

            {}
        }
    });

    private final Point[][][] kicks;

    private static final int[][] SPINSTATES = {
        {12, 0, 8, 7},
        {1, 12, 2, 10},
        {9, 3, 12, 4},
        {6, 11, 5, 12},
    };

    public KickTable(int[][][][] kicks) {
        this.kicks = new Point[2][13][];
        for (int i = 0; i < kicks.length; i++) {
            int[][][] piece = kicks[i];
            for (int j = 0; j < piece.length; j++) {
                int[][] state = piece[j];
                this.kicks[i][j] = new Point[state.length];
                for (int k = 0; k < state.length; k++) {
                    int[] offset = state[k];
                    this.kicks[i][j][k] = new Point(offset[0], offset[1]);
                }
            }
        }
    }

    public int getState(int oldRotation, int newRotation) {
        return SPINSTATES[oldRotation][newRotation];
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
