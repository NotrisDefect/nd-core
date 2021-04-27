package tetrcore;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

public abstract class GameLogic {
    /*
    abcd abcd
    abc1 xabc
    ab1d xabd
    ab11 xxab
    a1cd xacd
    a1c1 xxac
    a11d xxad
    a111 xxxa
    1bcd xbcd
    1bc1 xxbc
    1b1d xxbd
    1b11 xxxb
    11cd xxcd
    11c1 xxxc
    111d xxxd
    1111 xxxx
     */

    // private static final int MAXIMUMMOVES = 15;
    public static int STAGESIZEX = 10;
    public static int STAGESIZEY = 40;
    public static int PLAYABLEROWS = 20;
    public static int NEXTPIECESMAX = 5;
    private final Point[][][] pieces = Pieces.pieces;
    private final Point[][][] kicktable = Kicktable.kicktable_srs_guideline_180;
    private final int[][] garbagetable = Garbagetable.tetrio;
    private final ArrayList<Integer> garbageQueue = new ArrayList<>();
    private final Property garbage = new Property(4d, 60, 0.05d, 8d);
    private final Property garbageMultiplier = new Property(1d, 30, 0.1d, 8d);
    private final Property gravity = new Property(1d, 30, 0.1d, 20d);
    private final Property lockDelay = new Property(2d, 30, -0.02d, 0.5d);
    private final Piece current = new Piece();
    private final ArrayList<Integer> nextPieces = new ArrayList<>();
    private final int[][] stage = new int[STAGESIZEY][STAGESIZEX];
    public String magicString = "";
    private int magicStringsActive = 0;
    private int garbageHole;
    private double counter = 0;
    private int zoneLines;
    private boolean zone;
    private int heldPiece = -1;
    private boolean held;
    // private int timesMoved = 0;
    private boolean gameover;
    private int combo;
    private int backToBack;
    private long totalScore;
    private long totalLinesCleared;
    private long totalPiecesPlaced;
    private long totalGarbageReceived;
    // cache
    private int currentPieceLowestPossiblePosition;
    private boolean currentPieceHasSpun;
    private boolean currentPieceHasSpunMini;

    private static void debug(String s) {

    }

    private static String intToPieceName(int p) {
        switch (p) {
            case 0:
                return "z";
            case 1:
                return "l";
            case 2:
                return "o";
            case 3:
                return "s";
            case 4:
                return "i";
            case 5:
                return "j";
            case 6:
                return "t";
            case 7:
                return "_";
            case 8:
                return "#";
            case 16:
                return "w";
            default:
                return null;
        }
    }

    private static boolean isOutOfBounds(int x, int y) {
        return y < 0 || STAGESIZEY <= y || x < 0 || STAGESIZEX <= x;
    }

    // turn into event
    public boolean checkTSpin() {
        if (current.getPiece() == 6) {
            boolean[] corners = {true, true, true, true};
            if (!isOutOfBounds(current.getPosition().x, current.getPosition().y)) {
                if (stage[current.getPosition().y][current.getPosition().x] == 7) {
                    corners[0] = false;
                }
            }

            if (!isOutOfBounds(current.getPosition().x + 2, current.getPosition().y)) {
                if (stage[current.getPosition().y][current.getPosition().x + 2] == 7) {
                    corners[1] = false;
                }
            }

            if (!isOutOfBounds(current.getPosition().x, current.getPosition().y + 2)) {
                if (stage[current.getPosition().y + 2][current.getPosition().x] == 7) {
                    corners[2] = false;
                }
            }

            if (!isOutOfBounds(current.getPosition().x + 2, current.getPosition().y + 2)) {
                if (stage[current.getPosition().y + 2][current.getPosition().x + 2] == 7) {
                    corners[3] = false;
                }
            }

            int cornersFilled = 0;
            for (int i = 0; i < 4; i++) {
                if (corners[i]) {
                    cornersFilled++;
                }
            }

            if (cornersFilled >= 3) {

                currentPieceHasSpunMini = true;

                switch (current.getRotation()) {
                    case 0:
                        if (corners[0] && corners[1]) {
                            currentPieceHasSpunMini = false;
                        }
                        break;
                    case 1:
                        if (corners[1] && corners[3]) {
                            currentPieceHasSpunMini = false;
                        }
                        break;
                    case 2:
                        if (corners[3] && corners[2]) {
                            currentPieceHasSpunMini = false;
                        }
                        break;
                    case 3:
                        if (corners[2] && corners[0]) {
                            currentPieceHasSpunMini = false;
                        }
                        break;
                }
                return true;
            }
        }
        return false;
    }

