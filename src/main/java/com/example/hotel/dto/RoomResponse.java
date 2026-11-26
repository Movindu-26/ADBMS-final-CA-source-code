package com.example.hotel.dto;

import com.example.hotel.entity.Room;
import com.example.hotel.entity.RoomStatus;
import com.example.hotel.entity.RoomType;

import java.math.BigDecimal;

public record RoomResponse(Long id, String branchCode, String roomNumber, RoomType roomType, Integer capacity,
                           BigDecimal pricePerNight, RoomStatus status) {

    public static RoomResponse from(Room r) {
        return new RoomResponse(r.getId(), r.getBranchCode(), r.getRoomNumber(), r.getRoomType(),
                r.getCapacity(), r.getPricePerNight(), r.getStatus());
    }
}
