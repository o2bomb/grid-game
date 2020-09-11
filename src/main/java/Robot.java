import java.util.concurrent.ThreadLocalRandom;

import javafx.application.Platform;

public class Robot implements Runnable {
    private int id;
    private int x;
    private int y;
    private long movementDelay;
    private Grid grid; // reference to Grid object

    public Robot(int id, int x, int y, Grid grid) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.movementDelay = ThreadLocalRandom.current().nextInt(500, 2000 + 1);
        this.grid = grid;
    }

    @Override
    public void run() {
        // update GUI's logger to indicate that this robot has spawned
        Platform.runLater(() -> {

        });
        System.out.println(String.format("Robot #%d has spawned at [%d, %d]", id, x, y));
        try {
            while (true) {
                try {
                    System.out.println(String.format("(Robot #%d) is at [%d, %d]", id, x, y));
                    // attempt to move to a different square
                    attemptMove();
                    // perform animation of moving robot in GUI
                    Platform.runLater(() -> {
                        
                    });
                } catch (AlreadyOccupiedException e) {
                    // square is already occupied; do nothing
                    System.out.println(String.format("(Robot #%d) a clash has occurred when trying to move from [%d, %d]", id, x, y));
                } catch (RobotMismatchException e) {
                    // square cannot be cleared right now; do nothing
                    System.out.println(String.format("(Robot #%d) failed to remove robot from [%d, %d]", id, x, y));
                }
                Thread.sleep(movementDelay);
            }
        } catch (InterruptedException e) {
            System.out.println(String.format("Robot #%d has exited", id));
        }
    }

    private void attemptMove() throws AlreadyOccupiedException, RobotMismatchException {
        int newX = x + ThreadLocalRandom.current().nextInt(-1, 1 + 1); // randomly increment/decrement x by 1
        int newY = y + ThreadLocalRandom.current().nextInt(-1, 1 + 1); // randomly increment/decrement y by 1

        GridSquare currSquare = grid.getGridSquare(x, y);
        GridSquare square = grid.getGridSquare(newX, newY);

        // clear the previous square's robot
        currSquare.clearRobot(this);
        // update square's occupying robot
        square.setRobot(this);
        // update robots position
        x = square.getX();
        y = square.getY();
    }

    public int getId() {
        return this.id;
    }
}
