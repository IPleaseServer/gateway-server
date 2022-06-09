package site.iplease.gatewayserver.data.dto

import site.iplease.gatewayserver.data.type.PermissionType

data class AccountDto (
    val accountId: Long,
    val permission: PermissionType,
)
