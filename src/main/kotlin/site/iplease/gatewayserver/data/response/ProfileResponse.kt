package site.iplease.gatewayserver.data.response

import site.iplease.gatewayserver.data.type.AccountType
import site.iplease.gatewayserver.data.type.DepartmentType

data class ProfileResponse (
    val type: AccountType,
    val common: CommonProfileResponse,
    val student: StudentProfileResponse?,
    val teacher: TeacherProfileResponse?
)

data class CommonProfileResponse (
    val accountId: Long,
    val name: String,
    val email: String,
    val profileImage: String
)

data class StudentProfileResponse (
    val studentNumber: Int,
    val department: DepartmentType
)

class TeacherProfileResponse()