package ee.zstronics.photoediting.filters

import ee.ja.burhanrashid52.photoeditor.PhotoFilter


interface FilterListener {
    fun onFilterSelected(photoFilter: PhotoFilter)
}