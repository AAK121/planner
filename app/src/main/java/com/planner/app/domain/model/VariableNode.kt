package com.planner.app.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class VariableNode {
    abstract val id: String
    abstract val label: String

    @Serializable
    @SerialName("group")
    data class GroupNode(
        override val id: String,
        override val label: String,
    ) : VariableNode()
}