    public boolean collides(int x, int y, int rotation) {
        for (Point point : pieces[current.getPiece()][rotation]) {
            // first we check if the piece is inside borders
            if ((0 <= point.y + y && point.y + y < STAGESIZEY) && (0 <= point.x + x && point.x + x < STAGESIZEX)) {
                // check for the collision with other pieces
                if (stage[point.y + y][point.x + x] != 7) {
                    return true;
                }
            } else {
                return true;
            }
        }
        return false;
    }

    public int getBackToBack() {
        return backToBack;
    }

    public int getCombo() {
        return combo;
    }

    public double getCounter() {
        return counter;
    }

    public Piece getCurrentPiece() {
        return current;
    }

    private void setCurrentPiece(int piece) {
        current.setPiece(piece);
    }

    public int getCurrentPieceInt() {
        return current.getPiece();
    }

    public int getCurrentPieceLowestPossiblePosition() {
        return currentPieceLowestPossiblePosition;
    }

    public Point getCurrentPiecePosition() {
        return current.getPosition();
    }

    public int getCurrentPieceRotation() {
        return current.getRotation();
    }

    public boolean getGameover() {
        return gameover;
    }

    public void setGameover(boolean gameover) {
        this.gameover = gameover;
    }

    public int getGarbageHole() {
        return garbageHole;
    }

    public ArrayList<Integer> getGarbageQueue() {
        return garbageQueue;
    }

    public int[][] getGarbagetable() {
        return garbagetable;
    }

    public int getHeldPiece() {
        return heldPiece;
    }

    public String getMagicString() {
        return magicString;
    }

    private void setMagicString(String s) {
        new Thread() {
            @Override
            public void run() {
                magicStringsActive++;
                magicString = s + " " + combo + " combo";
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (magicStringsActive == 1) {
                    magicString = "";
                }
                magicStringsActive--;
                this.interrupt();
            }
        }.start();
    }

    public int getMagicStringsActive() {
        return magicStringsActive;
    }

    public ArrayList<Integer> getNextPieces() {
        return nextPieces;
    }

    public Point[] getPiece(int piece, int rotation) {
        return pieces[piece][rotation];
    }

    public long getScore() {
        return totalScore;
    }

    public int[][] getStage() {
        return stage;
    }

    public long getTotalGarbageReceived() {
        return totalGarbageReceived;
    }

    public long getTotalLinesCleared() {
        return totalLinesCleared;
    }

    public long getTotalPiecesPlaced() {
        return totalPiecesPlaced;
    }

    public int getZonelines() {
        return zoneLines;
    }

    public void hardDropPiece() {
        int lines = 0;
        while (!collides(current.getPosition().x, current.getPosition().y + lines + 1, current.getRotation())) {
            lines++;
        }
        if (lines > 0) {
            movePieceRelative(0, +lines);
            totalScore += lines * 2L;
        }
        lockPiece();
    }

    public boolean holdPiece() {
        if (!held) {
            int temp;

            // if first hold
            if (heldPiece == -1) {
                heldPiece = current.getPiece();
                makeNextPiece();
            } else {
                // swap
                temp = current.getPiece();
                setCurrentPiece(heldPiece);
                heldPiece = temp;

                current.setPosition(new Point(3, 17));
                current.setRotation(0);

                topOutCheck();
            }
            calculateCurrentPieceLowestPossiblePosition();
            held = true;
            return true;
        } else {
            return false;
        }
    }

