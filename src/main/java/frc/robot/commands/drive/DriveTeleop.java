package frc.robot.commands.drive;

import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.Constants;
import frc.robot.IMU;
import frc.robot.OI;
import frc.robot.subsystems.DriveSubsystem;
import friarLib2.utility.DoubleSlewRateLimiter;
import friarLib2.utility.Vector3309;

/**
 * Use the left joystick position to control the robot's direction of 
 * travel relative to the field. Robot heading change is determined 
 * by moving the right joystick left and right.
 * 
 * <p>
 * i.e., the robot will move in whatever direction the stick is pushed,
 * regardless of its orientation on te field.
 * 
 * <p>
 * This class is designed to be subclassed so that other commands can 
 * use the same translational velcoity calculations for field-relative 
 * teleop control while being able to set the rotational speed 
 * independently (based on vision data for example).
 */
public class DriveTeleop extends CommandBase {

    protected DriveSubsystem drive;

    private DoubleSlewRateLimiter xAccelLimiter;
    private DoubleSlewRateLimiter yAccelLimiter;

    public DriveTeleop(DriveSubsystem drive) {
        this.drive = drive;

        xAccelLimiter = new DoubleSlewRateLimiter(Constants.Drive.MAX_TELEOP_ACCELERATION, Constants.Drive.MAX_TELEOP_DECELERATION);
        yAccelLimiter = new DoubleSlewRateLimiter(Constants.Drive.MAX_TELEOP_ACCELERATION, Constants.Drive.MAX_TELEOP_DECELERATION);

        addRequirements(drive);
    }

    @Override
    public void initialize() {
        xAccelLimiter.reset(0);
        yAccelLimiter.reset(0);
    }

    @Override
    public void execute() {
        Vector3309 translationalSpeeds = Vector3309.fromCartesianCoords(
            -OI.leftStick.getXWithDeadband(), 
            -OI.leftStick.getYWithDeadband()).capMagnitude(1).scale(Constants.Drive.MAX_TELEOP_SPEED);

        // Limit the drivebase's acceleration to reduce wear on the swerve modules
        translationalSpeeds.setXComponent(xAccelLimiter.calculate(translationalSpeeds.getXComponent()));
        translationalSpeeds.setYComponent(yAccelLimiter.calculate(translationalSpeeds.getYComponent()));

        ChassisSpeeds speeds = ChassisSpeeds.fromFieldRelativeSpeeds(
            translationalSpeeds.getXComponent(), 
            translationalSpeeds.getYComponent(), 
            calculateRotationalSpeed(translationalSpeeds), 
            IMU.getRobotYaw());

        drive.setChassisSpeeds(speeds);
    }

    /**
     * Given the field-relative translational speeds requested by the
     * operators, calculate the rotational speed of the robot.
     * 
     * @param translationalSpeeds
     * @return The rotational speed in radians/second
     */
    protected double calculateRotationalSpeed (Vector3309 translationalSpeeds) {
        double rotationalSpeed = Constants.Drive.MAX_TELEOP_ROTATIONAL_SPEED * -OI.rightStick.getXWithDeadband();

        return rotationalSpeed;
    }

    @Override
    public boolean isFinished() {
        return false;
    }
}
