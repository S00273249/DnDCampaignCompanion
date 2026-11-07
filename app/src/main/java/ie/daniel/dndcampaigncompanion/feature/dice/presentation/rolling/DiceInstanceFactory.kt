
package ie.daniel.dndcampaigncompanion.feature.dice.presentation.rolling

import ie.daniel.dndcampaigncompanion.feature.dice.domain.DiceType
import ie.daniel.dndcampaigncompanion.feature.dice.domain.RollResult
import kotlin.math.ceil
import kotlin.math.sqrt

// Creates individual 3D dice instances for previewing and rolling.
object DiceInstanceFactory {

    private const val HORIZONTAL_SPACING = 1.15f
    private const val VERTICAL_SPACING = 1.15f

    /**
     * Creates preview dice before actual results are generated.
     * Each preview die temporarily displays face 1.
     */
    fun createPreviewInstances(
        diceQuantities: Map<DiceType, Int>
    ): List<DiceInstance> {

        // flatMap creates a list containing the requested number of
        // each dice type, then combines everything into one list.
        // For example, 3 D6s and 2 D20s become [D6, D6, D6, D20, D20].
        val dice = diceQuantities.flatMap { (type, quantity) ->
            List(quantity) { type }
        }

        // Create one temporary result of 1 for every preview die.
        return createPositionedInstances(
            diceTypes = dice,
            results = List(dice.size) { 1 }
        )
    }

    /**
     * Creates individual dice using the actual generated results.
     */
    fun createInstances(
        rollResult: RollResult
    ): List<DiceInstance> {

        val dice = rollResult.individualResults

        if (dice.isEmpty()) {
            return emptyList()
        }

        // map extracts one property from every result.
        // Both lists keep the same order, so each dice type
        // stays matched with its corresponding result.
        return createPositionedInstances(
            diceTypes = dice.map { it.diceType },
            results = dice.map { it.value }
        )
    }

    /**
     * Assigns each die a unique ID and position.
     */
    private fun createPositionedInstances(
        diceTypes: List<DiceType>,
        results: List<Int>
    ): List<DiceInstance> {

        if (diceTypes.isEmpty()) {
            return emptyList()
        }

        // Both lists must have the same number of items because
        // each dice type needs a matching result at the same index.
        require(diceTypes.size == results.size)

        // sqrt calculates the square root of the number of dice.
        // ceil rounds it up to determine how many columns are needed.
        // toDouble() is used for decimal calculations, and toInt()
        // converts the final column count back into a whole number.
        val columns = ceil(sqrt(diceTypes.size.toDouble())).toInt()

        // Divide the number of dice by the number of columns.
        // ceil rounds up so there is enough space for every die,
        // including when the final row is only partially filled.
        val totalRows = ceil(
            diceTypes.size.toDouble() / columns
        ).toInt()

        // mapIndexed works like map but also provides the index
        // of each die, which is used to calculate its position.
        return diceTypes.mapIndexed { index, diceType ->

            // Integer division determines the row.
            // The remainder (%) determines the column.
            // For example, with 3 columns, index 4 gives row 1, column 1.
            val row = index / columns
            val column = index % columns


            // Work out how many dice belong in this row.
            // Subtract the number of dice already placed in previous rows
            // from the total number of dice remaining.
            // minOf() ensures a row never contains more dice than columns.
            val diceInCurrentRow = minOf(
                columns,
                diceTypes.size - row * columns
            )

            // Calculate the horizontal position of this die.
            // Subtract half the row's width (measured between the first
            // and last dice) from its column number to centre the row.
            // For 3 dice, this produces positions -1, 0 and 1.
            // For 2 dice, it produces -0.5 and 0.5.
            // Multiply by HORIZONTAL_SPACING to set the actual distance.
            val xOffset = (
                    column - (diceInCurrentRow - 1) / 2f
                    ) * HORIZONTAL_SPACING

            // Calculate the vertical position of this row.
            // Subtract half the distance between the first and last rows
            // from the row number to centre all rows around zero.
            // For 2 rows, this produces 0.5 and -0.5.
            // Multiply by VERTICAL_SPACING to set the distance between rows.
            val yOffset = (
                    (totalRows - 1) / 2f - row
                    ) * VERTICAL_SPACING

            // Create the die using its index as a unique ID.
            // results[index] retrieves the result at the same position
            // in the results list, keeping each die matched to its result.
            DiceInstance(
                id = index,
                diceType = diceType,
                result = results[index],
                position = DiceScenePosition(
                    x = xOffset,
                    y = yOffset,
                    z = 0f
                )
            )
        }
    }
}
