# Content Filter Documentation

## Overview
The `ContentFilter` utility provides comprehensive content filtering to block adult and inappropriate content from being displayed in the IPTV app.

## Features

### 1. Category Filtering
Blocks channels based on their category names. The following categories are automatically blocked:

**English Keywords:**
- adult
- xxx
- 18+, 18 +
- porn
- erotic
- sex, sexy
- adults only
- mature
- nsfw
- x-rated
- r-rated

**International Keywords:**
- adulto (Spanish/Portuguese)
- adulte (French)
- erwachsene (German)
- 成人 (Chinese)
- 大人 (Japanese)
- 성인 (Korean)

### 2. Channel Name Filtering
Blocks channels if their name contains specific keywords:
- xxx
- porn
- adult
- sex
- erotic
- playboy
- hustler
- brazzers
- bangbros

### 3. Custom Blocking (Optional)
Administrators can add custom blocked categories at runtime:

```kotlin
// Add custom blocked category
ContentFilter.addCustomBlockedCategory("CustomCategory")

// Remove custom blocked category
ContentFilter.removeCustomBlockedCategory("CustomCategory")

// Clear all custom blocked categories
ContentFilter.clearCustomBlockedCategories()
```

## Usage

### Basic Usage

```kotlin
// Check if a category should be blocked
val isBlocked = ContentFilter.isBlockedCategory("Adult")

// Check if a channel name should be blocked
val isBlocked = ContentFilter.isBlockedChannelName("XXX Channel")

// Check both category and channel name
val shouldBlock = ContentFilter.shouldBlockContent("Channel Name", "Category")
```

### Integration with ChannelsProvider

The `ContentFilter` is automatically integrated into the `ChannelsProvider`:

1. **During M3U Parsing**: Channels with blocked categories or names are filtered out
2. **Category List**: Blocked categories don't appear in the category chips
3. **Logging**: Blocked content is logged for debugging purposes

## How It Works

1. **Case-Insensitive Matching**: All comparisons are case-insensitive
2. **Contains Check**: Blocks if the category/name contains any blocked keyword
3. **Automatic Filtering**: Applied during channel parsing, no manual intervention needed
4. **Logging**: All blocked content is logged with the tag "ContentFilter"

## Benefits

- **Family-Friendly**: Ensures the app is safe for all audiences
- **Comprehensive**: Covers multiple languages and variations
- **Extensible**: Easy to add new blocked keywords
- **Transparent**: Logs all blocked content for debugging
- **Performance**: Efficient Set-based lookups

## Maintenance

To add new blocked keywords:

1. Open `ContentFilter.kt`
2. Add keywords to `BLOCKED_CATEGORIES` or `BLOCKED_CHANNEL_KEYWORDS`
3. Keywords are automatically applied on next app launch

## Testing

To verify content filtering:

1. Check logs for "Blocked content" messages
2. Verify adult categories don't appear in category chips
3. Confirm adult channels are not in the channel list

## Privacy & Safety

This filter helps maintain:
- App Store compliance
- Family-friendly content
- User safety
- Brand reputation
