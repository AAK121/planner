package com.planner.app.data.local.database.converter

// No custom TypeConverters needed:
// - String columns (scheduleJson, treeJson, goalJson, dataJson) are stored as-is
// - Long, Int, Boolean are Room-native types
// JSON marshaling is done in repository layer via kotlinx.serialization
class Converters
