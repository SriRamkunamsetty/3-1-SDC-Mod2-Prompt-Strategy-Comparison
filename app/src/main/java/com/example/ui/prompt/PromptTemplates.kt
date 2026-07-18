package com.example.ui.prompt

data class PromptTemplate(
    val id: String,
    val name: String,
    val description: String,
    val defaultInput: String,
    val zeroShotTemplate: (String) -> String,
    val fewShotTemplate: (String) -> String,
    val structuredTemplate: (String) -> String
)

object PromptTemplates {
    val templates = listOf(
        PromptTemplate(
            id = "email_triage",
            name = "Email Classification & Routing",
            description = "Classify a customer's email and draft a concise, polite reply.",
            defaultInput = "Hey, I ordered a laptop (Order #98124) last week with expedited 2-day shipping. It's been 5 days and the tracking still says 'Label Created'. I need this for a work presentation on Monday! This is unacceptable, I want a refund on my shipping fees.",
            zeroShotTemplate = { input ->
                """
                Classify the following customer email into Support, Sales, or Billing, and draft a short reply to the customer.
                
                Customer Email:
                "$input"
                """.trimIndent()
            },
            fewShotTemplate = { input ->
                """
                Classify the customer email into Support, Sales, or Billing, and draft a short reply.
                
                Example 1:
                Email: "Hi, I received my bill today and there is a charge of ${'$'}49 that I don't recognize. Can you please explain what this is for?"
                Classification: Billing
                Draft Reply: "Hello, I would be happy to look into that ${'$'}49 charge for you. Could you please provide your account number so I can check your billing history? Thank you!"
                
                Example 2:
                Email: "I am interested in upgrading our team to the enterprise plan. We have 25 users. Who should I speak with to get a custom quote?"
                Classification: Sales
                Draft Reply: "Hello, thank you for your interest in our Enterprise plan! I've connected you with our sales team who will send over a custom quote for 25 users shortly. Have a great day!"
                
                Example 3:
                Email: "My app keeps crashing every time I try to upload a profile picture. I've reinstalled it twice but it didn't help."
                Classification: Support
                Draft Reply: "Hello, we are sorry to hear that the app is crashing during photo uploads. I have forwarded this issue to our technical support team to investigate. We will get back to you shortly."
                
                Now classify and reply to this email:
                Email: "$input"
                """.trimIndent()
            },
            structuredTemplate = { input ->
                """
                # ROLE
                You are a highly efficient customer service triage assistant for an e-commerce platform.
                
                # TASK
                Analyze the customer email below, classify it into the appropriate department, and draft a response.
                
                # CLASSIFICATION CATEGORIES
                - Support: Technical bugs, order delivery delays, damaged items, tracking issues.
                - Sales: Product inquiries, bulk purchase quotes, subscription upgrades.
                - Billing: Incorrect charges, refund requests, payment method updates.
                
                # RESPONSE CONSTRAINTS
                - Professional, empathetic, and polite tone.
                - The reply must be under 4 sentences.
                - Acknowledge the specific complaint directly.
                - Address any compensation requests (like refunding shipping fees) if reasonable.
                
                # INPUT EMAIL
                "$input"
                
                # OUTPUT FORMAT (Use exact keys)
                DEPARTMENT: [Support / Sales / Billing]
                CONFIDENCE: [Score 0.0 - 1.0]
                REASONING: [Brief explanation of why you classified it here]
                DRAFT_REPLY: [Your drafted customer email response]
                """.trimIndent()
            }
        ),
        PromptTemplate(
            id = "sql_gen",
            name = "Natural Language to SQL",
            description = "Convert a plain English description into a clean PostgreSQL query.",
            defaultInput = "Show me the top 5 customers by total purchase amount in the year 2025 who live in California or Washington, along with their total order count.",
            zeroShotTemplate = { input ->
                """
                Convert this request into a PostgreSQL query:
                "$input"
                """.trimIndent()
            },
            fewShotTemplate = { input ->
                """
                Convert natural language requests into PostgreSQL queries.
                
                Example 1:
                Request: "Find all employees hired in the last 6 months earning more than 80000."
                SQL:
                SELECT first_name, last_name, hire_date, salary 
                FROM employees 
                WHERE hire_date >= CURRENT_DATE - INTERVAL '6 months' 
                  AND salary > 80000;
                
                Example 2:
                Request: "List each product category along with its average price and total stock, but only for categories with more than 10 products."
                SQL:
                SELECT category, AVG(price) AS average_price, SUM(stock) AS total_stock
                FROM products
                GROUP BY category
                HAVING COUNT(id) > 10;
                
                Example 3:
                Request: "Get the customer name and the date of their most recent order."
                SQL:
                SELECT c.name, MAX(o.order_date) AS latest_order_date
                FROM customers c
                JOIN orders o ON c.id = o.customer_id
                GROUP BY c.id, c.name;
                
                Now convert this request:
                Request: "$input"
                """.trimIndent()
            },
            structuredTemplate = { input ->
                """
                # ROLE
                You are an expert database administrator and PostgreSQL developer.
                
                # CONTEXT
                The query will be run on a relational database with the following schema:
                - `customers` (id INT PRIMARY KEY, name VARCHAR, state VARCHAR, email VARCHAR)
                - `orders` (id INT PRIMARY KEY, customer_id INT REFERENCES customers(id), order_date DATE, total_amount DECIMAL)
                - `order_items` (id INT PRIMARY KEY, order_id INT REFERENCES orders(id), product_id INT, quantity INT, price DECIMAL)
                
                # TASK
                Write a syntactically correct, highly optimized PostgreSQL query based on the natural language request.
                
                # CONSTRAINTS
                - Use proper explicit SQL JOINs.
                - Group by necessary non-aggregated columns.
                - Filter dates appropriately using standard PostgreSQL date functions.
                - Output ONLY valid SQL code. Do not wrap the query in anything other than markdown code blocks.
                
                # REQUEST
                "$input"
                
                # OUTPUT FORMAT
                Provide your response in this structure:
                -- DESCRIPTION: [Briefly explain the approach and what columns/tables are utilized]
                -- QUERY:
                [Your PostgreSQL Query Here]
                """.trimIndent()
            }
        ),
        PromptTemplate(
            id = "data_extraction",
            name = "JSON Receipt Data Extraction",
            description = "Extract structured billing information from unstructured receipt text.",
            defaultInput = "VALLEY ORGANIC MARKET\nStore #204 - Tel: 555-0199\nDate: 04/12/2026 14:32\n---------------------------\n2x Organic Bananas @ 1.49    2.98\n1x Oat Milk 1L              4.50\n1x Sourdough Bread          5.20\n3x Almond Croissant @ 3.50 10.50\n---------------------------\nSUBTOTAL                    23.18\nTAX (8.5%)                   1.97\nTOTAL                       25.15\n---------------------------\nPaid via Visa ************4421\nThank you for shopping local!",
            zeroShotTemplate = { input ->
                """
                Extract the items, store name, date, and total from this receipt in JSON format.
                
                Receipt:
                $input
                """.trimIndent()
            },
            fewShotTemplate = { input ->
                """
                Extract structured JSON data from receipt text.
                
                Example 1:
                Receipt:
                "CAFE BREW\n12/01/2026\n1 Espresso: 3.50\nTotal: 3.50\nCash"
                JSON:
                {
                  "store": "CAFE BREW",
                  "date": "2026-01-12",
                  "items": [
                    { "name": "Espresso", "quantity": 1, "price": 3.50 }
                  ],
                  "total": 3.50,
                  "paymentMethod": "Cash"
                }
                
                Example 2:
                Receipt:
                "GAS STOP\nDate: 05/02/2026\nUnleaded Fuel: 45.00\nSnack: 2.50\nTotal: 47.50"
                JSON:
                {
                  "store": "GAS STOP",
                  "date": "2026-02-05",
                  "items": [
                    { "name": "Unleaded Fuel", "quantity": 1, "price": 45.00 },
                    { "name": "Snack", "quantity": 1, "price": 2.50 }
                  ],
                  "total": 47.50,
                  "paymentMethod": "Unknown"
                }
                
                Now extract this receipt:
                Receipt:
                $input
                """.trimIndent()
            },
            structuredTemplate = { input ->
                """
                # ROLE
                You are a high-precision data extraction parser specializing in optical character recognition (OCR) parsing of purchase receipts.
                
                # TASK
                Parse the unstructured receipt text below into a structured JSON payload.
                
                # SCHEMATIC CONSTRAINTS
                The output JSON must strictly contain these fields:
                - `merchant_name`: String (name of the store)
                - `transaction_date`: String in YYYY-MM-DD format (convert from input date)
                - `items`: Array of Objects, each having:
                  - `item_name`: String
                  - `quantity`: Integer (default 1 if not specified)
                  - `unit_price`: Float (default total price divided by quantity if individual price not present)
                  - `total_price`: Float
                - `subtotal`: Float
                - `tax`: Float
                - `grand_total`: Float
                - `payment_card_last_four`: String (4-digit card number suffix if paid by card, otherwise null)
                
                # RECEIPT TEXT
                $input
                
                # OUTPUT FORMAT
                Return ONLY valid JSON. No conversational text, no markdown block wrappers around the JSON. Keep it raw.
                """.trimIndent()
            }
        ),
        PromptTemplate(
            id = "custom",
            name = "Custom Prompt Scenario",
            description = "Write your own input text and customize prompts for complete control.",
            defaultInput = "Write a motivational motto for a group of software engineers launching a prompt engineering tool.",
            zeroShotTemplate = { input -> input },
            fewShotTemplate = { input -> input },
            structuredTemplate = { input -> input }
        )
    )
}
