import java.util.concurrent.ThreadLocalRandom;

import javafx.application.Platform;

public class Robot implements Runnable {
    // ROBOT DATA
    private int id;
    private int x;
    private int y;
    private long movementDelay;
    private Grid grid; // reference to Grid object

    // UI STUFF
    UIElements ui = UIElements.getInstance();

    // THREADING STUFF
    Object monitor = new Object();

    public Robot(int id, int x, int y, Grid grid) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.movementDelay = ThreadLocalRandom.current().nextInt(500, 2000 + 1);
        this.grid = grid;
    }

    public int getId() {
        return this.id;
    }

    public int getX() {
        synchronized(monitor) {
            return this.x;
        }
    }

    public int getY() {
        synchronized(monitor) {
            return this.y;
        }
    }

    @Override
    public void run() {
        // update GUI's logger to indicate that this robot has spawned
        Platform.runLater(() -> {
            ui.getLogger().appendText(String.format("Robot #%d has spawned at [%d, %d]\n", id, x, y));
        });
        System.out.println(String.format("Robot #%d has spawned at [%d, %d]", id, x, y));
        try {
            while (true) {
                try {
                    System.out.println(String.format("(Robot #%d) is at [%d, %d]", id, x, y));
                    // attempt to move to a different square
                    attemptMove();
                    // request GUI to update all robot positions
                    synchronized(monitor) {
                        Platform.runLater(() -> {
                            ui.getArena().updateRobotPositions();
                        });
                    }
                } catch (AlreadyOccupiedException e) {
                    // square is already occupied; do nothing
                    // System.out.println(String.format("(Robot #%d) a clash has occurred when trying to move from [%d, %d]", id, x, y));
                } catch (RobotMismatchException e) {
                    // square cannot be cleared right now; do nothing
                    // System.out.println(String.format("(Robot #%d) failed to remove robot from [%d, %d]", id, x, y));
                }
                Thread.sleep(movementDelay);
            }
        } catch (InterruptedException e) {
            Platform.runLater(() -> {
                ui.getLogger().appendText(String.format("Robot #%d has been destroyed\n", id));
            });
            System.out.println(String.format("Robot #%d has exited", id));
        }
    }

    private void attemptMove() throws AlreadyOccupiedException, RobotMismatchException {
        synchronized(monitor) {
            GridSquare currSquare = grid.getGridSquare(x, y);
            GridSquare newSquare = grid.getRandomAdjacentGridSquare(x, y);
    
            System.out.println(String.format("(Robot #%d) attempting move to [%d, %d]", id, newSquare.getX(), newSquare.getY()));
            // update new square's occupying robot
            newSquare.setRobot(this);
            // clear the previous square's robot
            currSquare.clearRobot(this);
            // update robots position
            x = newSquare.getX();
            y = newSquare.getY();
        }
    }
}
