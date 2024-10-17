const API_URL = 'http://localhost:8080/api/rules';

async function createRule() {
    const name = document.getElementById('ruleName').value;
    const expression = document.getElementById('ruleExpression').value;

    try {
        const response = await fetch(API_URL, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ name, expression }),
        });

        const result = await response.json();
        displayResult('Rule created', result);
    } catch (error) {
        displayResult('Error creating rule', error);
    }
}

async function evaluateRule() {
    const ruleId = document.getElementById('ruleId').value;
    const data = JSON.parse(document.getElementById('evaluationData').value);

    try {
        const response = await fetch(`${API_URL}/${ruleId}/evaluate`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(data),
        });

        const result = await response.json();
        displayResult('Rule evaluation result', result);
    } catch (error) {
        displayResult('Error evaluating rule', error);
    }
}

async function combineRules() {
    const ruleIds = document.getElementById('ruleIds').value.split(',').map(id => parseInt(id.trim()));

    try {
        const response = await fetch(`${API_URL}/combine`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(ruleIds),
        });

        const result = await response.json();
        displayResult('Rules combined', result);
    } catch (error) {
        displayResult('Error combining rules', error);
    }
}

function displayResult(title, content) {
    const resultDiv = document.getElementById('result');
    resultDiv.innerHTML = `<h3>${title}</h3><pre>${JSON.stringify(content, null, 2)}</pre>`;
}
