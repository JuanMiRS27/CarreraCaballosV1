package com.example.carreracaballos.api;

import com.example.carreracaballos.service.RaceGameService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/games")
public class RaceGameController {
    private final RaceGameService raceGameService;

    public RaceGameController(RaceGameService raceGameService) {
        this.raceGameService = raceGameService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GameStateResponse create(@Valid @RequestBody CreateGameRequest request) {
        return raceGameService.createGame(
                request.userId(),
                request.distance(),
                request.horses(),
                request.selectedHorse(),
                request.betPoints()
        );
    }

    @GetMapping("/{id}")
    public GameStateResponse get(@PathVariable UUID id) {
        return raceGameService.getGame(id);
    }

    @PostMapping("/{id}/step")
    public GameStateResponse step(@PathVariable UUID id) {
        return raceGameService.step(id);
    }
}
