---
name: email-automation
description: Send automated emails, manage email campaigns, and handle email workflows. Use when needing email communication, notifications, or marketing automation.
license: MIT
metadata:
  author: agent-skill-team
  version: "1.0"
compatibility: Requires SMTP server access and email credentials
---

# Email Automation Skill

## When to use this skill
Use this skill when you need to send emails automatically, manage email marketing campaigns, handle transactional emails, or create email-based workflows. Supports various email providers and template systems.

## How to send emails

### Basic Email Sending
1. **Configure**: Set up SMTP connection and authentication
2. **Compose**: Create email content with templates
3. **Send**: Deliver emails with tracking and delivery confirmation
4. **Monitor**: Track delivery status and handle bounces

### Parameters
- `to` (required): Recipient email address(es) - can be single email or array
- `subject` (required): Email subject line
- `body` (optional): Email body content (HTML or plain text)
- `template` (optional): Template name for email formatting
- `template_data` (optional): Data for template variable substitution
- `cc` (optional): Carbon copy recipients
- `bcc` (optional): Blind carbon copy recipients
- `attachments` (optional): Array of file paths to attach
- `priority` (optional): Email priority - "high", "normal", "low" (default: "normal")
- `send_at` (optional): Scheduled send time (ISO 8601 format)

### Example
```
Request: "Send welcome email to new user"
Parameters: {
  "to": "john.doe@example.com",
  "subject": "Welcome to Our Service!",
  "template": "welcome-email",
  "template_data": {
    "first_name": "John",
    "product_name": "Amazing Service"
  },
  "priority": "high"
}
```

## Email Templates

### Template System
Templates use variable substitution with `{{variable}}` syntax:

#### Welcome Template
```html
<!DOCTYPE html>
<html>
<head>
    <title>Welcome {{first_name}}!</title>
</head>
<body>
    <h1>Welcome, {{first_name}}!</h1>
    <p>Thank you for joining {{product_name}}.</p>
</body>
</html>
```

#### Newsletter Template
```html
<html>
<body>
    <h2>{{newsletter_title}}</h2>
    <div>{{content}}</div>
    <p>Unsubscribe: {{unsubscribe_link}}</p>
</body>
</html>
```

#### Password Reset Template
```html
<html>
<body>
    <h1>Password Reset Request</h1>
    <p>Click here to reset: {{reset_link}}</p>
    <p>Link expires: {{expiry_time}}</p>
</body>
</html>
```

## Campaign Management

### Bulk Email Sending
1. **Campaign setup**: Define campaign parameters and goals
2. **Audience segmentation**: Target specific user groups
3. **A/B testing**: Test different subject lines and content
4. **Scheduling**: Optimal send time determination
5. **Analytics**: Track opens, clicks, and conversions

### Campaign Parameters
- `campaign_name` (required): Internal campaign identifier
- `audience_segment` (optional): Target user segment criteria
- `ab_test_config` (optional): A/B test variations
- `schedule` (optional): Send schedule and frequency
- `analytics_tracking` (optional): Enable click and open tracking (default: true)

### Example
```
Request: "Launch product announcement campaign"
Parameters: {
  "campaign_name": "product-launch-2024",
  "audience_segment": "premium-users",
  "ab_test_config": {
    "subject_a": "Introducing Amazing Product!",
    "subject_b": "You're Invited: Amazing Product Launch!"
  },
  "schedule": {
    "send_time": "2024-03-15T09:00:00Z",
    "timezone": "America/New_York"
  }
}
```

## Email Providers

### Supported Providers
- **SMTP**: Generic SMTP server configuration
- **Gmail**: Gmail API integration
- **Outlook**: Microsoft Graph API
- **SendGrid**: Email delivery service
- **Mailgun**: Email API service
- **AWS SES**: Amazon Simple Email Service

### Provider Configuration
```json
{
  "provider": "smtp",
  "config": {
    "host": "smtp.example.com",
    "port": 587,
    "use_tls": true,
    "username": "user@example.com",
    "password": "{{encrypted_password}}"
  }
}
```

## Delivery Management

### Bounce Handling
- **Hard bounces**: Invalid email addresses
- **Soft bounces**: Temporary delivery issues
- **Auto-unsubscribe**: Remove invalid addresses
- **Retry logic**: Exponential backoff for failed sends

### Analytics Integration
- **Open tracking**: Pixel tracking for email opens
- **Click tracking**: Link engagement measurement
- **Conversion tracking**: Goal completion measurement
- **Geographic analytics**: Location-based engagement data

## Scripts and Tools

### `scripts/email-sender.py`
Core email sending functionality:
- SMTP connection management
- Template rendering with Jinja2
- Attachment handling and encoding
- Delivery retry logic

### `scripts/campaign-manager.py`
Email campaign orchestration:
- Audience segmentation
- A/B test execution
- Scheduling and queue management
- Performance analytics

### `scripts/template-engine.py`
Template processing system:
- Variable substitution and validation
- Multi-language support
- Conditional content blocks
- Dynamic content generation

### `scripts/analytics-tracker.py`
Email performance tracking:
- Open rate calculation
- Click-through analysis
- Conversion funnel tracking
- Geographic and device analytics

## Security Features

### Authentication & Security
- **API key management**: Secure credential storage
- **Rate limiting**: Prevent abuse and ensure deliverability
- **Content scanning**: Spam detection and prevention
- **Encryption**: TLS/SSL for secure transmission

### Compliance
- **GDPR**: Data protection and consent management
- **CAN-SPAM**: Canadian anti-spam compliance
- **CASL**: Commercial email best practices
- **Unsubscribe**: Easy opt-out mechanisms

## Error Handling

### Common Issues
- **Authentication failures**: Invalid credentials or expired tokens
- **Rate limiting**: Too many requests in time window
- **Content blocks**: Spam filter triggers
- **Bounce processing**: Invalid or unreachable addresses

### Recovery Strategies
- **Fallback providers**: Alternate sending routes
- **Queue management**: Delayed retry for failed sends
- **Alert notifications**: Admin alerts for critical failures
- **Manual review**: Queue problematic emails for human review

This skill provides comprehensive email automation capabilities with enterprise-grade features for transactional and marketing email workflows.