
# README Template

Your [design product spec](https://hackmd.io/s/H1wGpVUh7) (described in that link) will look like the following in your README:

## 1. User Stories (Required and Optional)

**Required Must-have Stories**

 * [Login/Signup screen]
   * [Set home location]
   * [Toggle between local and visitor]
 * [Lyft map to see locals nearby]
   * [Click on dot to see their profile]
   * [Ratings]
 * [Select person, (within radius), time, date]
    * [Display everyone nearby, only show preferences]
    * [search for preferences]

**Optional Nice-to-have Stories**

 * [Group meetup]
 * [Messaging within the app]
 * [Cancel]

## 2. Screen Archetypes

 * [Login/Signup]
   * [Set home location]

 * [Home]
   * [Any commitments]
   * Visitor/local toggle
   
 * [Map screen]
   * Lyft map
   * Click on area to display people
   * Select date, list of people shows different time ranges
     * No overlapping times 

 * [Local Selection Screen]
   * Can require 21+
   * Select date times

 * [Settings Screen]
   * Choose your preferences
   * Update location

 * [Calendar Screen]
   * Show upcoming events

## 3. Navigation

**Tab Navigation** (Tab to Screen)

 * [Home tab]
 * [Local selection tab / Visitor map tab]
 * [Calendar tab]
 * [Settings tab]

**Flow Navigation** (Screen to Screen)

 * [Home screen]
   * [Settings action view]
   * [Swipe right for visitor screen]
   * [Swipe left for local screen]
 * [Local/Visitor screen]
   * [Settings action view]
   * [Swipe back for home screen]


## Schema Design

User object
  - id
  - name
  - username
  - email
  - password
  - profile picture
  - bio
  - [optional] phone number

Event object
  - id
  - address
  - food type
  - restaurant/home-cooked
  - date/time
  - 21+ toggle
  - Number of guests
  - Pointer to host User
  - Pointer to accepted visitors
  - Pointer to pending visitors
  - description
  - Pointer to Conversation

Conversation object
  - id
  - array of Users
  - array of Messages
  (deleted after event)

Messages object
  - id
  - Pointer to sender User
  - body
  
