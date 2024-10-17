package org.example.ruleengine.controller;

import java.util.List;
import java.util.Map;

import org.example.ruleengine.model.Node;
import org.example.ruleengine.model.Rule;
import org.example.ruleengine.service.RuleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rules")
@CrossOrigin(origins = "http://localhost:8080")
public class RuleController {

    @Autowired
    private RuleService ruleService;

    private static final Logger logger = LoggerFactory.getLogger(RuleController.class);

    @PostMapping
    public ResponseEntity<?> createRule(@RequestBody Map<String, Object> ruleData) {
        try {
            String ruleName = (String) ruleData.get("name");
            Object ruleExpression = ruleData.get("expression");
            
            if (ruleName == null || ruleExpression == null) {
                return ResponseEntity.badRequest().body("Rule name and expression are required");
            }

            Rule rule = new Rule();
            rule.setName(ruleName);
            
            try {
                Node rootNode;
                if (ruleExpression instanceof String) {
                    rootNode = ruleService.parseRuleString((String) ruleExpression);
                } else if (ruleExpression instanceof Map) {
                    rootNode = createNodeFromMap((Map<String, Object>) ruleExpression);
                } else {
                    throw new IllegalArgumentException("Invalid rule expression format");
                }
                rule.setRootNode(rootNode);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body("Invalid rule expression: " + e.getMessage());
            }
            
            Rule createdRule = ruleService.createRule(rule);
            return ResponseEntity.ok(createdRule);
        } catch (Exception e) {
            logger.error("Error creating rule", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while creating the rule: " + e.getMessage());
        }
    }

    private Node createNodeFromMap(Map<String, Object> nodeMap) {
        Node node = new Node();
        node.setType((String) nodeMap.get("type"));
        node.setNodeValue((String) nodeMap.get("nodeValue"));
        
        if (nodeMap.containsKey("left")) {
            node.setLeft(createNodeFromMap((Map<String, Object>) nodeMap.get("left")));
        }
        if (nodeMap.containsKey("right")) {
            node.setRight(createNodeFromMap((Map<String, Object>) nodeMap.get("right")));
        }
        
        return node;
    }

    @PostMapping("/combine")
    public ResponseEntity<?> combineRules(@RequestBody List<Long> ruleIds) {
        try {
            Rule combinedRule = ruleService.combineRules(ruleIds);
            return ResponseEntity.ok(combinedRule);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while combining rules: " + e.getMessage());
        }
    }

    @PostMapping("/{ruleId}/evaluate")
public ResponseEntity<?> evaluateRule(@PathVariable Long ruleId, @RequestBody Map<String, Object> data) {
    try {
        boolean result = ruleService.evaluateRule(ruleId, data);
        return ResponseEntity.ok(result);
    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    } catch (RuntimeException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Rule not found with ID: " + ruleId);
    }
}
}
