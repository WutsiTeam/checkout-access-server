package com.wutsi.checkout.access.dto

import kotlin.collections.List

public data class ListCategoryResponse(
    public val categories: List<Category> = emptyList()
)
