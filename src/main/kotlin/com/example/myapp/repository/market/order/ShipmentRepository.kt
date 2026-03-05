package com.example.myapp.repository.market.order

import com.example.myapp.entity.market.order.Shipment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ShipmentRepository : JpaRepository<Shipment, Long>

