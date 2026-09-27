package net.modpackmuseum.core.energy;

/** Minecraft-independent integer block position used by the M2 world-topology adapter. */
public record GridPos(int x, int y, int z) {
    private static final int[][] DIRECTIONS = {
        {1, 0, 0}, {-1, 0, 0},
        {0, 1, 0}, {0, -1, 0},
        {0, 0, 1}, {0, 0, -1}
    };

    public GridPos offset(int dx, int dy, int dz) {
        return new GridPos(
            Math.addExact(x, dx),
            Math.addExact(y, dy),
            Math.addExact(z, dz)
        );
    }

    public GridPos[] neighbors() {
        GridPos[] result = new GridPos[DIRECTIONS.length];
        for (int i = 0; i < DIRECTIONS.length; i++) {
            int[] direction = DIRECTIONS[i];
            result[i] = offset(direction[0], direction[1], direction[2]);
        }
        return result;
    }
}
