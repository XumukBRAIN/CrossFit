package com.example.crossFit.controller;

import com.example.crossFit.config.SwaggerConfig;
import com.example.crossFit.model.dto.CoachDTO;
import com.example.crossFit.model.entity.Coach;
import com.example.crossFit.model.mapStruct.CoachMapper;
import com.example.crossFit.service.CoachService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/coach")
@Api(tags = SwaggerConfig.COACH_TAG)
public class CoachController {

    private final CoachService coachService;
    private final CoachMapper coachMapper;

    @Autowired
    public CoachController(CoachService coachService, CoachMapper coachMapper) {
        this.coachService = coachService;
        this.coachMapper = coachMapper;
    }

    @ApiOperation("Метод для поиска тренера по ID")
    @GetMapping("/getOne/{id}")
    public ResponseEntity<CoachDTO> getCoach(@PathVariable Integer id) {
        Coach coach = coachService.getCoach(id);
        return ResponseEntity.ok(coachMapper.toCoachDTO(coach));
    }

    @ApiOperation("Метод для поиска тренера по имени")
    @GetMapping("/getCoach")
    public ResponseEntity<CoachDTO> getCoachByName(@RequestParam String name) {
        Coach coach = coachService.findByName(name);
        return ResponseEntity.ok(coachMapper.toCoachDTO(coach));
    }

    @ApiOperation("Метод для добавления тренера в базу")
    @PostMapping("/createCoach")
    public ResponseEntity<String> createCoach(@RequestBody CoachDTO coachDTO) {
        Coach coach = coachMapper.toCoach(coachDTO);
        return ResponseEntity.ok(coachService.createCoach(coach));
    }

    @ApiOperation("Метод для удаления тренера")
    @DeleteMapping("/deleteCoach")
    public ResponseEntity<String> deleteCoach(@RequestParam Integer id) {
        return ResponseEntity.ok(coachService.deleteCoach(id));
    }

}
