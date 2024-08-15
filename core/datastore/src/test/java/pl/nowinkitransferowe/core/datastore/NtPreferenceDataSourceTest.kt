package pl.nowinkitransferowe.core.datastore

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import pl.nowinkitransferowe.core.datastoretest.testUserPreferencesDataStore

class NtPreferenceDataSourceTest {

    private val testScope = TestScope(UnconfinedTestDispatcher())
    private lateinit var subject: NtPreferencesDataSource

    @get:Rule
    val tmpFolder: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

    @Before
    fun setup() {
        subject = NtPreferencesDataSource(
            tmpFolder.testUserPreferencesDataStore(TestScope(UnconfinedTestDispatcher())),
        )
    }


    @Test
    fun shouldUseDynamicColorFalseByDefault() = testScope.runTest {
        assertFalse(subject.userData.first().useDynamicColor)
    }

    @Test
    fun userShouldUseDynamicColorIsTrueWhenSet() = testScope.runTest {
        subject.setDynamicColorPreference(true)
        assertTrue(subject.userData.first().useDynamicColor)
    }

    @Test
    fun isNewsNotificationsAllowedFalseByDefault() = runTest {
        assertFalse(subject.userData.first().isNewsNotificationsAllowed)
    }

    @Test
    fun isNewsNotificationsAllowedIsTrueWhenSet() = runTest {
        subject.setNewsNotificationsAllowed(true)
        assertTrue(subject.userData.first().isNewsNotificationsAllowed)
    }

    @Test
    fun isTransferNotificationsAllowedFalseByDefault() = runTest {
        assertFalse(subject.userData.first().isTransfersNotificationsAllowed)
    }

    @Test
    fun isTransferNotificationsAllowedIsTrueWhenSet() = runTest {
        subject.setTransfersNotificationsAllowed(true)
        assertTrue(subject.userData.first().isTransfersNotificationsAllowed)
    }

    @Test
    fun isGeneralNotificationsAllowedFalseByDefault() = runTest {
        assertFalse(subject.userData.first().isTransfersNotificationsAllowed)
    }

    @Test
    fun isGeneralNotificationsAllowedIsTrueWhenSet() = runTest {
        subject.setGeneralNotificationsAllowed(true)
        assertTrue(subject.userData.first().isGeneralNotificationAllowed)
    }

}
