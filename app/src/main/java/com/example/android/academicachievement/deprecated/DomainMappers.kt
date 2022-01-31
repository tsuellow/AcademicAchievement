package com.example.android.academicachievement.deprecated

import com.example.android.academicachievement.domain.model.Course
import com.example.android.academicachievement.domain.model.Milestone
import com.example.android.academicachievement.domain.model.Part
import com.example.android.academicachievement.data.remote.dto.CourseDto
import com.example.android.academicachievement.data.remote.dto.MilestoneDto
import com.example.android.academicachievement.data.remote.dto.PartDto

interface DomainMappers {
    fun mapToDomain(milestoneDto: MilestoneDto):Milestone
    fun mapToDomain(partDto: PartDto): Part
    fun mapToDomain(courseDto: CourseDto):Course

    fun mapFromDomain(milestone: Milestone):MilestoneDto
    fun mapFromDomain(part: Part): PartDto
    fun mapFromDomain(course: Course):CourseDto
}