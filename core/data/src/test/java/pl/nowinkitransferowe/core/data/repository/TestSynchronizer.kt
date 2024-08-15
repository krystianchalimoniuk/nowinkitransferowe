package pl.nowinkitransferowe.core.data.repository

import pl.nowinkitransferowe.core.data.Synchronizer
import pl.nowinkitransferowe.core.datastore.ChangeListVersions
import pl.nowinkitransferowe.core.datastore.NtPreferencesDataSource

/**
 * Test synchronizer that delegates to [NtPreferencesDataSource]
 */
class TestSynchronizer(
    private val ntPreferences: NtPreferencesDataSource,
) : Synchronizer {
    override suspend fun getChangeListVersions(): ChangeListVersions =
        ntPreferences.getChangeListVersions()

    override suspend fun updateChangeListVersions(
        update: ChangeListVersions.() -> ChangeListVersions,
    ) = ntPreferences.updateChangeListVersion(update)
}
