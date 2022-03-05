package frc.robot;

import edu.wpi.first.math.util.Units;
import friarLib2.vision.PhotonCameraWrapper;
import friarLib2.vision.VisionCamera;
import friarLib2.vision.utility.PixelToAngle;

/**
 * Container for the vision systems
 */
public class Vision {

    public static VisionCamera shooterCamera = new PhotonCameraWrapper("");

    private static final PixelToAngle ANGLE_CONVERTER = new PixelToAngle(320, 240, 54, 41); // Constants for the limelight 2
    private static final double HEIGHT_OF_CAMERA = 0.5; // Meters
    private static final double ANGLE_OF_CAMERA = 20; // Degrees

    /**
     * @return the distance in meters from the target
     */
    public static double getMetersFromTarget () {
        double a2 = ANGLE_CONVERTER.calculateYAngle(shooterCamera.getBestTarget());
        double a1 = ANGLE_OF_CAMERA;
        double h1 = HEIGHT_OF_CAMERA;
        double h2 = Units.inchesToMeters(12*8 + 8);

        return (h2 - h1) / Math.tan(Math.toRadians(a1 + a2));
    }
}