    public void initGame() {
        for (int i = 0; i < STAGESIZEY; i++) {
            for (int j = 0; j < STAGESIZEX; j++) {
                stage[i][j] = 7;
            }
        }
        gameover = false;
        nextPieces.clear();
        heldPiece = -1;
        held = false;
        totalScore = 0;
        combo = -1;
        backToBack = -1;

        totalLinesCleared = 0;
        totalPiecesPlaced = 0;
        totalGarbageReceived = 0;

        zone = false;
        zoneLines = 0;

        garbageQueue.clear();
        garbageHole = (int) (Math.random() * STAGESIZEX);

        magicString = "";
        makeNextPiece();
        gameLoop();
    }

    public boolean movePiece(int newX, int newY, int newR) {
        if (!collides(newX, newY, newR)) {
            counter = 0;
            totalScore += Math.max(0, newY - current.getPosition().y);
            current.setPosition(new Point(newX, newY));
            current.setRotation(newR);
            currentPieceHasSpun = false;
            calculateCurrentPieceLowestPossiblePosition();
            return true;
        }
        return false;
    }

    public boolean movePieceRelative(int xOffset, int yOffset) {
        return movePiece(current.getPosition().x + xOffset, current.getPosition().y + yOffset, current.getRotation());
    }

    public void receiveGarbage(int n) {
        garbageQueue.add(n);
    }

    public boolean rotatePiece(int d) {
        int newRotation = (current.getRotation() + d + 4) % 4;

        int special = -1;

        if (current.getRotation() == 0 && newRotation == 1) {
            special = 0;
        } else if (current.getRotation() == 1 && newRotation == 0) {
            special = 1;
        } else if (current.getRotation() == 1 && newRotation == 2) {
            special = 2;
        } else if (current.getRotation() == 2 && newRotation == 1) {
            special = 3;
        } else if (current.getRotation() == 2 && newRotation == 3) {
            special = 4;
        } else if (current.getRotation() == 3 && newRotation == 2) {
            special = 5;
        } else if (current.getRotation() == 3 && newRotation == 0) {
            special = 6;
        } else if (current.getRotation() == 0 && newRotation == 3) {
            special = 7;
        } else if (current.getRotation() == 0 && newRotation == 2) {
            special = 8;
        } else if (current.getRotation() == 2 && newRotation == 0) {
            special = 9;
        } else if (current.getRotation() == 1 && newRotation == 3) {
            special = 10;
        } else if (current.getRotation() == 3 && newRotation == 1) {
            special = 11;
        }

        int pieceType = current.getPiece() == 4 ? 1 : 0;

        int maxtries = kicktable[pieceType][special].length;

        for (int tries = 0; tries < maxtries; tries++) {
            if (movePiece(current.getPosition().x + kicktable[pieceType][special][tries].x,
                    current.getPosition().y - kicktable[pieceType][special][tries].y, newRotation)) {
                return true;
            }
        }

        return false;
    }

    @Deprecated
    public void settSpin(boolean b) {
        currentPieceHasSpun = b;
    }

    public void sonicDrop() {
        int num = 0;
        Point position = current.position;
        while (!collides(position.x, position.y + num + 1, current.getRotation())) {
            num++;
        }
        movePieceRelative(0, num);
    }

    public void startZone() {
        zone = true;
        magicString = "Zone activated";
    }

    public abstract void whatWhenSendGarbage(int n);

    public void onLineClearEvent(int row, int[] content) {

    }

    private void calculateCurrentPieceLowestPossiblePosition() {
        Point position = current.getPosition();
        int result = position.y;
        while (!collides(position.x, result + 1, current.getRotation())) {
            result++;
        }
        currentPieceLowestPossiblePosition = result;
    }

