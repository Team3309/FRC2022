package frc.robot.commands.drive;

import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.Constants;
import frc.robot.OI;
import frc.robot.subsystems.DriveSubsystem;

/**
 * Use the left joystick position to control the robot's direction of 
 * travel relative to the field. Robot heading change is determined 
 * by moving the right joystick left and right.
 * 
 * <p>
 * i.e., the robot will move in whatever direction the stick is pushed,
 * regardless of its orientation on te field.
 */
public class FieldRelativeTeleopControl extends CommandBase {

    private DriveSubsystem drive;

    public FieldRelativeTeleopControl(DriveSubsystem drive) {
        this.drive = drive;

        addRequirements(drive);
    }

    @Override
    public void execute() {
        double xSpeed = Constants.Drive.MAX_TELEOP_SPEED * -OI.leftStick.getXWithDeadband();
        double ySpeed = Constants.Drive.MAX_TELEOP_SPEED * OI.leftStick.getYWithDeadband();
        double rotationalSpeed = Constants.Drive.MAX_TELEOP_ROTATIONAL_SPEED * OI.rightStick.getXWithDeadband();

        ChassisSpeeds speeds = ChassisSpeeds.fromFieldRelativeSpeeds(xSpeed, ySpeed, rotationalSpeed, drive.getRobotRotation());

        drive.setChassisSpeeds(speeds);
    }

    @Override
    public boolean isFinished() {
        return false;
    }
}