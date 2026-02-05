---
name: api-integration
description: Connect to external APIs, fetch data, handle authentication, and manage API workflows. Use when needing to integrate with third-party services, webhooks, or external data sources.
license: MIT
metadata:
  author: agent-skill-team
  version: "1.0"
compatibility: Requires internet access and API credentials
---

# API Integration Skill

## When to use this skill
Use this skill when you need to connect to external APIs, fetch data from web services, handle authentication, or create API-based workflows. Supports REST APIs, GraphQL, webhooks, and various authentication methods.

## How to connect to APIs

### Basic API Request
1. **Authentication**: Handle API credentials and tokens
2. **Request**: Construct HTTP requests with proper headers
3. **Response**: Parse and validate API responses
4. **Error Handling**: Manage rate limits and failures
5. **Data Processing**: Transform and store returned data

### Parameters
- `api_url` (required): API endpoint URL
- `method` (optional): HTTP method - "GET", "POST", "PUT", "DELETE" (default: "GET")
- `headers` (optional): Custom HTTP headers
- `body` (optional): Request body payload
- `auth_type` (optional): Authentication type - "bearer", "basic", "api_key", "oauth"
- `credentials` (optional): Authentication credentials object
- `timeout` (optional): Request timeout in seconds (default: 30)

### Example
```
Request: "Fetch user data from CRM API"
Parameters: {
  "api_url": "https://api.crm.com/v2/users/12345",
  "method": "GET",
  "auth_type": "bearer",
  "credentials": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  },
  "timeout": 60
}
```

## Authentication Methods

### Bearer Token
- **Usage**: OAuth 2.0 and JWT tokens
- **Header**: `Authorization: Bearer <token>`
- **Refresh**: Automatic token renewal when expired

### API Key
- **Usage**: Simple key-based authentication
- **Header**: `X-API-Key: <key>` or custom header
- **Rotation**: Support for key rotation policies

### Basic Auth
- **Usage**: Username/password authentication
- **Header**: `Authorization: Basic <base64 credentials>`
- **Security**: HTTPS required for production use

### OAuth 2.0 Flow
- **Authorization**: Full OAuth 2.0 implementation
- **Scopes**: Request specific permission scopes
- **Refresh**: Handle token expiration gracefully

## API Response Handling

### Response Formats
- **JSON**: Standard JSON response parsing
- **XML**: XML response processing
- **CSV**: Comma-separated value parsing
- **Binary**: File download and processing

### Error Handling
- **HTTP Status**: Proper error code handling
- **Rate Limits**: Exponential backoff for retries
- **API Errors**: Parse and categorize API error messages
- **Network Issues**: Timeout and connectivity management

### Success Response Structure
```json
{
  "data": { /* actual response data */ },
  "metadata": {
    "request_id": "req_123456",
    "timestamp": "2024-03-15T10:30:00Z",
    "rate_limit_remaining": 4500
  }
}
```

### Error Response Structure
```json
{
  "error": {
    "code": "RATE_LIMIT_EXCEEDED",
    "message": "API rate limit exceeded. Try again in 60 seconds.",
    "details": {
      "retry_after": "2024-03-15T10:31:00Z",
      "limit": 5000,
      "window": "1hour"
    }
  }
}
```

## API Types and Workflows

### REST APIs
- **CRUD Operations**: Create, Read, Update, Delete
- **Pagination**: Handle large result sets
- **Filtering**: Query parameters and sorting
- **Batch Operations**: Bulk request processing

### GraphQL APIs
- **Query Construction**: GraphQL query building
- **Variable Handling**: Parameter substitution
- **Schema Introspection**: Dynamic API discovery
- **Subscription Management**: Real-time data updates

### Webhook Handling
- **Event Reception**: Receive and process webhook events
- **Signature Verification**: Validate webhook authenticity
- **Event Routing**: Forward to appropriate handlers
- **Acknowledge**: Confirm receipt to webhook sender

## Configuration and Caching

### API Configuration
```yaml
api_configs:
  weather_api:
    base_url: "https://api.weather.com/v1"
    auth_type: "api_key"
    rate_limit: 1000_per_hour
    retry_config:
      max_retries: 3
      backoff_factor: 2
  crm_api:
    base_url: "https://api.crm.com/v2"
    auth_type: "oauth"
    oauth_flow: "authorization_code"
```

### Caching Strategy
- **Response Caching**: Store API responses to reduce calls
- **Rate Limit Awareness**: Cache based on API limits
- **Invalidation**: Smart cache invalidation triggers
- **TTL Management**: Time-based cache expiration

## Scripts and Tools

### `scripts/api-client.py`
Generic API interaction framework:
- HTTP client with retries and timeouts
- Authentication handling for all types
- Request/response logging
- Automatic pagination support

### `scripts/oauth-handler.py`
OAuth 2.0 flow implementation:
- Authorization code exchange
- Token refresh automation
- Secure token storage
- Multi-provider support

### `scripts/webhook-server.py`
Webhook event receiver:
- Flask/FastAPI webhook endpoint
- Signature validation and security
- Event routing and processing
- Async event handling

### `scripts/rate-limiter.py`
API rate limit management:
- Token bucket algorithm
- Sliding window tracking
- Automatic backoff calculation
- Priority queue support

## Monitoring and Analytics

### API Performance
- **Response Time**: Track API latency
- **Success Rate**: Monitor successful requests
- **Error Analysis**: Categorize and track failures
- **Usage Patterns**: Identify optimization opportunities

### Metrics Collection
```json
{
  "api_metrics": {
    "requests_total": 15420,
    "success_rate": 98.5,
    "average_response_time": 245,
    "errors_by_type": {
      "rate_limit": 120,
      "timeout": 35,
      "auth_error": 85
    }
  }
}
```

## Security Best Practices

### Credential Management
- **Encrypted Storage**: Never store plain text credentials
- **Key Rotation**: Regular API key rotation
- **Environment Variables**: Use secure env var storage
- **Access Control**: Principle of least privilege

### Request Security
- **HTTPS Only**: Enforce TLS for all requests
- **Input Validation**: Sanitize all user inputs
- **SQL Injection Prevention**: Use parameterized queries
- **Request Signing**: HMAC signatures when required

This skill provides comprehensive API integration capabilities with enterprise-grade security and monitoring features for reliable external service connectivity.