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
        val children: List<VariableNode> = emptyList(),
        val daysOfWeek: List<Int> = emptyList(), // 1=Mon…7=Sun, empty = every day
    ) : VariableNode()

    @Serializable
    @SerialName("list")
    data class ListNode(
        override val id: String,
        override val label: String,
        val itemTemplate: List<VariableNode> = emptyList(),
    ) : VariableNode()

    @Serializable
    @SerialName("value")
    data class ValueNode(
        override val id: String,
        override val label: String,
        val valueType: ValueType,
        val unit: String = "",
        val choices: List<String> = emptyList(),
        val isRequired: Boolean = false,
    ) : VariableNode()
}
