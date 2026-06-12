# Recipe Planner

Recipe Planner is a web application for managing recipes, planning meals, and creating shopping lists. It provides user authentication, role-based access control, and a RESTful API.

## Features

- User registration and authentication (JWT)
- Recipe management (CRUD)
- Categorization of recipes
- Ingredient management and recipe-ingredient linking
- Meal planning (with meal types: breakfast, lunch, dinner, snack)
- Shopping list generation
- Admin and regular user roles
- API documentation (OpenAPI / Swagger UI)
- Health monitoring (Actuator)

## Technology Stack

- **Java 25**
- **Spring Boot 4.1.0** (Spring MVC, Spring Data JPA, Spring Security)
- **PostgreSQL** (production) / **H2** (testing)
- **Liquibase** for database migrations
- **JWT** for stateless authentication
- **Gradle** as build tool
- **Lombok** to reduce boilerplate code
- **OpenAPI (springdoc-openapi)** for API documentation
- **JUnit & Mockito** for testing

## API Endpoints Guide

You can check the project's functionality using the **swagger-ui** link

### 🔐 Authentication

**POST /api/auth/register** – Register a new user
```json
{
  "username": "john",
  "email": "john@test.com",
  "password": "password123"
}
```

**POST /api/auth/login** – Login

```json
{
  "username": "john",
  "password": "password123"
}
```
Copy the token from the response → click Authorize 🔒 at the top → paste the token (without the word Bearer).

### 📖 Recipes
**GET /api/recipes** – List recipes with filters  
Pageable example:

```json
{
  "page": 0,
  "size": 10
}
```
Optional filters: title, categoryId, difficulty (leave empty if not needed).  
- Difficulty values: EASY, MEDIUM, HARD.
- Title = Scrambled
- CategoryId = 1

**GET /api/recipes/{id}** – Get recipe by ID (e.g. id = 1)

**GET /api/recipes/my** – My recipes (requires token)  
Pageable as above.

**POST /api/recipes** – Create a recipe (requires token)

```json
{
  "title": "Omelette",
  "description": "Simple and quick omelette",
  "instructions": "1. Beat eggs with milk and salt.\n2. Melt butter in pan.\n3. Pour egg mixture and cook 3 minutes.\n4. Fold and serve.",
  "cookingTimeMinutes": 10,
  "servings": 1,
  "difficulty": "EASY",
  "categoryId": 1,
  "ingredients": [
    { "ingredientId": 6, "quantity": 3, "notes": "large eggs" },
    { "ingredientId": 7, "quantity": 50, "notes": null }
  ]
}
```
**PUT /api/recipes/{id}** – Update a recipe (requires token, owner only)

```json
{
  "title": "Scrambled Eggs Updated",
  "description": "Even creamier version",
  "instructions": "1. Whisk eggs.\n2. Cook on low heat.\n3. Serve.",
  "cookingTimeMinutes": 8,
  "servings": 2,
  "difficulty": "EASY",
  "categoryId": 1
}
```
**DELETE /api/recipes/{id}** – Delete a recipe (requires token, owner only)

### 🥕 Ingredients
**GET /api/ingredients** – List ingredients  
Pageable: 
```json
{
  "page": 0,
  "size": 20
}
```
optional search (e.g. Chicken)

**GET /api/ingredients/{id}** – Get ingredient by ID

**POST /api/ingredients** – Create ingredient (requires token, admin)

```json
{
  "name": "Broccoli",
  "unit": "g",
  "caloriesPerUnit": 0.34
}
```
**PUT /api/ingredients/{id}** – Update ingredient (admin)

```json
{
  "name": "Chicken breast",
  "unit": "g",
  "caloriesPerUnit": 1.65
}
```
**DELETE /api/ingredients/{id}** – Delete ingredient (admin)

### 🗂 Categories
**GET /api/categories** – All categories (no parameters)

**GET /api/categories/{id}** – Category by ID

**POST /api/categories** – Create category (admin)

```json
{
  "name": "Smoothies",
  "description": "Healthy blended drinks"
}
```
**PUT /api/categories/{id}** – Update category (admin)

```json
{
  "name": "Breakfast",
  "description": "Updated description"
}
```
**DELETE /api/categories/{id}** – Delete category (admin)

### 📅 Meal Plans
**GET /api/meal-plans/week** – Weekly meal plan (requires token)  
Query params:
- from = 2026-05-31
- to = 2026-06-06

**GET /api/meal-plans/day** – Daily meal plan (requires token)  
Query param: date = 2026-05-31

**POST /api/meal-plans** – Add a dish to meal plan (requires token)

```json
{
  "recipeId": 1,
  "plannedDate": "2026-06-01",
  "mealType": "BREAKFAST",
  "servings": 1,
  "notes": "With orange juice"
}
```
Meal types: BREAKFAST, LUNCH, DINNER, SNACK

**PUT /api/meal-plans/{id}** – Update a meal plan entry (requires token)

```json
{
  "recipeId": 2,
  "plannedDate": "2026-06-01",
  "mealType": "DINNER",
  "servings": 2,
  "notes": "For two people"
}
```
**DELETE /api/meal-plans/{id}** – Remove from meal plan (requires token)

### 🛒 Shopping List
**GET /api/shopping-list** – My shopping list (requires token)

**POST /api/shopping-list** – Add an item (requires token)

```json
{
  "ingredientId": 2,
  "quantity": 400
}
```
**PATCH /api/shopping-list/{id}/toggle** – Toggle purchased/unpurchased (requires token)

**DELETE /api/shopping-list/{id}** – Delete an item (requires token)

**DELETE /api/shopping-list/purchased** – Clear purchased items (requires token)

### 💡 Typical Workflow
**POST /api/auth/login** → get token  
Insert token into Authorize button

**GET /api/recipes** – browse recipes

**POST /api/recipes** – create your own recipe

**POST /api/meal-plans** – add recipe to meal plan

**POST /api/shopping-list** – add ingredients to shopping list

**PATCH /api/shopping-list/{id}/toggle** – mark as purchased

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/TheRyffo/RecipePlanner.git
cd recipeplanner

