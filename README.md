# Rule Engine Application

This Rule Engine Application is a Spring Boot-based system that allows users to create, evaluate, and combine rules dynamically.

## Installation

1. Clone the repository:
   ```
   git clone https://github.com/yourusername/rule-engine.git
   ```

2. Navigate to the project directory:
   ```
   cd rule-engine
   ```

3. Build the project using Maven:
   ```
   mvn clean install
   ```

4. Run the application:
   ```
   java -jar target/ruleEngine-0.0.1-SNAPSHOT.jar
   ```

5. Open a web browser and go to `http://localhost:8080` to access the application.

## Usage

The application provides a simple web interface with three main functions:

### 1. Create Rule

- **Input Format**: 
  - Rule Name: A string to identify the rule.
  - Rule Expression: A string or JSON object representing the rule logic.

- **Description**: Creates a new rule in the system. The rule can be a simple condition or a complex logical expression.

- **Example**:
  ```json
  {
    "name": "Age Check",
    "expression": "age > 30"
  }
  ```
  or
  ```json
  {
    "name": "Complex Rule",
    "expression": {
      "type": "operator",
      "nodeValue": "AND",
      "left": {
        "type": "condition",
        "nodeValue": "age > 30"
      },
      "right": {
        "type": "condition",
        "nodeValue": "salary >= 50000"
      }
    }
  }
  ```

### 2. Evaluate Rule

- **Input Format**:
  - Rule ID: The ID of the rule to evaluate.
  - Evaluation Data: A JSON object containing the data to evaluate against the rule.

- **Description**: Evaluates a specific rule against provided data and returns a boolean result.

- **Example**:
  ```json
  {
    "ruleId": 1,
    "data": {
      "age": 35,
      "salary": 60000
    }
  }
  ```

### 3. Combine Rules

- **Input Format**: An array of rule IDs to combine.

- **Description**: Combines multiple existing rules into a new rule using AND logic.

- **Example**:
  ```json
  [1, 2, 3]
  ```

## API Endpoints

- Create Rule: POST `/api/rules`
- Evaluate Rule: POST `/api/rules/{ruleId}/evaluate`
- Combine Rules: POST `/api/rules/combine`

For detailed API documentation, please refer to the API documentation section (if available).

## Contributing

Contributions to the Rule Engine Application are welcome. Please feel free to submit pull requests or create issues for bugs and feature requests.

## License

[Specify your license here]
