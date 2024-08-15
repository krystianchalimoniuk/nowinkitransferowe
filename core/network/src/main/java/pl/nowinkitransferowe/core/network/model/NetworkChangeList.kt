package pl.nowinkitransferowe.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Network representation of a change list for a model.
 *
 * Change lists are a representation of a server-side map like data structure of model ids to
 * metadata about that model. In a single change list, a given model id can only show up once.
 */
@Serializable
data class NetworkChangeList(
    /**
     * The id of the model that was changed
     */
    @SerialName("id")
    val id: String,
    /**
     * Unique consecutive, monotonically increasing version number in the collection describing
     * the relative point of change between models in the collection
     */
    @SerialName("changeListVersion")
    val changeListVersion: Int,
    /**
     * Summarizes the update to the model; whether it was deleted or updated.
     * Updates include creations.
     */
    @SerialName("isDelete")
    val isDelete: Boolean,
)
