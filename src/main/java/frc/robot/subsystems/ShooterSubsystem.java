package frc.robot.subsystems;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonFX;

import edu.wpi.first.math.filter.LinearFilter;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import frc.robot.UnitConversions;
import frc.robot.util.FiringSolution;
import frc.robot.util.TalonStatusFrames;
import friarLib2.math.RateOfChangeCalculator;

import static frc.robot.Constants.Shooter.*;
import static frc.robot.UnitConversions.Shooter.*;

/**
 * Represents the robot's shooter
 * 
 * <p>
 * The mechanism contains a single flywheel with a fixed hood and a
 * pneumatically actuated "deflector" that redirects the shot to a
 * more horizontal trajectory
 */
public class ShooterSubsystem extends SubsystemBase {

    // Flywheel motors
    private final WPI_TalonFX flywheelLeader;
    private final WPI_TalonFX flywheelFollower;

    private final Solenoid deflectorSolenoid;

    private final RateOfChangeCalculator flywheelROC = new RateOfChangeCalculator();
    private final LinearFilter ROCFilter = LinearFilter.movingAverage(7);
    private double smoothedROC = 0;

    public ShooterSubsystem() {
        // Configure the flywheel motors
        flywheelLeader = new WPI_TalonFX(LEADER_MOTOR_ID);
        flywheelLeader.configFactoryDefault();
        flywheelLeader.setNeutralMode(NeutralMode.Coast);
        FLYWHEEL_MOTOR_PID.configureMotorPID(flywheelLeader);
        flywheelLeader.config_IntegralZone(0, UnitConversions.Shooter.flywheelRPMToEncoderTicksPer100ms(FLYWHEEL_IZONE));

        flywheelFollower = new WPI_TalonFX(FOLLOWER_MOTOR_ID);
        flywheelFollower.configFactoryDefault();
        flywheelFollower.setNeutralMode(NeutralMode.Coast);
        FLYWHEEL_MOTOR_PID.configureMotorPID(flywheelFollower);
        flywheelLeader.config_IntegralZone(0, UnitConversions.Shooter.flywheelRPMToEncoderTicksPer100ms(FLYWHEEL_IZONE));
        flywheelFollower.follow(flywheelLeader);
        TalonStatusFrames.configFollowerFrames(flywheelFollower);

        flywheelLeader.setInverted(true);
        flywheelFollower.setInverted(false);

        deflectorSolenoid = new Solenoid(
            Constants.PCM_CAN_ID,
            Constants.PCM_TYPE,
            HOOD_SOLENOID_ID
        );
    }

    /**
     * Set the flywheel's closed loop setpoint
     * 
     * @param RPM Velocity in rotations per minute
     */
    public void setFlywheelSpeed (double RPM) {
        flywheelLeader.set(ControlMode.Velocity, flywheelRPMToEncoderTicksPer100ms(RPM));
    }

    /**
     * Stop the flywheel
     */
    public void stopFlywheel () {
        flywheelLeader.stopMotor();
    }

    /**
     * Get the speed of the flywheel in RPM
     * 
     * @return
     */
    public double getFlywheelRPM () {
        return flywheelEncoderTicksPer100msToRPM(flywheelLeader.getSelectedSensorVelocity());
    }

    /**
     * @return True if the flywheel is on and at its target velocity
     */
    public boolean isFlywheelUpToSpeed () {
        boolean isRunning = getFlywheelRPM() >= 50  && flywheelLeader.getMotorOutputPercent() >= 0.05;

        boolean isUpToSpeed = Math.abs(flywheelEncoderTicksPer100msToRPM(flywheelLeader.getClosedLoopError())) <= FLYWHEEL_SPEED_TOLERANCE;

        boolean isSpeedStable = Math.abs(smoothedROC) <= FLYWHEEL_ROC_TOLERANCE;

        return isRunning && isUpToSpeed && isSpeedStable;
    }

    /**
     * Deploy or retract the shot deflector
     * 
     * @param deployed If the deflector should be deployed or retracted
     */
    public void setDeflector (boolean deployed) {
        deflectorSolenoid.set(!deployed);
    }

    public void goToFiringSolution (FiringSolution solution) {
        setFlywheelSpeed(solution.getFlywheelRPM());
        setDeflector(solution.getDeflector());
    }

    @Override
    public void periodic() {
        flywheelROC.update(getFlywheelRPM());
        smoothedROC = ROCFilter.calculate(flywheelROC.getRoC());

        SmartDashboard.putNumber("Flywheel Speed", getFlywheelRPM());
        SmartDashboard.putNumber("Flywheel Speed RoC", smoothedROC);
        SmartDashboard.putNumber("Flywheel Setpoint", flywheelEncoderTicksPer100msToRPM(flywheelLeader.getClosedLoopTarget()));
        SmartDashboard.putNumber("Flywheel Error", flywheelEncoderTicksPer100msToRPM(flywheelLeader.getClosedLoopError()));
        SmartDashboard.putBoolean("Flywheel Up To Speed", isFlywheelUpToSpeed());
    }
}
