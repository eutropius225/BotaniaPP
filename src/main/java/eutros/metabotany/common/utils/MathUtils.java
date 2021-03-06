package eutros.metabotany.common.utils;

import com.google.common.primitives.Longs;
import net.minecraft.util.Direction;
import org.apache.commons.lang3.ArrayUtils;

import java.awt.geom.Point2D;
import java.util.Arrays;

public class MathUtils {

    public static final Direction[] HORIZONTALS = {Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.EAST};

    private MathUtils() {
    }

    public static int gcf(int a, int b) {
        while(a != b) {
            if(a > b) a -= b;
            else b -= a;
        }
        return a;
    }

    public static int lcm(int a, int b) {
        return (a * b) / gcf(a, b);
    }

    public static Point2D rotatePointAbout(Point2D in, Point2D about, double degrees) {
        double rad = degrees * Math.PI / 180.0;
        double newX = Math.cos(rad) * (in.getX() - about.getX()) - Math.sin(rad) * (in.getY() - about.getY()) + about.getX();
        double newY = Math.sin(rad) * (in.getX() - about.getX()) + Math.cos(rad) * (in.getY() - about.getY()) + about.getY();
        return new Point2D.Double(newX, newY);
    }

    public static <T extends Comparable<T>> T clamp(T val, T min, T max) {
        return val.compareTo(min) < 0 ? min :
               val.compareTo(max) > 0 ? max :
               val;
    }

    public static long[] toLongArray(byte[] bytes) {
        int length = (int) Math.ceil(bytes.length / 8F);
        byte[] padded = ArrayUtils.addAll(new byte[length * 8 - bytes.length], bytes);
        long[] longs = new long[length];

        for(int i = 0; i < length; i++)
            longs[i] = Longs.fromByteArray(Arrays.copyOfRange(padded, i * 8, (i + 1) * 8));
        return longs;
    }

    public static byte[] toByteArray(long[] longs) {
        return Arrays.stream(longs).mapToObj(Longs::toByteArray).reduce(new byte[0], ArrayUtils::addAll);
    }

    public static Direction rotateAround(Direction direction, Direction.Axis axis) {
        switch(axis) {
            case X: {
                switch(direction) {
                    case DOWN:
                        return Direction.SOUTH;
                    case SOUTH:
                        return Direction.UP;
                    case UP:
                        return Direction.NORTH;
                    case NORTH:
                        return Direction.DOWN;
                }
                break;
            }
            case Y: {
                switch(direction) {
                    case NORTH:
                        return Direction.EAST;
                    case EAST:
                        return Direction.SOUTH;
                    case SOUTH:
                        return Direction.WEST;
                    case WEST:
                        return Direction.NORTH;
                }
                break;
            }
            case Z: {
                switch(direction) {
                    case DOWN:
                        return Direction.WEST;
                    case WEST:
                        return Direction.UP;
                    case UP:
                        return Direction.EAST;
                    case EAST:
                        return Direction.DOWN;
                }
                break;
            }
        }

        return direction;
    }

    public static Direction roll(Direction direction) {
        return Direction.byIndex(direction.getIndex() + 3);
    }

}
