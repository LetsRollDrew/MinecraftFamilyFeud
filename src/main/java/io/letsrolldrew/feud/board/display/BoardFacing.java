package io.letsrolldrew.feud.board.display;

public enum BoardFacing {
    NORTH(180f, 0, -1),
    EAST(-90f, 1, 0),
    SOUTH(0f, 0, 1),
    WEST(90f, -1, 0);

    private final float yaw;
    private final double forwardX;
    private final double forwardZ;

    BoardFacing(float yaw, double forwardX, double forwardZ) {
        this.yaw = yaw;
        this.forwardX = forwardX;
        this.forwardZ = forwardZ;
    }

    public float yaw() {
        return yaw;
    }

    public double forwardX() {
        return forwardX;
    }

    public double forwardZ() {
        return forwardZ;
    }

    public double rightX() {
        return -forwardZ;
    }

    public double rightZ() {
        return forwardX;
    }

    public static BoardFacing fromYaw(float yawDeg) {
        float y = yawDeg % 360f;
        if (y < 0) {
            y += 360f;
        }
        int quadrant = Math.round(y / 90f) % 4;
        return switch (quadrant) {
            case 0 -> SOUTH;
            case 1 -> WEST;
            case 2 -> NORTH;
            default -> EAST;
        };
    }
}