    private void clearLine(int line) {

        onLineClearEvent(line, stage[line].clone());

        for (int i = line; i > 0; i--) {
            if (STAGESIZEX >= 0) System.arraycopy(stage[i - 1], 0, stage[i], 0, STAGESIZEX);
        }

        for (int j = 0; j < STAGESIZEX; j++) {
            stage[0][j] = 7;
        }

        totalLinesCleared++;

    }

    private int clearLines() {
        int numClears = 0;
        boolean yes;
        for (int i = STAGESIZEY - 1; i > 0; i--) {
            yes = true;
            for (int j = 0; j < STAGESIZEX; j++) {
                if (stage[i][j] == 7) {
                    yes = false;
                    break;
                }
            }
            if (yes) {
                clearLine(i);
                i++;
                numClears++;
            }
        }

        return numClears;
    }

    private void clearLinesZone() {

        boolean yes;
        for (int i = STAGESIZEY - 1 - zoneLines; i > 0; i--) {
            yes = true;
            for (int j = 0; j < STAGESIZEX; j++) {
                if (stage[i][j] == 7) {
                    yes = false;
                    break;
                }
            }
            if (yes) {
                zoneLines++;
                clearLineZone(i);
            }
        }
    }

    private void clearLineZone(int line) {
        int gap = STAGESIZEY - (line + zoneLines);
        for (int i = line; i < line + gap; i++) {
            if (STAGESIZEX >= 0) System.arraycopy(stage[i + 1], 0, stage[i], 0, STAGESIZEX);
        }
        for (int j = 0; j < STAGESIZEX; j++) {
            stage[STAGESIZEY - zoneLines][j] = 16;
        }

        magicString = zoneLines + " LINES";
    }

    private int clearTypeToInt(ClearType c) {
        switch (c) {
            case SINGLE:
                return 0;
            case DOUBLE:
                return 1;
            case TRIPLE:
                return 2;
            case QUAD:
                return 3;
            case SPIN_MINI_SINGLE:
                return 4;
            case SPIN_SINGLE:
                return 5;
            case SPIN_MINI_DOUBLE:
                return 6;
            case SPIN_DOUBLE:
                return 7;
            case SPIN_TRIPLE:
                return 8;
            case SPIN_QUAD:
                return 9;
            default:
                return -1;
        }
    }

    private void gameLoop() {

        new Thread(() -> {
            while (!gameover) {
                // long timeStart = System.nanoTime();
                if (counter >= (isTouchingGround() ? lockDelay.getWorkingValue() * 1000
                        : (Math.pow(gravity.getWorkingValue(), -1) * 1000))) {
                    if (!movePieceRelative(0, +1)) {
                        lockPiece();
                    }
                }

                counter += 10;

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                // long timeEnd = System.nanoTime();

                // long timeElapsed = timeEnd - timeStart;
                // System.out.println((float)timeElapsed/1000000 + " ms");
            }
        }).start();

        gravity.start();
        garbage.start();
        garbageMultiplier.start();
        lockDelay.start();
    }

    private ClearType getClearType(int lines) {
        switch (lines) {
            case 1:
                if (currentPieceHasSpun) {
                    if (currentPieceHasSpunMini) {
                        return ClearType.SPIN_MINI_SINGLE;
                    } else {
                        return ClearType.SPIN_SINGLE;
                    }
                } else {
                    return ClearType.SINGLE;
                }
            case 2:
                if (currentPieceHasSpun) {
                    if (currentPieceHasSpunMini) {
                        return ClearType.SPIN_MINI_DOUBLE;
                    } else {
                        return ClearType.SPIN_DOUBLE;
                    }
                } else {
                    return ClearType.DOUBLE;
                }
            case 3:
                if (currentPieceHasSpun) {
                    if (currentPieceHasSpunMini) {
                        return null;
                    } else {
                        return ClearType.SPIN_TRIPLE;
                    }
                } else {
                    return ClearType.TRIPLE;
                }
            case 4:
                if (currentPieceHasSpun) {
                    if (currentPieceHasSpunMini) {
                        return null;
                    } else {
                        return ClearType.SPIN_QUAD;
                    }
                } else {
                    return ClearType.QUAD;
                }
            default:
                return null;
        }
    }

