package org.example.ruleengine.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.example.ruleengine.model.Node;
import org.example.ruleengine.model.Rule;
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

    @InjectMocks
    private RuleService ruleService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateRule() {
        String ruleString = "age > 30 AND department = Sales";
        Rule savedRule = new Rule();
        savedRule.setId(1L);
        savedRule.setName("Rule 1");

        when(ruleRepository.count()).thenReturn(0L);
        when(ruleRepository.save(any(Rule.class))).thenReturn(savedRule);

        Rule result = ruleService.createRule(ruleString);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Rule 1", result.getName());
        verify(ruleRepository, times(1)).save(any(Rule.class));
    }

    @Test
    void testEvaluateRule() {
        Long ruleId = 1L;
        Rule rule = new Rule();
        rule.setId(ruleId);
        
        Node rootNode = new Node();
        rootNode.setType("condition");
        rootNode.setValue("age > 30");
        rule.setRootNode(rootNode);

        Map<String, Object> data = new HashMap<>();
        data.put("age", 35);

        when(ruleRepository.findById(ruleId)).thenReturn(Optional.of(rule));

        boolean result = ruleService.evaluateRule(ruleId, data);

        assertTrue(result);
        verify(ruleRepository, times(1)).findById(ruleId);
    }
}
