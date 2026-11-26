package com.example.hotel.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.hotel.dto.GuestRequest;
import com.example.hotel.dto.GuestResponse;
import com.example.hotel.dto.MessageResponse;
import com.example.hotel.service.GuestService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/guests")
public class GuestController {

    private final GuestService guestService;

    public GuestController(GuestService guestService) {
        this.guestService = guestService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GuestResponse create(@Valid @RequestBody GuestRequest request) {
        return guestService.create(request);
    }

    @GetMapping
    public List<GuestResponse> list() {
        return guestService.list();
    }

    @GetMapping("/{id}")
    public GuestResponse get(@PathVariable Long id) {
        return guestService.get(id);
    }

    @PutMapping("/{id}")
    public GuestResponse update(@PathVariable Long id, @Valid @RequestBody GuestRequest request) {
        return guestService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public MessageResponse delete(@PathVariable Long id) {
        guestService.delete(id);
        return new MessageResponse("Guest " + id + " deleted");
    }
}