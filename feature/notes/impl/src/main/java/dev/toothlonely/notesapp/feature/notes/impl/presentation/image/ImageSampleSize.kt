package dev.toothlonely.notesapp.feature.notes.impl.presentation.image

internal fun calculateImageSampleSize(
    width: Int,
    height: Int,
    requestedWidth: Int,
    requestedHeight: Int,
): Int {
    val safeRequestedWidth = requestedWidth.coerceAtLeast(1)
    val safeRequestedHeight = requestedHeight.coerceAtLeast(1)
    var sampleSize = 1
    while (
        width / (sampleSize * 2) >= safeRequestedWidth &&
        height / (sampleSize * 2) >= safeRequestedHeight
    ) {
        sampleSize *= 2
    }
    return sampleSize
}
