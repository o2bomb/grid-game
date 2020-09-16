import java.util.List;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;

public class Grid implements Runnable {
    // GRID DATA
    private int length;
    private int height;
    private long spawnDelay;
    private GridSquare[][] grid;
    private static int robotCounter = 1;
    private List<Robot> robots = new LinkedList<>();

    // THREADING STUFF
    private ExecutorService es = new ThreadPoolExecutor(2, 2, 4, TimeUnit.SECONDS, new SynchronousQueue<>());
    private List<Future<?>> robotTasks = new LinkedList<>();
    private Object monitor = new Object();

    public Grid() {
        this.length = 9;
        this.height = 9;
        this.spawnDelay = 2000;
        this.grid = new GridSquare[length][height];

        for (int i = 0; i < length; i++) {
            for (int j = 0; j < height; j++) {
                if (i == length / 2 &&  j == height / 2) {
                    grid[i][j] = new PlayerSquare(i, j);
                } else {
                    grid[i][j] = new GridSquare(i, j);
                }
            }
        }
    }

    public Grid(int length, int height, int spawnDelay) {
        this.length = length;
        this.height = height;
        this.spawnDelay = spawnDelay;
        this.grid = new GridSquare[height][length];

        for (int i = 0; i < length; i++) {
            for (int j = 0; j < height; j++) {
                if (i == length / 2 &&  j == height / 2) {
                    grid[i][j] = new PlayerSquare(i, j);
                } else {
                    grid[i][j] = new GridSquare(i, j);
                }
            }
        }
    }

    @Override
    public void run() {
        System.out.println("Grid thread created");
        try {
            while(true) {
                // attempt to spawn robot
                attemptSpawn();
                Thread.sleep(spawnDelay);
            }
        } catch (InterruptedException e) {
            System.out.println("Grid thread has exited");
        }
    }

    public int getLength() {
        return length;
    }

    public int getHeight() {
        return height;
    }

    /**
     * Returns a copy of the robots list
     */
    public List<Robot> getRobots() {
        synchronized(monitor) {
            return Collections.unmodifiableList(robots);
        }
    }
    
    /**
     * Returns the GridSquare object at (x, y) coordinates
     * @param x
     * @param y
     * @return A GridSquare object
     */
    public GridSquare getGridSquare(int x, int y) {
        synchronized(monitor) {
            if (x < 0) x = 0;
            if (y < 0) y = 0;
            if (x > length - 1) x = length - 1;
            if (y > height - 1) y = height - 1;
    
            return grid[x][y];
        }
    }

    /**
     * Fires a shot at a given grid square, and returns the Robot that
     * was killed by the shot. If no Robot was hit, returns null.
     * Killing a Robot ends its thread.
     * @param firedShot
     * @return The killed Robot
     */
    public Robot fireShot(Shot firedShot) {
        synchronized(monitor) {
            int x = firedShot.getX();
            int y = firedShot.getY();
            Robot killedRobot = null;

            Robot robot = getGridSquare(x, y).getRobot();
            try {
                if (robot != null) {
                    // end the robot's thread
                    robot.destroy(x, y);
                    // remove the robot from the list (thus removing it from the GUI)
                    robots.remove(robot);
                    // indicate that the shot was successful
                    killedRobot = robot;
                    // update GUI to remove the robot
                    Platform.runLater(() -> {
                        UIElements ui = UIElements.getInstance();
                        ui.getArena().updateRobotPositions();
                    });
                } else {
                    Platform.runLater(() -> {
                        UIElements ui = UIElements.getInstance();
                        ui.getLogger().appendText(String.format("A shot failed to hit any robot at [%d, %d]\n", x, y));
                    });
                }
            } catch (RobotMismatchException e) {
                System.out.println(String.format("Failed to destroy robot at [%d, %d]", x, y));
            }
            return killedRobot;
        }
    }

    /**
     * Attemps to place a new Robot at one of the four corners of the grid.
     * If no corners are free during the execution of this method, the grid 
     * will remain unchanged (i.e. no new robot is spawned). A new thread will
     * be created for the successfully spawned robot via a thread-pool
     */
    private void attemptSpawn() {
        synchronized(monitor) {
            for (int i = 1; i <= 4; i++) {
                GridSquare corner = getCorner(i);
                Robot newRobot = new Robot(robotCounter, corner.getX(), corner.getY());
                if (!corner.isOccupied()) {
                    try {
                        // attempt to set the robot on the square
                        corner.setRobot(newRobot);
                        // create a new thread for the robot to run in
                        robotTasks.add(es.submit(newRobot));
                        // update robotCounter
                        robotCounter++;
                        // add the robot to the list
                        robots.add(newRobot);
                        // exit the loop (the robot has been successfully spawned)
                        return;
                    } catch (AlreadyOccupiedException e) {
                        // the square is already occupied; try a different square
                        continue;
                    } catch (RejectedExecutionException e) {
                        // the robot cannot be spawned due to constraints set for thread
                        // spawning for the ExecutorService object; do nothing
                        break;
                    }
                }
            }
        }
    }

    /**
     * Returns a GridSquare object corresponding to the one of the four 
     * corners in the grid (as indicated by cornerId):
     * 1: Top-left corner
     * 2: Bottom-left corner
     * 3: Bottom-right corner
     * 4: Top-right corner
     * If the value of cornerId is invalid, this method returns the
     * top-left GridSquare.
     */
    private GridSquare getCorner(int cornerId) {
        switch (cornerId) {
            case 1:
                return getGridSquare(0, 0);
            case 2:
                return getGridSquare(0, height-1);
            case 3:
                return getGridSquare(length-1, height-1);
            case 4:
                return getGridSquare(length-1, 0);
            default:
                return getGridSquare(0, 0);
        }
    }

    /**
     * Returns a random grid square that is adjacent from the grid square
     * located at the (x, y) coordinates provided. This method will never
     * return a grid square at (x, y).
     */
    public GridSquare getRandomAdjacentGridSquare(int x, int y) {
        int diffX = ThreadLocalRandom.current().nextInt(-1, 1 + 1);
        int diffY = ThreadLocalRandom.current().nextInt(-1, 1 + 1);
        int newX = x + diffX;
        int newY = y + diffY;

        while (getGridSquare(x, y) == getGridSquare(newX, newY) || Math.abs(diffX + diffY) != 1) {
            diffX = ThreadLocalRandom.current().nextInt(-1, 1 + 1);
            diffY = ThreadLocalRandom.current().nextInt(-1, 1 + 1);    
            newX = x + diffX;
            newY = y + diffY;
        }
        
        return getGridSquare(newX, newY);
    }
}
