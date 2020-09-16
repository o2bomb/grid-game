public class GridSquare {
    private int x;
    private int y;

    private Object monitor = new Object();
    private Robot robot = null;

    public GridSquare(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean isOccupied() {
        synchronized(monitor) {
            return robot != null;
        }
    }

    public Robot getRobot() {
        synchronized(monitor) {
            return robot;
        }
    }

    public void setRobot(Robot robot) throws AlreadyOccupiedException {
        synchronized(monitor) {
            if (isOccupied()) {
                System.out.println(String.format("(Robot #%d) clashed with Robot #%d", robot.getId(), this.robot.getId()));
                throw new AlreadyOccupiedException("Grid square is already occupied");
            }

            this.robot = robot;
        }
    }

    public void clearRobot(Robot robot) throws RobotMismatchException {
        synchronized(monitor) {
            if (this.robot != null && this.robot != robot) {
                System.out.println(String.format("(Robot #%d) failed to remove Robot #%d", robot.getId(), this.robot.getId()));
                throw new RobotMismatchException("Only the robot that occupies this square can remove itself");
            }

            this.robot = null;
        }
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
