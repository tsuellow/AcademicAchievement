package com.example.android.academicachievement.data.remote.dto

import com.example.android.academicachievement.domain.model.Course

data class CourseDto(
    val key: String = "",
    val name: String = "",
    val dateEnrolled: String = "never",
    val enrolledBy: String = "none",
    var dateCompleted: String = "never",
    var completed: Boolean = false,
    val parts: HashMap<String, PartDto> = hashMapOf()
) {

    fun toCourse(): Course {
        return Course(
            key,
            name,
            dateEnrolled,
            enrolledBy,
            dateCompleted,
            completed,
            HashMap(parts.mapValues { it.value.toPart() })
        )
    }
}
