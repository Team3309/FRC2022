package friarLib2.commands.builders

import edu.wpi.first.wpilibj.Timer
import edu.wpi.first.wpilibj2.command.*

fun group(init: CommandGroupBuilder.() -> Unit): CommandGroupBuilder {
    val builder = CommandGroupBuilder()
    builder.init()
    return builder
}

/**
 * A builder for command groups
 */
open class CommandGroupBuilder(): CommandBuilder {
    // The command group that contains all other commands
    private var rootCommand: Command? = null;

    /**
     * A timer that starts counting when this command initializes
     */
    val timer: Timer = Timer()
    init {
        timer.stop()
        timer.reset()
    }

    /**
     * Set the root command using the unary plus operator
     */
    override fun Command.unaryPlus() {
        rootCommand = this
    }

    private fun <B: CommandBuilder> initCommand(commandBuilder: B, init: B.() -> Unit): Command {
        commandBuilder.init()
        // Get the built command from the command builder
        return commandBuilder.buildCommand();
    }

    /** Add a SequentialCommandGroup */
    fun sequential(init: SequentialCommandGroupBuilder.() -> Unit): Command =
        initCommand(SequentialCommandGroupBuilder(), init)

    /** Add a ParallelCommandGroup */
    fun parallel(init: ParallelCommandGroupBuilder.() -> Unit): Command =
        initCommand(ParallelCommandGroupBuilder(), init)

    /** Add a ParallelRaceGroup */
    fun race(init: ParallelRaceGroup.() -> Unit): Command =
        initCommand(ParallelRaceGroupBuilder(), init)

    /**
     * Add a ParallelDeadlineGroup
     *
     * <p>Use the unary plus operator to add a command to the command
     * group. Use the unary minus operator to add the deadline command
     */
    fun deadline(init: ParallelDeadlineGroup.() -> Unit): Command =
        initCommand(ParallelDeadlineGroupBuilder(), init)

    /**
     * Use a TimedCommandBuilder to create a new TimedCommand
     *
     * <p>Use the unary plus operator to add a command to the timed
     * parallel command group
     */
    fun timed(init: TimedCommandBuilder.() -> Unit): Command =
        initCommand(TimedCommandBuilder(), init)

    override fun buildCommand(): Command {
        return sequential {
            +InstantCommand({ // Reset the timer before starting the normally scheduled command
                timer.reset()
                timer.start()
            })
            +(rootCommand ?: PrintCommand("No commands have been added to the command group builder"))
        }
    }
}