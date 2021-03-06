package frc.robot.commands.climb

import edu.wpi.first.wpilibj2.command.CommandBase
import frc.robot.OI
import frc.robot.subsystems.ClimberSubsystem

class ClimbManual(private val climber: ClimberSubsystem) : CommandBase() {
    init {
        addRequirements(climber)
    }

    override fun execute() {
        if (climber.isExtended) {
            climber.setClimberPower(OI.operatorController.leftYWithDeadband)
        } else {
            climber.setClimberPower(0.0)
        }
    }
}