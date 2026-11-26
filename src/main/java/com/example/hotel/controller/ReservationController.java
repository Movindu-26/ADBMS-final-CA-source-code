package com.example.hotel.controller;

import com.example.hotel.dto.CreateReservationRequest;
import com.example.hotel.dto.ModifyReservationRequest;
import com.example.hotel.dto.ReservationResponse;
import com.example.hotel.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final boolean simulateFailureEnabled;

    public ReservationController(ReservationService reservationService,
                                 @Value("${booking.demo.simulate-failure-enabled:false}") boolean simulateFailureEnabled) {
        this.reservationService = reservationService;
        this.simulateFailureEnabled = simulateFailureEnabled;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse create(@Valid @RequestBody CreateReservationRequest request,
                                      @RequestHeader(value = "X-Simulate-Failure", required = false) String simulate) {
        boolean simulateFailure = simulateFailureEnabled && "true".equalsIgnoreCase(simulate);
        return reservationService.create(request, simulateFailure);
    }

    @GetMapping
    public List<ReservationResponse> list(@RequestParam(required = false) Long guestId) {
        return reservationService.list(guestId);
    }

    @GetMapping("/{id}")
    public ReservationResponse get(@PathVariable Long id) {
        return reservationService.get(id);
    }

    /** T2 */
    @PutMapping("/{id}")
    public ReservationResponse modify(@PathVariable Long id, @Valid @RequestBody ModifyReservationRequest request) {
        return reservationService.modify(id, request);
    }

    /** T3 */
    @PatchMapping("/{id}/cancel")
    public ReservationResponse cancel(@PathVariable Long id) {
        return reservationService.cancel(id);
    }
}
