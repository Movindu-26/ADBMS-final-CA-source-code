package com.example.hotel.controller;

import com.example.hotel.dto.MessageResponse;
import com.example.hotel.dto.RoomRequest;
import com.example.hotel.dto.RoomResponse;
import com.example.hotel.entity.RoomType;
import com.example.hotel.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoomResponse create(@Valid @RequestBody RoomRequest request) {
        return roomService.create(request);
    }

    @GetMapping
    public List<RoomResponse> list(@RequestParam(required = false) String branch,
                                   @RequestParam(required = false) RoomType type) {
        return roomService.list(branch, type);
    }

    /** Temporal query: free rooms of a branch for a date range. Declared before /{id}. */
    @GetMapping("/available")
    public List<RoomResponse> available(
            @RequestParam String branch,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {
        return roomService.findAvailable(branch, checkIn, checkOut);
    }

    @GetMapping("/{id}")
    public RoomResponse get(@PathVariable Long id) {
        return roomService.get(id);
    }

    @PutMapping("/{id}")
    public RoomResponse update(@PathVariable Long id, @Valid @RequestBody RoomRequest request) {
        return roomService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public MessageResponse delete(@PathVariable Long id) {
        roomService.delete(id);
        return new MessageResponse("Room " + id + " deleted");
    }
}