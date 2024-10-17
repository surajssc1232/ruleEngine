package org.example.ruleengine.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.example.ruleengine.model.Node;
import org.example.ruleengine.model.Rule;
import org.example.ruleengine.repository.NodeRepository;
import org.example.ruleengine.repository.RuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RuleService {

    private final RuleRepository ruleRepository;
    private final NodeRepository nodeRepository;
    private static final Logger logger = LoggerFactory.getLogger(RuleService.class);

    private static final Pattern RULE_PATTERN = Pattern.compile("^(\\w+)\\s*(>|<|=|>=|<=|!=)\\s*([\\w'\"]+)$");
    private static final Set<String> VALID_OPERATORS = new HashSet<>(Arrays.asList(">", "<", "=", ">=", "<=", "!="));

    @Autowired
    public RuleService(RuleRepository ruleRepository, NodeRepository nodeRepository) {
        this.ruleRepository = ruleRepository;
        this.nodeRepository = nodeRepository;
    }

    public Rule createRule(Rule rule) {
        validateRule(rule);
        if (rule.getRootNode() != null) {
            setupNodeRelationships(rule, rule.getRootNode());
        }
        return ruleRepository.save(rule);
    }

    public Rule updateRule(Long id, Rule updatedRule) {
        validateRule(updatedRule);
        Rule existingRule = ruleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rule not found with id: " + id));

        existingRule.setName(updatedRule.getName());
        if (updatedRule.getRootNode() != null) {
            setupNodeRelationships(existingRule, updatedRule.getRootNode());
        }

        return ruleRepository.save(existingRule);
    }

    private void setupNodeRelationships(Rule rule, Node node) {
        node.setRule(rule);
        rule.getNodes().add(node);
        
        if (node.getLeft() != null) {
            setupNodeRelationships(rule, node.getLeft());
        }
        if (node.getRight() != null) {
            setupNodeRelationships(rule, node.getRight());
        }
    }

    private void validateRule(Rule rule) {
        if (rule.getName() == null || rule.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Rule name cannot be null or empty");
        }
        if (rule.getRootNode() == null) {
            throw new IllegalArgumentException("Root node cannot be null");
        }
        validateNode(rule.getRootNode());
    }

    private void validateNode(Node node) {
        if (node.getType() == null || node.getType().trim().isEmpty()) {
            throw new IllegalArgumentException("Node type cannot be null or empty");
        }
        if (node.getNodeValue() == null || node.getNodeValue().trim().isEmpty()) {
            throw new IllegalArgumentException("Node value cannot be null or empty");
        }
        if (node.getType().equals("condition")) {
            validateCondition(node.getNodeValue());
        } else if (node.getType().equals("operator")) {
            if (!node.getNodeValue().equals("AND") && !node.getNodeValue().equals("OR")) {
                throw new IllegalArgumentException("Invalid operator: " + node.getNodeValue());
            }
            if (node.getLeft() == null || node.getRight() == null) {
                throw new IllegalArgumentException("Operator node must have both left and right child nodes");
            }
            validateNode(node.getLeft());
            validateNode(node.getRight());
        } else {
            throw new IllegalArgumentException("Invalid node type: " + node.getType());
        }
    }

    private void validateCondition(String condition) {
        Matcher matcher = RULE_PATTERN.matcher(condition);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid condition format: " + condition);
        }
        String operator = matcher.group(2);
        if (!VALID_OPERATORS.contains(operator)) {
            throw new IllegalArgumentException("Invalid operator in condition: " + operator);
        }
    }

    public Rule combineRules(List<Long> ruleIds) {
        if (ruleIds == null || ruleIds.size() < 2) {
            throw new IllegalArgumentException("At least two rule IDs are required for combination");
        }

        List<Rule> rulesToCombine = ruleRepository.findAllById(ruleIds);

        if (rulesToCombine.size() != ruleIds.size()) {
            logger.error("Not all requested rules were found. Requested: {}, Found: {}", ruleIds.size(), rulesToCombine.size());
            throw new IllegalArgumentException("One or more requested rules were not found");
        }

        try {
            Node combinedNode = rulesToCombine.get(0).getRootNode();
            for (int i = 1; i < rulesToCombine.size(); i++) {
                Node newNode = new Node();
                newNode.setType("operator");
                newNode.setNodeValue("AND");
                newNode.setLeft(combinedNode);
                newNode.setRight(rulesToCombine.get(i).getRootNode());
                combinedNode = nodeRepository.save(newNode);
            }

            Rule combinedRule = new Rule();
            combinedRule.setName("Combined Rule");
            combinedRule.setRootNode(combinedNode);
            return ruleRepository.save(combinedRule);
        } catch (Exception e) {
            logger.error("Error occurred while combining rules: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to combine rules", e);
        }
    }

    public boolean evaluateRule(Long ruleId, Map<String, Object> data) {
        if (ruleId == null) {
            throw new IllegalArgumentException("Rule ID cannot be null");
        }
        validateData(data);
        Rule rule = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new RuntimeException("Rule not found with ID: " + ruleId));
        return evaluateNode(rule.getRootNode(), data);
    }

    private void validateData(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Data map cannot be null or empty");
        }
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (entry.getKey() == null || entry.getKey().trim().isEmpty()) {
                throw new IllegalArgumentException("Data key cannot be null or empty");
            }
            if (entry.getValue() == null) {
                throw new IllegalArgumentException("Data value cannot be null for key: " + entry.getKey());
            }
            if (!(entry.getValue() instanceof String || entry.getValue() instanceof Number)) {
                throw new IllegalArgumentException("Data value must be a String or Number for key: " + entry.getKey());
            }
        }
    }

    public Node parseRuleString(String ruleString) {
        if (ruleString == null || ruleString.trim().isEmpty()) {
            throw new IllegalArgumentException("Rule expression cannot be null or empty");
        }
        ruleString = ruleString.replaceAll("\\s+", " ").trim();
        Queue<String> tokens = tokenize(ruleString);
        return parseExpression(tokens);
    }

    private Queue<String> tokenize(String ruleString) {
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (char c : ruleString.toCharArray()) {
            if (c == '"' || c == '\'') {
                inQuotes = !inQuotes;
                sb.append(c);
            } else if (!inQuotes && (c == '(' || c == ')' || c == ' ')) {
                if (sb.length() > 0) {
                    tokens.add(sb.toString());
                    sb.setLength(0);
                }
                if (c != ' ') {
                    tokens.add(String.valueOf(c));
                }
            } else {
                sb.append(c);
            }
        }

        if (sb.length() > 0) {
            tokens.add(sb.toString());
        }

        return new LinkedList<>(tokens);
    }

    private Node parseExpression(Queue<String> tokens) {
        if (tokens.isEmpty()) {
            throw new IllegalArgumentException("Invalid rule expression: empty token list");
        }
        Node left = parseTerm(tokens);
        while (!tokens.isEmpty() && (tokens.peek().equals("AND") || tokens.peek().equals("OR"))) {
            String operator = tokens.poll();
            Node right = parseTerm(tokens);
            Node newNode = new Node();
            newNode.setType("operator");
            newNode.setNodeValue(operator);
            newNode.setLeft(left);
            newNode.setRight(right);
            left = newNode;
        }
        return left;
    }

    private Node parseTerm(Queue<String> tokens) {
        if (tokens.isEmpty()) {
            throw new IllegalArgumentException("Unexpected end of expression");
        }
        if (tokens.peek().equals("(")) {
            tokens.poll(); // Remove opening parenthesis
            Node node = parseExpression(tokens);
            if (tokens.isEmpty() || !tokens.poll().equals(")")) {
                throw new IllegalArgumentException("Mismatched parentheses");
            }
            return node;
        } else {
            String attribute = tokens.poll();
            if (tokens.isEmpty()) {
                throw new IllegalArgumentException("Incomplete condition: missing operator and value");
            }
            String operator = tokens.poll();
            if (tokens.isEmpty()) {
                throw new IllegalArgumentException("Incomplete condition: missing value");
            }
            String value = tokens.poll();
            
            if (!VALID_OPERATORS.contains(operator)) {
                throw new IllegalArgumentException("Invalid operator: " + operator);
            }
            Node node = new Node();
            node.setType("condition");
            node.setNodeValue(attribute + " " + operator + " " + value);
            return node;
        }
    }

    private boolean evaluateNode(Node node, Map<String, Object> data) {
        if (node.getType().equals("operator")) {
            boolean leftResult = evaluateNode(node.getLeft(), data);
            boolean rightResult = evaluateNode(node.getRight(), data);
            return node.getNodeValue().equals("AND") ? leftResult && rightResult : leftResult || rightResult;
        } else {
            String[] parts = node.getNodeValue().split(" ", 3);
            String attribute = parts[0];
            String operator = parts[1];
            String value = parts[2].replaceAll("^['\"]|['\"]$", ""); // Remove surrounding quotes if present
            Object dataValue = data.get(attribute);
            
            if (dataValue == null) {
                throw new IllegalArgumentException("Attribute not found in data: " + attribute);
            }

            return switch (operator) {
                case ">" -> compareValues(dataValue, value) > 0;
                case "<" -> compareValues(dataValue, value) < 0;
                case "=" -> compareValues(dataValue, value) == 0;
                case ">=" -> compareValues(dataValue, value) >= 0;
                case "<=" -> compareValues(dataValue, value) <= 0;
                case "!=" -> compareValues(dataValue, value) != 0;
                default -> throw new IllegalArgumentException("Unsupported operator: " + operator);
            };
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
