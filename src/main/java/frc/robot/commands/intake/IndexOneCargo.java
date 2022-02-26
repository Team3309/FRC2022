package frc.robot.commands.intake;

import edu.wpi.first.wpilibj2.command.ConditionalCommand;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.WaitCommand;
import edu.wpi.first.wpilibj2.command.WaitUntilCommand;
import frc.robot.Constants;
import frc.robot.subsystems.IndexerSubsystem;

/**
 * Run the indexer's gate wheel until a cargo has been indexed, then
 */
public class IndexOneCargo extends ConditionalCommand {
    public IndexOneCargo(IndexerSubsystem indexer) {
        super(
            new WaitCommand(0), // Don't do anything if the indexer already has a cargo
            new SequentialCommandGroup(
                new InstantCommand(indexer::activateGateWheel), // Turn on the gatewheel
                new WaitUntilCommand(() -> indexer.getGateWheelSupplyCurrent() > 20), // Once a cargo has contacted the rollers, the current drawn by the motor should increase
                new InstantCommand(() -> indexer.rotateGateWheelByXDegrees(Constants.Indexer.GATE_WHEEL_INDEXING_DEGREES)) // Quickly spin the roller by a few rotations to "suck in" the cargo and hold it in place
            ),
            indexer::hasCargo
        );
    }
}