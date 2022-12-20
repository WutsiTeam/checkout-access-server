package com.wutsi.checkout.access.service

import com.wutsi.checkout.access.dao.PaymentProviderRepository
import com.wutsi.checkout.access.dto.SearchPaymentProviderRequest
import com.wutsi.checkout.access.entity.PaymentProviderEntity
import com.wutsi.checkout.access.entity.PaymentProviderPrefixEntity
import com.wutsi.checkout.access.error.ErrorURN
import com.wutsi.enums.PaymentMethodType
import com.wutsi.platform.core.error.Error
import com.wutsi.platform.core.error.Parameter
import com.wutsi.platform.core.error.exception.NotFoundException
import org.springframework.stereotype.Service
import javax.persistence.EntityManager
import javax.persistence.Query

@Service
class PaymentProviderService(
    private val dao: PaymentProviderRepository,
    private val em: EntityManager,
) {
    fun findById(id: Long): PaymentProviderEntity =
        dao.findById(id)
            .orElseThrow {
                NotFoundException(
                    error = Error(
                        code = ErrorURN.PAYMENT_PROVIDER_NOT_FOUND.urn,
                        parameter = Parameter(
                            value = id,
                        ),
                    ),
                )
            }

    fun search(request: SearchPaymentProviderRequest): List<PaymentProviderEntity> {
        val sql = sql(request)
        val query = em.createQuery(sql)
        parameters(request, query)
        return (query.resultList as List<PaymentProviderPrefixEntity>)
            .filter { (request.number === null) || request.number.startsWith(it.numberPrefix) }
            .map { it.provider }
            .associateBy { it.id }
            .map { it.value }
    }

    private fun sql(request: SearchPaymentProviderRequest): String {
        val select = select()
        val where = where(request)
        return if (where.isNullOrEmpty()) {
            select
        } else {
            "$select WHERE $where"
        }
    }

    private fun select(): String =
        "SELECT a FROM PaymentProviderPrefixEntity a"

    private fun where(request: SearchPaymentProviderRequest): String {
        val criteria = mutableListOf<String>()

        if (!request.country.isNullOrEmpty()) {
            criteria.add("a.country=:country")
        }
        if (!request.type.isNullOrEmpty()) {
            criteria.add("a.provider.type=:type")
        }
        return criteria.joinToString(separator = " AND ")
    }

    private fun parameters(request: SearchPaymentProviderRequest, query: Query) {
        if (!request.country.isNullOrEmpty()) {
            query.setParameter("country", request.country)
        }
        if (!request.type.isNullOrEmpty()) {
            query.setParameter("type", PaymentMethodType.valueOf(request.type.uppercase()))
        }
    }
}
