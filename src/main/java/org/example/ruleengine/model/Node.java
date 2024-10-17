package org.example.ruleengine.model;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "nodes")
public class Node {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;
    
    @Column(name = "node_value")
    private String nodeValue;

    @ManyToOne
    @JoinColumn(name = "rule_id")
    @JsonBackReference
    private Rule rule;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "left_node_id")
    private Node left;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "right_node_id")
    private Node right;

    // Getters and setters
}
