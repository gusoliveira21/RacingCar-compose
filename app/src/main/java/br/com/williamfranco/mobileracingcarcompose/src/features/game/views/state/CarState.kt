package br.com.williamfranco.mobileracingcarcompose.src.features.game.views.state

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toOffset
import androidx.compose.ui.unit.toSize
import br.com.williamfranco.mobileracingcarcompose.src.features.game.models.AccelerationData
import br.com.williamfranco.mobileracingcarcompose.src.features.game.models.CarPosition
import br.com.williamfranco.mobileracingcarcompose.src.features.game.models.CarPosition.Left
import br.com.williamfranco.mobileracingcarcompose.src.features.game.models.CarPosition.Middle
import br.com.williamfranco.mobileracingcarcompose.src.features.game.models.CarPosition.Right
import br.com.williamfranco.mobileracingcarcompose.src.features.game.models.RotationDirection
import br.com.williamfranco.mobileracingcarcompose.src.features.game.models.SwipeDirection
import br.com.williamfranco.mobileracingcarcompose.src.services.Constants.ACCELERATION_X_Y_OFFSET_TRIGGER
import br.com.williamfranco.mobileracingcarcompose.src.services.Constants.CAR_POSITION_PERCENTAGE_FROM_BOTTOM
import br.com.williamfranco.mobileracingcarcompose.src.services.Constants.CAR_SIZE
import br.com.williamfranco.mobileracingcarcompose.src.services.Constants.LANE_COUNT
import br.com.williamfranco.mobileracingcarcompose.src.services.Constants.STREET_SIDE_PERCENTAGE_EACH

data class CarState(
    private val image: ImageBitmap,
    private var position: CarPosition = Middle
) {

    private var rotationDirection: RotationDirection? = null

    fun draw(
        drawScope: DrawScope,
        offsetIndex: Float
    ): Rect {
        drawScope.apply {
            val initialOffsetX = (size.width.toInt() * STREET_SIDE_PERCENTAGE_EACH / 100)
            val laneSize =
                (size.width.toInt() * (100 - (2 * STREET_SIDE_PERCENTAGE_EACH)) / 100) / LANE_COUNT
            val carOffsetX =
                initialOffsetX +
                        (laneSize / 2) - (CAR_SIZE / 2) +
                        (laneSize * offsetIndex).toInt()

            val dstOffset = IntOffset(
                x = carOffsetX,
                y = size.height.toInt() - (size.height.toInt() * CAR_POSITION_PERCENTAGE_FROM_BOTTOM / 100)
            )
            val dstSize = IntSize(width = CAR_SIZE, height = CAR_SIZE)
            val rect = Rect(offset = dstOffset.toOffset(), size = dstSize.toSize())

            withTransform({
                val rotationDegrees = when (rotationDirection) {
                    RotationDirection.Right -> 90
                    RotationDirection.Left -> -90
                    else -> 0
                }
                val progress = offsetIndex.rem(1.0)
                val degrees = if (progress < 0.5f)
                    rotationDegrees * progress
                else
                    rotationDegrees * (1 - progress)

                rotate(
                    degrees = degrees.toFloat(),
                    pivot = rect.center
                )
            }) {
                drawImage(
                    image = image,
                    dstOffset = dstOffset,
                    dstSize = dstSize
                )
            }
            return rect
        }
    }

    fun moveWithTapGesture(position: CarPosition) {
        val currentPositionIndex = this.position.fromLeftOffsetIndex()
        val nextPositionIndex = position.fromLeftOffsetIndex()

        if (nextPositionIndex == currentPositionIndex)
            return
        else if (nextPositionIndex > currentPositionIndex)
            moveRight()
        else
            moveLeft()
    }

    fun moveWithSwipeGesture(swipeDirection: SwipeDirection) {
        when (swipeDirection) {
            SwipeDirection.Right -> moveRight()
            SwipeDirection.Left -> moveLeft()
        }
    }

    private fun moveRight() {
        rotationDirection = RotationDirection.Right
        position = when (position) {
            Right -> Right
            Middle -> Right
            Left -> Middle
        }
    }

    private fun moveLeft() {
        rotationDirection = RotationDirection.Left
        position = when (position) {
            Right -> Middle
            Middle -> Left
            Left -> Left
        }
    }

    fun moveWithAcceleration(acceleration: AccelerationData) {
        val ratio = acceleration.x * acceleration.y
        position = if (ratio > ACCELERATION_X_Y_OFFSET_TRIGGER) {
            rotationDirection = RotationDirection.Left
            Left
        } else if (ratio < -ACCELERATION_X_Y_OFFSET_TRIGGER) {
            rotationDirection = RotationDirection.Right
            Right
        } else {
            rotationDirection = when (position) {
                Right -> RotationDirection.Left
                Middle -> null
                Left -> RotationDirection.Right
            }
            Middle
        }
    }

    fun getPosition() = position
}
