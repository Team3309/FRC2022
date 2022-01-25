package friarLib2.hardware;

import edu.wpi.first.math.kinematics.SwerveModuleState;

/**
 * An interface representing any swerve module
 */
public interface SwerveModule {
    public void setState (SwerveModuleState state);
    public SwerveModuleState getState ();

    default public boolean steeringHasSlipped () {
        return false;
    }
}