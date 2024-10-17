package org.example.ruleengine.model;

import java.util.ArrayList;
import java.util.List;

import org.example.ruleengine.serializer.RuleSerializer;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "rules")
@JsonSerialize(using = RuleSerializer.class)
public class Rule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "root_node_id", referencedColumnName = "id")
    @JsonManagedReference
    private Node rootNode;

    @OneToMany(mappedBy = "rule", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Node> nodes = new ArrayList<>();

    // Add this method to properly set up the relationship
    public void setRootNode(Node rootNode) {
        this.rootNode = rootNode;
        if (rootNode != null) {
            rootNode.setRule(this);
            this.nodes.add(rootNode);
        }
    }
}
