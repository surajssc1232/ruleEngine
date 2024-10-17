package org.example.ruleengine.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.example.ruleengine.model.Node;
import org.example.ruleengine.model.Rule;
import org.example.ruleengine.repository.NodeRepository;
import org.example.ruleengine.repository.RuleRepository;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

class RuleServiceTest {

    @Mock
    private RuleRepository ruleRepository;

    @Mock
    private NodeRepository nodeRepository;

    @InjectMocks
    private RuleService ruleService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateRule() {
        Rule inputRule = new Rule();
        inputRule.setName("Rule 1");
        Node rootNode = new Node();
        rootNode.setType("condition");
        rootNode.setNodeValue("age > 30");
        inputRule.setRootNode(rootNode);

        Rule savedRule = new Rule();
        savedRule.setId(1L);
        savedRule.setName("Rule 1");
        savedRule.setRootNode(rootNode);

        when(nodeRepository.save(any(Node.class))).thenReturn(rootNode);
        when(ruleRepository.save(any(Rule.class))).thenReturn(savedRule);

        Rule result = ruleService.createRule(inputRule);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Rule 1", result.getName());
        verify(nodeRepository, times(1)).save(any(Node.class));
        verify(ruleRepository, times(1)).save(any(Rule.class));
    }

    @Test
    void testEvaluateRule() {
        Long ruleId = 1L;
        Rule rule = new Rule();
        rule.setId(ruleId);
        
        Node rootNode = new Node();
        rootNode.setType("condition");
        rootNode.setNodeValue("age > 30");
        rule.setRootNode(rootNode);

        Map<String, Object> data = new HashMap<>();
        data.put("age", 35);

        when(ruleRepository.findById(ruleId)).thenReturn(Optional.of(rule));

        boolean result = ruleService.evaluateRule(ruleId, data);

        assertTrue(result);
        verify(ruleRepository, times(1)).findById(ruleId);
    }
}
