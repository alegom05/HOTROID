# Chatbot System Implementation for HOTROID

## Overview
This document describes the implementation of an interactive chatbot system for hotels that allows users to make predefined queries through numbered options and retrieve information from Firestore Database.

## Implemented Features

### 1. Predefined Options System
The chatbot provides the following menu options:
- **Option 1**: Hotel general information
- **Option 2**: View my reservations
- **Option 3**: Available services
- **Option 4**: Customer service/Contact
- **Option 5**: Hotel policies
- **Option 0**: Return to main menu

### 2. Firestore Integration
The system integrates with Firestore to query:
- Specific hotel information
- Current user reservations
- Services and amenities
- Contact information
- Hotel policies

### 3. Modified Files

#### ChatItem.java
- Added `isChatbot` boolean flag to distinguish chatbot from regular chats
- Added constructor with chatbot parameter
- Added getter/setter methods for chatbot flag

#### ChatFragment.java
- Added sample chatbot chat item with robot emoji indicator
- Updated intent extras to pass chatbot flag to detailed chat view

#### ChatDetalladoUser.java
- Added chatbot detection and initialization
- Implemented chatbot response processing
- Updated toolbar to show "Virtual Assistant - Online" for chatbots
- Added chatbot welcome message display
- Integrated with ChatbotManager for option processing

#### Message.java
- Extended MessageType enum with `CHATBOT_OPTIONS` and `CHATBOT_RESPONSE`

#### MessageAdapter.java
- Added `VIEW_TYPE_CHATBOT` for chatbot messages
- Updated view type detection to handle chatbot messages
- Added chatbot message layout support

### 4. New Classes

#### ChatbotManager.java
Main chatbot logic manager that handles:
- Option menu display
- User input processing (1-5, 0)
- Integration with FirestoreService for data retrieval
- Formatted response generation with emojis and structure
- Error handling and validation

#### FirestoreService.java
Singleton service class for Firestore operations:
- Hotel information queries
- User reservation retrieval
- Hotel services lookup
- Contact information access
- Policy data retrieval
- Default response generation for missing data

#### ChatbotOption.java
Model class representing chatbot menu options:
- Option ID, title, description, and number
- Designed for future Firestore storage of dynamic options

### 5. UI Enhancements

#### item_message_chatbot.xml
New layout for chatbot messages featuring:
- Robot emoji and "Virtual Assistant" label
- Different styling from regular hotel messages
- Clear visual distinction for bot responses

### 6. Firestore Data Structure
The system expects the following Firestore structure:

```
hotels/{hotelId}/
  - name: "Hotel Name"
  - description: "Hotel Description"
  - amenities: "List of amenities"
  - contact: "Contact info"
  - phone: "Phone number"
  - email: "Email address"
  - address: "Hotel address"
  - policies: "General policies"
  - checkInTime: "3:00 PM"
  - checkOutTime: "12:00 PM"
  - cancellationPolicy: "Cancellation terms"
  
  services/{serviceId}/
    - name: "Service Name"
    - description: "Service Description"
    - price: "Service Price"

users/{userId}/
  reservations/{reservationId}/
    - hotelId: "hotel_reference"
    - checkIn: "Check-in date"
    - checkOut: "Check-out date"
    - status: "active/completed/cancelled"
    - details: "Reservation details"

chatbot_responses/{hotelId}/
  - options: {1: "info", 2: "reservations", 3: "services", 4: "contact", 5: "policies"}
  - responses: {info: "Custom info text", contact: "Custom contact text", policies: "Custom policies text"}
```

## Interaction Flow

1. **User opens chat**: System detects if it's a chatbot based on `isChatbot` flag
2. **Welcome message**: Chatbot displays menu with options 1-5 and 0
3. **User selects option**: Types a number (1-5 for actions, 0 for menu)
4. **System processes**: ChatbotManager validates input and calls appropriate FirestoreService method
5. **Data retrieval**: FirestoreService queries Firestore for relevant information
6. **Response formatting**: ChatbotManager formats response with emojis and structure
7. **Display result**: Formatted response shown in chat with chatbot styling
8. **Continue conversation**: User can select another option or return to menu

## Key Features

### Error Handling
- Input validation for numeric options (0-5)
- Firestore query error handling with user-friendly messages
- Default responses when data is unavailable

### User Experience
- Clear visual distinction between user, hotel, and chatbot messages
- Emoji-rich responses for better readability
- Structured information display
- Consistent navigation with option 0 for main menu

### Scalability
- Modular design allows easy addition of new options
- FirestoreService can be extended for additional data types
- ChatbotManager supports dynamic option loading from Firestore

## Testing Recommendations

1. **Chatbot Detection**: Verify chatbot chats show "🤖 Virtual Assistant" indicator
2. **Menu Display**: Confirm welcome message appears with all options
3. **Option Processing**: Test each option (1-5) processes correctly
4. **Firestore Integration**: Verify data retrieval from Firestore collections
5. **Error Scenarios**: Test invalid input and missing data handling
6. **UI Rendering**: Check chatbot messages display with correct styling

## Future Enhancements

1. **Rich Media Support**: Add image and file attachments for service information
2. **Multilingual Support**: Implement language selection for international hotels
3. **AI Integration**: Connect with AI services for natural language processing
4. **Analytics**: Track popular queries and user interaction patterns
5. **Personalization**: Customize responses based on user history and preferences

## Technical Notes

- All Firestore operations are asynchronous with callback interfaces
- UI updates are performed on the main thread using `runOnUiThread()`
- The system uses singleton pattern for ChatbotManager and FirestoreService
- Message types are handled through enum for type safety
- Layout inflation is optimized through view type detection in RecyclerView

This implementation provides a solid foundation for hotel chatbot functionality while maintaining the existing chat system architecture and allowing for future enhancements.