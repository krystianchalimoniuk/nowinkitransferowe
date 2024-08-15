package pl.nowinkitransferowe.core.datastore

/**
 * Class summarizing the local version of each model for sync
 */
data class ChangeListVersions(
    val transferResourceVersion: Int = -1,
    val newsResourceVersion: Int = -1,
)
