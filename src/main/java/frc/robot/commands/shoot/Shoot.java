package frc.robot.commands.shoot;

import java.util.function.BooleanSupplier;

import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.subsystems.IndexerSubsystem;
import frc.robot.subsystems.ShooterSubsystem;
import frc.robot.util.FiringSolution;

/**
 * Activate the shooter, firing cargo if the condition evalutes to true
 */
public class Shoot extends CommandBase {

    protected BooleanSupplier shootCondition;
    private final FiringSolution solution;

    private final ShooterSubsystem shooter;
    private final IndexerSubsystem indexer;

    public Shoot(BooleanSupplier shootCondition, FiringSolution solution, ShooterSubsystem shooter, IndexerSubsystem indexer) {
        this.shootCondition = shootCondition;
        this.solution = solution;

        this.shooter = shooter;
        this.indexer = indexer;

        addRequirements(shooter, indexer);
    }

    @Override
    public void initialize() {
        shooter.goToFiringSolution(solution);
    }

    @Override
    public void execute() {
        // If the flywheel is up to speed and the condition is true
        if (shooter.isFlywheelUpToSpeed() && shootCondition.getAsBoolean()) {
            indexer.startConveyor();
            indexer.startGateWheelForShooting();
        } else {
            indexer.stopConveyor();
            indexer.stopGateWheel();
        }
    }

    @Override
    public void end(boolean interrupted) {
        shooter.stopFlywheel();
        indexer.stopConveyor();
        indexer.stopGateWheel();
        //intake.retractIntake();
    }

    @Override
    public boolean isFinished() {
        return false;
    }
}
