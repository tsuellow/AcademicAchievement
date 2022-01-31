package com.example.android.academicachievement.deprecated

import com.example.android.academicachievement.domain.model.Course
import com.example.android.academicachievement.domain.model.Milestone
import com.example.android.academicachievement.domain.model.Part
import com.example.android.academicachievement.data.remote.dto.CourseDto
import com.example.android.academicachievement.data.remote.dto.MilestoneDto
import com.example.android.academicachievement.data.remote.dto.PartDto

class DomainMapperImpl: DomainMappers {
    override fun mapToDomain(milestoneDto: MilestoneDto): Milestone {
        with(milestoneDto){
            return Milestone(key,name,date,completed,comment,grades)
        }
    }

    override fun mapToDomain(partDto: PartDto): Part {
        with(partDto){
            return Part(key,name, completed, HashMap(milestones.mapValues { mapToDomain(it.value) }))
        }
    }

    override fun mapToDomain(courseDto: CourseDto): Course {
        with(courseDto){
            return Course(key,name, completed, HashMap(parts.mapValues { mapToDomain(it.value) }))
        }
    }

    override fun mapFromDomain(milestone: Milestone): MilestoneDto {
        with(milestone){
            return MilestoneDto(key,name,date,completed,comment,grades)
        }
    }

    override fun mapFromDomain(part: Part): PartDto {
        with(part){
            return PartDto(key,name, completed, HashMap(milestones.mapValues { mapFromDomain(it.value) }))
        }
    }

    override fun mapFromDomain(course: Course): CourseDto {
        with(course){
            return CourseDto(key,name, completed, HashMap(parts.mapValues { mapFromDomain(it.value) }))
        }
    }
}