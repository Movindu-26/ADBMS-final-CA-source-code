package com.example.hotel.service;

import com.example.hotel.dto.GuestRequest;
import com.example.hotel.dto.GuestResponse;
import com.example.hotel.entity.Guest;
import com.example.hotel.exception.ConflictException;
import com.example.hotel.exception.ResourceNotFoundException;
import com.example.hotel.repository.GuestRepository;
import com.example.hotel.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class GuestService {

    private final GuestRepository guestRepository;
    private final ReservationRepository reservationRepository;

    public GuestService(GuestRepository guestRepository, ReservationRepository reservationRepository) {
        this.guestRepository = guestRepository;
        this.reservationRepository = reservationRepository;
    }

    public GuestResponse create(GuestRequest req) {
        String email = req.email().trim().toLowerCase();
        if (guestRepository.existsByEmail(email)) {
            throw new ConflictException("A guest with email " + email + " already exists");
        }
        Guest guest = new Guest();
        apply(guest, req);
        return GuestResponse.from(guestRepository.save(guest));
    }

    @Transactional(readOnly = true)
    public GuestResponse get(Long id) {
        return GuestResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public List<GuestResponse> list() {
        return guestRepository.findAll().stream().map(GuestResponse::from).toList();
    }

    public GuestResponse update(Long id, GuestRequest req) {
        Guest guest = find(id);
        String email = req.email().trim().toLowerCase();
        if (!guest.getEmail().equals(email) && guestRepository.existsByEmail(email)) {
            throw new ConflictException("A guest with email " + email + " already exists");
        }
        apply(guest, req);
        return GuestResponse.from(guestRepository.saveAndFlush(guest));
    }

    public void delete(Long id) {
        Guest guest = find(id);
        if (reservationRepository.existsByGuest_Id(id)) {
            throw new ConflictException("Guest has reservations and cannot be deleted");
        }
        guestRepository.delete(guest);
    }

    private Guest find(Long id) {
        return guestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Guest " + id + " not found"));
    }

    private void apply(Guest guest, GuestRequest req) {
        guest.setFullName(req.fullName().trim());
        guest.setEmail(req.email().trim().toLowerCase());
        guest.setPhone(req.phone().trim());
        guest.setIdNumber(req.idNumber().trim());
    }
}
