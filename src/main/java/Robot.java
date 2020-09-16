import java.util.concurrent.ThreadLocalRandom;

import javafx.application.Platform;

public class Robot implements Runnable {
    // ROBOT DATA
    private int id;
    private int x;
    private int y;
    private long movementDelay;
    private Grid grid = ThreadController.getInstance().getGrid(); // reference to Grid object
    private Player player = ThreadController.getInstance().getPlayer(); // reference to Player object

    // UI STUFF
    UIElements ui = UIElements.getInstance();
    private double transitionX; // for animation
    private double transitionY; // for animation

    // THREADING STUFF
    Object monitor = new Object();
    Thread robotThread = null;

    public Robot(int id, int x, int y) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.movementDelay = ThreadLocalRandom.current().nextInt(500, 2000 + 1);

        this.transitionX = (double)x;
        this.transitionY = (double)y;
    }

    public int getId() {
        return this.id;
    }

    public long getMovementDelay() {
        return movementDelay;
    }

    public double getTransitionX() {
        synchronized(monitor) {
            return transitionX;
        }
    }

    public double getTransitionY() {
        synchronized(monitor) {
            return transitionY;
        }
    }

    @Override
    public void run() {
        // store reference to this robot's thread
        robotThread = Thread.currentThread();
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
                    // animate it
                    Platform.runLater(() -> {
                        ui.getArena().updateRobotPositions();
                    });
                    // check for win condition
                    checkIfPlayer();
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
                ui.getLogger().appendText(String.format("Robot #%d has been destroyed at [%d, %d]\n", id, x, y));
            });
            System.out.println(String.format("Robot #%d has exited", id));
        }
    }

    /**
     * Removes this Robot from the Grid and also destroys 
     * the thread that posseses this Robot instance
     * @param x
     * @param y
     * @throws RobotMismatchException
     */
    public void destroy(int x, int y) throws RobotMismatchException {
        synchronized(monitor) {
            GridSquare targetSquare = grid.getGridSquare(x, y);
            GridSquare currSquare = grid.getGridSquare(this.x, this.y);
    
            // clear robot from its currently occupied squares
            targetSquare.clearRobot(this);
            currSquare.clearRobot(this);
            // then end the robot's thread
            if (robotThread != null) {
                robotThread.interrupt();
            }
        }
    }

    /**
     * This method attemps to move the robot from one GridSquare, to another randomly
     * chosen adjacent GridSquare
     * @throws AlreadyOccupiedException If the square is already occupied
     * @throws RobotMismatchException
     */
    private void attemptMove() throws AlreadyOccupiedException, RobotMismatchException {
        synchronized(monitor) {
            GridSquare currSquare = grid.getGridSquare(x, y);
            GridSquare newSquare = grid.getRandomAdjacentGridSquare(x, y);
    
            System.out.println(String.format("(Robot #%d) attempting move to [%d, %d]", id, newSquare.getX(), newSquare.getY()));
            // update new square's occupying robot
            newSquare.setRobot(this);
            // clear the previous square's robot
            currSquare.clearRobot(this);
            // success! now animate the robot on the gui thread
            // animateMove(currSquare.getX(), currSquare.getY(), newSquare.getX(), newSquare.getY());
            // update robots position
            x = newSquare.getX();
            y = newSquare.getY();
            transitionX = newSquare.getX();
            transitionY = newSquare.getY();
        }
    }

    /**
     * Check's if the current GridSquare is the player's fortress.
     * If it is, print out something to the GUI to indicate that
     */
    private void checkIfPlayer() {
        synchronized(monitor) {
            GridSquare square = grid.getGridSquare(x, y);
            if (square instanceof PlayerSquare) {
                Platform.runLater(() -> {
                    ui.getLogger().appendText(String.format("Robot #%d has landed on the fortress!\n", id));
                });
                player.end();
            }
        }
    }

    /**
     * THIS METHOD DOES NOT WORK
     * Animates the robot's movement so that its transition between
     * two grid squares is smooth
     * @param initX
     * @param initY
     * @param finalX
     * @param finalY
     */
    private void animateMove(double initX, double initY, double finalX, double finalY) {
        long startTime = System.currentTimeMillis();
        long lastTime = System.currentTimeMillis();
        long transitionTime = getMovementDelay();

        double differenceX = finalX - initX;
        double differenceY = finalY - initY;
        double incrementX = differenceX / ((double)transitionTime / 1000.0);
        double incrementY = differenceY / ((double)transitionTime / 1000.0);

        final double ms = 1000000.0 / 20.0; // 20 fps
        double delta = 0.0;
        while(System.currentTimeMillis() - startTime < transitionTime){
            long now = System.currentTimeMillis();
            delta += (now - lastTime) / ms;
            lastTime = now;
            while(delta >= 1){
                System.out.println(String.format("animating: [%d, %d]", transitionX, transitionY));
                transitionX = initX + incrementX * ((double)(now - lastTime) / 1000.0);
                transitionY = initY + incrementY * ((double)(now - lastTime) / 1000.0);

                Platform.runLater(() -> {
                    ui.getArena().updateRobotPositions();
                });
            }
        }
    }
}
