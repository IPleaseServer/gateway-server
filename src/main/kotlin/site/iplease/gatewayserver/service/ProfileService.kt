package site.iplease.gatewayserver.service

import site.iplease.gatewayserver.data.response.ProfileResponse

interface ProfileService {
    fun getProfileByAccessToken(accessToken: String): ProfileResponse
    fun existProfileByAccessToken(token: String): Boolean
}
