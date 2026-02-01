package cz.splnsito.mrthom.loglantern.feature.auth.model

import com.google.gson.annotations.SerializedName

data class SplunkTokenJsonResponse(
    val entry: List<TokenEntry> = emptyList(),
    val paging: Paging? = null
)

data class TokenEntry(
    val name: String? = null,
    val id: String? = null,
    val updated: String? = null,
    val author: String? = null,
    val content: TokenContent? = null
)

data class TokenContent(
    val id: String? = null,
    val token: String? = null,
    @SerializedName("eai:acl")
    val eaiAcl: Any? = null
)

data class Paging(
    val total: Int = 0,
    val perPage: Int = 0,
    val offset: Int = 0
)
