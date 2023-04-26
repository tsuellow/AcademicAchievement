package com.example.android.academicachievement.domain.model

import com.example.android.academicachievement.data.remote.dto.CourseDto
import com.example.android.academicachievement.util.toDateTimeString
import java.util.*
import kotlin.collections.HashMap

data class Course(
    val key: String = "",
    val name: String = "",
    val dateEnrolled: String = "never",
    val enrolledBy: String = "none",
    var dateCompleted: String = "never",
    var completed: Boolean = false,
    val parts: HashMap<String, Part> = hashMapOf()
) {

    fun toCourseDto(): CourseDto {
        return CourseDto(
            key,
            name,
            dateEnrolled,
            enrolledBy,
            dateCompleted,
            completed,
            HashMap(parts.mapValues { it.value.toPartDto() })
        )
    }

    fun copy(): Course {
        return Course(
            key,
            name,
            dateEnrolled,
            enrolledBy,
            dateCompleted,
            completed,
            HashMap(parts.mapValues { it.value.copy() })
        )
    }

    fun getMilestoneByPath(path: String): Milestone? {
        val pathList: List<String> = path.split("/")
        return if (key.contentEquals(pathList[0])) {
            parts[pathList[1]]?.milestones?.get(pathList[2])?.copy()
        } else {
            null
        }
    }

    fun setMilestoneByPath(path: String, milestone: Milestone) {
        val part = getPartByPath(path)
        part?.milestones?.put(milestone.key, milestone)
        //propagate completion
        for (m in part!!.milestones) {
            if (!m.value.completed) {
                part.completed = false
                completed = false
                dateCompleted = "never"
                return
            }
        }
        part.completed = true
        for (p in parts) {
            if (!p.value.completed) {
                return
            }
        }
        completed = true
        dateCompleted = Date().toDateTimeString()
    }


    fun getPartByPath(path: String): Part? {
        val pathList: List<String> = path.split("/")
        return if (key.contentEquals(pathList[0])) {
            parts[pathList[1]]
        } else {
            null
        }
    }

    fun getMilestoneCourseName(path: String): String? {
        val pathList: List<String> = path.split("/")
        return if (key.contentEquals(pathList[0])) {
            "$key. $name"
        } else {
            null
        }
    }

    fun getMilestonePartName(path: String): String? {
        val pathList: List<String> = path.split("/")
        return if (key.contentEquals(pathList[0])) {
            parts[pathList[1]]?.key + ". " + parts[pathList[1]]?.name
        } else {
            null
        }
    }

    fun getMilestoneName(path: String): String? {
        val pathList: List<String> = path.split("/")
        return if (key.contentEquals(pathList[0])) {
            parts[pathList[1]]?.milestones?.get(pathList[2])!!.key + ". " + parts[pathList[1]]?.milestones?.get(
                pathList[2]
            )!!.name
        } else {
            null
        }
    }

}
