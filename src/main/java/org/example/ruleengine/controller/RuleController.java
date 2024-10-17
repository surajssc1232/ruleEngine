package org.example.ruleengine.controller;

import java.util.List;
import java.util.Map;

import org.example.ruleengine.model.Rule;
import org.example.ruleengine.service.RuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rules")
public class RuleController {

    @Autowired
    private RuleService ruleService;

    @PostMapping
    public ResponseEntity<Rule> createRule(@RequestBody String ruleString) {
        Rule createdRule = ruleService.createRule(ruleString);
        return ResponseEntity.ok(createdRule);
    }

    @PostMapping("/combine")
    public ResponseEntity<Rule> combineRules(@RequestBody List<Long> ruleIds) {
        Rule combinedRule = ruleService.combineRules(ruleIds);
        return ResponseEntity.ok(combinedRule);
    }

    @PostMapping("/{ruleId}/evaluate")
    public ResponseEntity<Boolean> evaluateRule(@PathVariable Long ruleId, @RequestBody Map<String, Object> data) {
        boolean result = ruleService.evaluateRule(ruleId, data);
        return ResponseEntity.ok(result);
    }
}