    private boolean isTouchingGround() {
        return collides(current.getPosition().x, current.getPosition().y + 1, current.getRotation());
    }

    private void lockPiece() {

        totalPiecesPlaced++;

        for (Point point : pieces[current.getPiece()][current.getRotation()]) {
            stage[current.getPosition().y + point.y][current.getPosition().x + point.x] = current.getPiece();
        }

        // check for too high placement
        int fails = 0;
        for (Point point : pieces[current.getPiece()][current.getRotation()]) {
            if (current.getPosition().y + point.y >= STAGESIZEY - PLAYABLEROWS) {
                break;
            } else {
                fails++;
            }
        }
        if (fails == 4) {
            gameover = true;
        }
        ///////////////////

        if (zone) {
            clearLinesZone();
        } else {
            int linesCleared = clearLines();

            if (linesCleared > 0) {
                combo++;

                if ((totalLinesCleared - totalGarbageReceived) * STAGESIZEX + totalGarbageReceived == totalPiecesPlaced
                        * 4) {

                    sendGarbage((int) ((garbagetable[clearTypeToInt(getClearType(linesCleared))][combo] + 10)
                            * garbageMultiplier.getWorkingValue()));
                } else {
                    sendGarbage((int) (garbagetable[clearTypeToInt(getClearType(linesCleared))][combo]
                            * garbageMultiplier.getWorkingValue()));
                }

                setMagicString((intToPieceName(current.getPiece()) + "-" + getClearType(linesCleared).getDescription())
                        .toUpperCase());

                totalScore += getClearType(linesCleared).getScore();
            } else {
                combo = -1;
                tryToPutGarbage();
            }

            debug("tspin=" + currentPieceHasSpun + ";combo=" + combo + ";linescleared=" + linesCleared);

        }

        makeNextPiece();
    }

    private void makeNextPiece() {
        if (nextPieces.size() <= 7) {
            ArrayList<Integer> bag = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                bag.add(i);
            }
            Collections.shuffle(bag);
            nextPieces.addAll(bag);
        }

        spawnPiece();

