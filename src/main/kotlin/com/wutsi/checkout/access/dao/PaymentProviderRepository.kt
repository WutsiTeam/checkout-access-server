package com.wutsi.checkout.access.dao

import com.wutsi.checkout.access.entity.PaymentProviderEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface PaymentProviderRepository : CrudRepository<PaymentProviderEntity, Long>
