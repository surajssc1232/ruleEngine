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

# Backend

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



# Frontend

## Using the Application

After starting the application, you can access the web interface by following these steps:

1. Open a web browser and navigate to `http://localhost:8080`.

2. You will see the Rule Engine Interface with three main sections:

   a. Create Rule:
      - Enter a rule name in the "Rule Name" field.
      - Enter the rule expression in the "Rule Expression" field.
      - Click the "Create Rule" button to create a new rule.

   b. Evaluate Rule:
      - Enter the rule ID in the "Rule ID" field.
      - Enter the evaluation data as a JSON object in the "Evaluation Data" field.
      - Click the "Evaluate Rule" button to evaluate the rule.

   c. Combine Rules:
      - Enter comma-separated rule IDs in the "Rule IDs" field.
      - Click the "Combine Rules" button to combine the specified rules.

3. The results of your actions will be displayed in the "Result" section at the bottom of the page.

### Example: Creating a Complex Rule

Let's create a complex rule that combines multiple conditions:

1. In the "Create Rule" section:
   - Rule Name: "Complex Employee Eligibility"
   - Rule Expression:
     ```
     ((age > 30 AND department = 'Sales') OR (age < 25 AND department = 'Marketing')) AND (salary > 50000 OR experience > 5)
     ```

2. Click "Create Rule". The system will return a rule ID, let's say it's 1.

3. To evaluate this rule, in the "Evaluate Rule" section:
   - Rule ID: 1
   - Evaluation Data:
     ```json
     {
       "age": 32,
       "department": "Sales",
       "salary": 55000,
       "experience": 4
     }
     ```

4. Click "Evaluate Rule". The result should be `true` because the employee is over 30, in Sales, and has a salary over 50000.

5. Try different combinations in the evaluation data to see how the rule behaves with various inputs.

Note: Ensure that the backend server is running on `http://localhost:8080` for the web interface to function correctly.


## Contributing

Contributions to the Rule Engine Application are welcome. Please feel free to submit pull requests or create issues for bugs and feature requests.