        topOutCheck();
    }

    private void putGarbageLine() {
        for (int i = 0; i < STAGESIZEY - 1; i++) {
            if (STAGESIZEX >= 0) System.arraycopy(stage[i + 1], 0, stage[i], 0, STAGESIZEX);
        }
        for (int j = 0; j < STAGESIZEX; j++) {
            if (j == garbageHole) {
                stage[STAGESIZEY - 1][j] = 7;
            } else {
                stage[STAGESIZEY - 1][j] = 8;
            }
        }

        totalGarbageReceived++;
    }

    private void sendGarbage(int n) {
        int garbageRemaining = n;
        while (!garbageQueue.isEmpty() && garbageRemaining > 0) {
            garbageQueue.set(0, garbageQueue.get(0) - 1);
            if (garbageQueue.get(0) == 0) {
                garbageQueue.remove(0);
                garbageHole = (int) (Math.random() * STAGESIZEX);
            }
            garbageRemaining--;
        }

        if (garbageRemaining > 0) {
            whatWhenSendGarbage(n);
        }
    }

    private void spawnPiece() {
        current.setPosition(new Point(3, 17));
        current.setRotation(0);
        current.setPiece(nextPieces.get(0));
        nextPieces.remove(0);
        held = false;
        currentPieceHasSpun = false;
        calculateCurrentPieceLowestPossiblePosition();
    }

    private void stopZone() {
        zone = false;
        for (int i = 0; i < STAGESIZEY; i++) {
            for (int j = 0; j < STAGESIZEX; j++) {
                if (STAGESIZEY - zoneLines - 1 - i >= 0) {
                    stage[STAGESIZEY - 1 - i][j] = stage[STAGESIZEY - zoneLines - 1 - i][j];
                } else {
                    stage[STAGESIZEY - 1 - i][j] = 7;
                }
            }
        }
        zoneLines = 0;
    }

    private boolean topOutCheck() {
        for (Point point : pieces[current.getPiece()][current.getRotation()]) {
            if (stage[point.y + current.getPosition().y][point.x + current.getPosition().x] != 7) {
                if (zone) {
                    stopZone();
                } else {
                    gameover = true;
                }
                return true;
            }
        }
        return false;
    }

    private void tryToPutGarbage() {
        for (int h = 0; h < garbage.getWorkingValue(); h++) {
            if (!garbageQueue.isEmpty()) {
                putGarbageLine();

                garbageQueue.set(0, garbageQueue.get(0) - 1);
                if (garbageQueue.get(0) == 0) {
                    garbageQueue.remove(0);
                    garbageHole = (int) (Math.random() * STAGESIZEX);
                }
            }
        }
    }

    private enum ClearType {
        SINGLE(100, "Single"),
        DOUBLE(300, "Double"),
        TRIPLE(500, "Triple"),
        QUAD(800, "Quad"),
        SPIN_MINI(100, "Spin Mini"),
        SPIN(400, "Spin"),
        SPIN_MINI_SINGLE(200, "Spin Mini Single"),
        SPIN_SINGLE(800, "Spin Single"),
        SPIN_MINI_DOUBLE(400, "Spin Mini Double"),
        SPIN_DOUBLE(1200, "Spin Double"),
        SPIN_TRIPLE(1600, "Spin Triple"),
        SPIN_QUAD(2600, "Spin Quad"),
        COMBO(50),
        ALL_CLEAR(3500, "All Clear"),
        SD(1),
        HD(2);

        private final int score;
        private final String description;

        ClearType(int score) {
            this.score = score;
            this.description = null;
        }

        ClearType(int score, String description) {
            this.score = score;
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public int getScore() {
            return score;
        }

    }

    private static class Piece {
        private int piece;
        private Point position;
        private int rotation;

        public Piece() {

        }

        @SuppressWarnings("unused")
        public Piece(int piece, Point position) {
            this.piece = piece;
            this.position = position;
            this.rotation = 0;
        }

        @SuppressWarnings("unused")
        public Piece(int piece, Point position, int rotation) {
            this.piece = piece;
            this.position = position;
            this.rotation = rotation;
        }

        public int getPiece() {
            return piece;
        }

        public void setPiece(int piece) {
            this.piece = piece;
        }

        public Point getPosition() {
            return position;
        }

        public void setPosition(Point position) {
            this.position = position;
        }

        public int getRotation() {
            return rotation;
        }

        public void setRotation(int rotation) {
            this.rotation = rotation;
        }
    }

    private static class Property {
        private final double base;
        private final int delay;
        private final double delta;
        private final boolean mode;
        private final double limit;

        private double workingValue;

        private Property(double base, int delay, double delta, double limit) {
            this.base = base;
            this.delay = delay;
            this.delta = delta;
            mode = delta > 0;
            this.limit = limit;
        }

        private int getIncreaseDelay() {
            return delay;
        }

        private double getLimit() {
            return limit;
        }

        private double getWorkingValue() {
            return workingValue;
        }

        private void start() {
            workingValue = base;

            Timer timer = new Timer();

            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    new Thread(() -> {
                        while (mode ? (getWorkingValue() < getLimit()) : (getLimit() < getWorkingValue())) {
                            tick();
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        workingValue = limit;
                    }).start();
                }
            };

            timer.schedule(task, getIncreaseDelay() * 1000L);
        }

        private void tick() {
            workingValue += delta;
        }

    }
}
