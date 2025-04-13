# **User Management Service**
User Management Service for my Ecommerce backend application "Vibe vault".


## SessionStatus Enum Values and Use Cases

The `SessionStatus` enum represents different states of authentication tokens in the user service. Here's what each status means:

### ACTIVE
- A valid, currently usable token
- User is properly authenticated
- All protected resources can be accessed
- Default status when a user successfully logs in

### INACTIVE
- Token exists but is temporarily not usable
- May indicate account suspension or system maintenance
- Could be used when a session is paused due to inactivity but could be reactivated
- Prevents access without completely terminating the session

### EXPIRED
- Token has reached its time limit (expiredAt date)
- Natural end-of-life for tokens that weren't explicitly logged out
- Requires the user to authenticate again
- Security measure to limit the window of opportunity for token misuse

### BLACKLISTED
- Token has been explicitly invalidated due to security concerns
- Used when suspicious activity is detected
- Can indicate potential compromised credentials
- Prevents any further use regardless of expiration date

### LOGGED_OUT
- User has explicitly terminated their session
- Result of calling the logout endpoint
- Clean session termination
- Distinguishes from other invalidation reasons for auditing purposes
