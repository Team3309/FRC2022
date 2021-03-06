package frc.robot.subsystems;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveDriveOdometry;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.IMU;
import frc.robot.Vision;
import frc.robot.swerve.SwerveModule3309;
import friarLib2.hardware.SwerveModule;

import static frc.robot.Constants.Drive.*;

public class DriveSubsystem extends SubsystemBase {

    private final Field2d field = new Field2d();

    private final SwerveModule frontLeftModule;
    private final SwerveModule frontRightModule;
    private final SwerveModule backLeftModule;
    private final SwerveModule backRightModule;

    private final SwerveDriveOdometry swerveOdometry;
    private final SwerveDriveKinematics swerveKinematics;
    private Pose2d currentRobotPose = new Pose2d();

    /**
     * Initialize the swerve modules, imu, and Kinematics/Odometry objects
     */
    public DriveSubsystem() {
        frontLeftModule = new SwerveModule3309(225, FRONT_LEFT_MODULE_IDS, "Front left");
        frontRightModule = new SwerveModule3309(315, FRONT_RIGHT_MODULE_IDS, "Front right");
        backLeftModule = new SwerveModule3309(135, BACK_LEFT_MODULE_IDS, "Back left");
        backRightModule = new SwerveModule3309(45, BACK_RIGHT_MODULE_IDS, "Back right");

        swerveKinematics = new SwerveDriveKinematics(
            FRONT_LEFT_MODULE_TRANSLATION,
            FRONT_RIGHT_MODULE_TRANSLATION,
            BACK_LEFT_MODULE_TRANSLATION,
            BACK_RIGHT_MODULE_TRANSLATION
        );
        swerveOdometry = new SwerveDriveOdometry(swerveKinematics, IMU.getRobotYaw());

        IMU.zeroIMU();

        SmartDashboard.putData("Odometry", field);
    }

    public void setModuleStates (SwerveModuleState[] states) {
        frontLeftModule.setState(states[0]);
        frontRightModule.setState(states[1]);
        backLeftModule.setState(states[2]);
        backRightModule.setState(states[3]);
    }

    /**
     * Use the CANCoders to rezero each swerve module
     */
    public void zeroModules () {
        frontLeftModule.zeroSteering();
        frontRightModule.zeroSteering();
        backLeftModule.zeroSteering();
        backRightModule.zeroSteering();
    }

    /**
     * Calculate and set the required SwerveModuleStates for a given ChassisSpeeds
     * 
     * @param speeds
     */
    public void setChassisSpeeds (ChassisSpeeds speeds) {
        SwerveModuleState[] moduleStates = swerveKinematics.toSwerveModuleStates(speeds); //Generate the swerve module states
        SwerveDriveKinematics.desaturateWheelSpeeds(moduleStates, SwerveModule3309.ABSOLUTE_MAX_DRIVE_SPEED);
        setModuleStates(moduleStates);
    }

    /**
     * Set the target speeds to zero (stop the drivetrain)
     */
    public void stopChassis () {
        setChassisSpeeds(new ChassisSpeeds());
    }

    /**
     * Get the current robot pose according to dead-reckoning odometry
     * See https://docs.wpilib.org/en/stable/docs/software/kinematics-and-odometry/swerve-drive-odometry.html for more details
     * 
     * @return Current robot pose
     */
    public Pose2d getRobotPose () {
        return currentRobotPose;
    }

    /**
     * Set the odometry readings
     * 
     * @param pose Pose to be written to odometry
     */
    public void resetOdometry (Pose2d pose) {
        IMU.tareIMU(pose.getRotation());
        swerveOdometry.resetPosition(pose, new Rotation2d());
    }

    @Override
    public void periodic() {
        //Update the odometry using module states and chassis rotation
        currentRobotPose = swerveOdometry.update(
            IMU.getRobotYaw(),
            frontLeftModule.getState(),
            frontRightModule.getState(),
            backLeftModule.getState(),
            backRightModule.getState()
        );

        frontLeftModule.outputToDashboard();
        frontRightModule.outputToDashboard();
        backLeftModule.outputToDashboard();
        backRightModule.outputToDashboard();

        field.setRobotPose(currentRobotPose);

        SmartDashboard.putNumber("Robot heading", IMU.getRobotYaw().getDegrees());
        SmartDashboard.putNumber("Meters to target", Vision.getMetersFromTarget());
    }
}
