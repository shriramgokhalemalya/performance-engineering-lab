package com.perflab.login.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class AllocationController {

    @GetMapping("/allocate/{mb}")
    public String allocate(@PathVariable int mb) {
        List<byte[]> allocations = new ArrayList<>();

        for (int i = 0; i < mb; i++) {
            allocations.add(new byte[1024 * 1024]);
        }

        return "Allocated " + mb + " MB";
    }
}
