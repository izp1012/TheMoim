package com.moim.payment.controller;


import com.moim.payment.domain.Usr.Usr;
import com.moim.payment.domain.UsrMoim;
import com.moim.payment.dto.UsrMoimReq;
import com.moim.payment.service.UsrMoimService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class UsrMoimController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final UsrMoimService usrMoimService;

    @PostMapping("/users")
    public ResponseEntity<Usr> addUser(@RequestBody UsrMoimReq usrMoimReq) {
        logger.debug("User added: " + usrMoimReq.getUserName());
        Usr usr = usrMoimService.findByUsrName(usrMoimReq.getUserName());

        return ResponseEntity.ok(usr);
    }

    @GetMapping
    public ResponseEntity<List<UsrMoim>> getList(@RequestParam String name) {
        return ResponseEntity.ok(usrMoimService.getList(name));
    }
}

