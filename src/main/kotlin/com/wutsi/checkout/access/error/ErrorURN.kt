package com.wutsi.checkout.access.error

enum class ErrorURN(val urn: String) {
    BUSINESS_NOT_FOUND("urn:wutsi:error:checkout-access:business-not-found"),

    FEES_NOT_FOUND("urn:wutsi:error:checkout-access:fees-not-found"),

    ORDER_NOT_FOUND("urn:wutsi:error:checkout-access:order-not-found"),

    PAYMENT_GATEWAY_NOT_SUPPORTED("urn:wutsi:error:checkout-access:payment-gateway-not-supported"),

    PAYMENT_METHOD_ALREADY_ASSIGNED("urn:wutsi:error:checkout-access:payment-method-already-assigned"),
    PAYMENT_METHOD_NOT_FOUND("urn:wutsi:error:checkout-access:payment-method-not-found"),

    STATUS_NOT_VALID("urn:wutsi:error:checkout-access:status-not-valid"),

    TRANSACTION_FAILED("urn:wutsi:error:checkout-access:transaction-failed"),
    TRANSACTION_NOT_FOUND("urn:wutsi:error:checkout-access:transaction-not-found")
}
