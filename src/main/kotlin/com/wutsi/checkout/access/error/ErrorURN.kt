package com.wutsi.checkout.access.error

enum class ErrorURN(val urn: String) {
    BUSINESS_NOT_FOUND("urn:wutsi:error:checkout-access:business-not-found"),

    ORDER_NOT_FOUND("urn:wutsi:error:checkout-access:order-not-found"),

    PAYMENT_METHOD_ALREADY_ASSIGNED("urn:wutsi:error:checkout-access:payment-method-already-assigned"),
    PAYMENT_METHOD_NOT_FOUND("urn:wutsi:error:checkout-access:payment-method-not-found"),

    STATUS_NOT_VALID("urn:wutsi:error:checkout-access:status-not-valid")
}
