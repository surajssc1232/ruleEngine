package org.example.ruleengine.service;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.example.ruleengine.model.Node;
import org.example.ruleengine.model.Rule;
import org.example.ruleengine.repository.RuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RuleService {

    @Autowired
    private RuleRepository ruleRepository;

    public Rule createRule(String ruleString) {
        if (ruleString == null || ruleString.trim().isEmpty()) {
            throw new IllegalArgumentException("Rule string cannot be null or empty");
        }
        try {
            Node rootNode = parseRuleString(ruleString);
            Rule rule = new Rule();
            rule.setName("Rule " + (ruleRepository.count() + 1));
            rule.setRootNode(rootNode);
            return ruleRepository.save(rule);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid rule string: " + e.getMessage(), e);
        }
    }           

    public Rule combineRules(List<Long> ruleIds) {
        List<Rule> rules = ruleRepository.findAllById(ruleIds);
        if (rules.size() < 2) {
            throw new IllegalArgumentException("At least two rules are required for combination");
        }
    
        Node combinedNode = rules.get(0).getRootNode();
        for (int i = 1; i < rules.size(); i++) {
            Node newNode = new Node();
            newNode.setType("operator");
            newNode.setValue("AND");
            newNode.setLeft(combinedNode);
            newNode.setRight(rules.get(i).getRootNode());
            combinedNode = newNode;
        }
    
        Rule combinedRule = new Rule();
        combinedRule.setName("Combined Rule");
        combinedRule.setRootNode(combinedNode);
        return ruleRepository.save(combinedRule);
    }

    public boolean evaluateRule(Long ruleId, Map<String, Object> data) {
        if (ruleId == null) {
            throw new IllegalArgumentException("Rule ID cannot be null");
        }
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Data map cannot be null or empty");
        }
        Rule rule = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new RuntimeException("Rule not found with ID: " + ruleId));
        return evaluateNode(rule.getRootNode(), data);
    }

    private Node parseRuleString(String ruleString) {
        ruleString = ruleString.replaceAll("\\s+", " ").trim();
        return parseExpression(new LinkedList<>(Arrays.asList(ruleString.split(" "))));
    }

    private Node parseExpression(Queue<String> tokens) {
        Node left = parseTerm(tokens);
        while (!tokens.isEmpty() && (tokens.peek().equals("AND") || tokens.peek().equals("OR"))) {
            String operator = tokens.poll();
            Node right = parseTerm(tokens);
            Node newNode = new Node();
            newNode.setType("operator");
            newNode.setValue(operator);
            newNode.setLeft(left);
            newNode.setRight(right);
            left = newNode;
        }
        return left;
    }

    private Node parseTerm(Queue<String> tokens) {
        if (tokens.peek().equals("(")) {
            tokens.poll(); // Remove opening parenthesis
            Node node = parseExpression(tokens);
            tokens.poll(); // Remove closing parenthesis
            return node;
        } else {
            Node node = new Node();
            node.setType("condition");
            node.setValue(tokens.poll() + " " + tokens.poll() + " " + tokens.poll());
            return node;
        }
    }

    private boolean evaluateNode(Node node, Map<String, Object> data) {
        if (node.getType().equals("operator")) {
            boolean leftResult = evaluateNode(node.getLeft(), data);
            boolean rightResult = evaluateNode(node.getRight(), data);
            return node.getValue().equals("AND") ? leftResult && rightResult : leftResult || rightResult;
        } else {
            String[] parts = node.getValue().split(" ");
            String attribute = parts[0];
            String operator = parts[1];
            String value = parts[2];
            Object dataValue = data.get(attribute);
            
            if (dataValue == null) {
                throw new IllegalArgumentException("Attribute not found in data: " + attribute);
            }
    
            switch (operator) {
                case ">" -> {
                    return compareValues(dataValue, value) > 0;
                }
                case "<" -> {
                    return compareValues(dataValue, value) < 0;
                }
                case "=" -> {
                    return compareValues(dataValue, value) == 0;
                }
                case ">=" -> {
                    return compareValues(dataValue, value) >= 0;
                }
                case "<=" -> {
                    return compareValues(dataValue, value) <= 0;
                }
                case "!=" -> {
                    return compareValues(dataValue, value) != 0;
                }
                default -> throw new IllegalArgumentException("Unsupported operator: " + operator);
            }
        }
    }

    private int compareValues(Object value1, Object value2) {
        if (value1 instanceof Number && value2 instanceof String) {
            return Double.compare(((Number) value1).doubleValue(), Double.parseDouble((String) value2));
        } else if (value1 instanceof String && value2 instanceof String) {
            return ((String) value1).compareTo((String) value2);
        } else {
            throw new IllegalArgumentException("Unsupported value types for comparison");
        }
    }
}
